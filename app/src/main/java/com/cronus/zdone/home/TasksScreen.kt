package com.cronus.zdone.home

import android.content.Context
import android.widget.Toast
import com.cronus.zdone.R
import com.cronus.zdone.Toaster
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.timer.TaskTimerManager
import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import com.wealthfront.magellan.rx2.RxScreen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class TasksScreen @Inject constructor(
    val tasksRepo: TasksRepository,
    val taskTimerManager: TaskTimerManager,
    val toaster: Toaster
) : RxScreen<TasksView>() {

    internal var inProgressTask: DisplayedTask? = null
    internal var currentTimeProcess: TimeProgress? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun createView(context: Context): TasksView {
        return TasksView(context, isLargeFingersModeEnabled(context))
    }

    private fun isLargeFingersModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences("zdone", Context.MODE_PRIVATE)
            .getBoolean("large_fingers_mode_key", false)
    }

    override fun getTitle(context: Context) = context.getString(R.string.tasks)

    override fun onSubscribe(context: Context?) {
        requestTaskData()
    }

    internal fun requestTaskData() {
        mainScope.launch {
            tasksRepo.getTasksFromStore()
                .collect { response ->
                    when (response) {
                        is StoreResponse.Loading -> {
                        }
                        is StoreResponse.Error -> view?.showError(response.error.message)
                        is StoreResponse.Data -> displayTasks(response.value)
                    }
                }
        }
        mainScope.launch {
            tasksRepo.getTimeDataFromStore()
                .collect { response ->
                    when (response) {
                        is StoreResponse.Loading -> {
                        }
                        is StoreResponse.Error -> view?.showError(response.error.message)
                        is StoreResponse.Data -> {
                            currentTimeProcess = response.value
                            view?.setTimeProgress(
                                getTimeProgress(
                                    response.value.timeAllocatedToday,
                                    response.value.timeCompletedToday
                                )
                            )
                        }
                    }
                    when (response.origin) {
                        ResponseOrigin.Cache -> toaster.showToast("Refreshed from cache")
                        ResponseOrigin.Fetcher -> toaster.showToast("Refreshed from network")
                    }
                }
        }
    }

    private fun getTimeProgress(timeAllocatedToday: Int, timeCompletedToday: Int) =
        (100 * timeCompletedToday.toDouble() / (timeAllocatedToday + timeCompletedToday)).toInt()

    private fun displayTasks(tasks: List<Task>) {
        val displayedTasks = getDisplayedTasks(tasks)
        view?.setTasks(displayedTasks)
    }

    internal fun getDisplayedTasks(tasks: List<Task>): List<DisplayedTask> {
        return tasks.flatMap { task ->
            var taskProgressState = TaskProgressState.READY
            if (inProgressTask?.id == task.id) {
                taskProgressState = TaskProgressState.IN_PROGRESS
            } else if (inProgressTask != null) {
                taskProgressState = TaskProgressState.WAITING
            }
            val topLevelTask = DisplayedTask(
                id = task.id,
                name = task.name,
                service = task.service,
                lengthMins = task.lengthMins,
                isSubtask = false,
                showDivider = task.subtasks.isNullOrEmpty(),
                progressState = taskProgressState
            )
            val result = mutableListOf(topLevelTask)
            task.subtasks?.mapIndexed { index, subTask ->
                if (!subTask.isCompleted()) {
                    result.add(
                        DisplayedTask(
                            id = topLevelTask.id,
                            subtaskId = subTask.id,
                            name = subTask.name,
                            service = subTask.service,
                            lengthMins = 0,
                            isSubtask = true,
                            showDivider = index == task.subtasks.size - 1,
                            progressState = taskProgressState // use parent progress state for subtasks
                        )
                    )
                }
            }
            result
        }
    }

    fun taskCompleted(task: DisplayedTask) {
        if (tasksRepo.taskIsPreviousDay(task)) {
            refreshTaskData()
            activity?.let {
                Toast.makeText(
                    it,
                    "New day has started since last task refresh. Updating tasks now...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        updateTimeProgress(task)
        mainScope.launch {
            tasksRepo.taskCompletedFromStore(
                TasksRepository.TaskUpdateInfo(
                    task.id,
                    task.subtaskId,
                    task.service,
                    null,
                    updateType = "complete"))
                .collect { completedResult ->
                    when (completedResult) {
                        is StoreResponse.Loading -> toaster.showToast("Telling server task complete")
                        is StoreResponse.Data -> {
                            if (completedResult.value.result == "success")
                                toaster.showToast("Completed task: ${task.name}")
                            else {
                                view?.showError(completedResult.value.message)
                                refreshTaskData()
                            }
                        }
                        is StoreResponse.Error -> {
                            toaster.showToast("Failed to mark ${task.name} as completed, reloading tasks...")
                            view?.showError(completedResult.error.message)
                            refreshTaskData() // ensure data is consistent with current task state
                        }
                    }
                }
        }
        updateInProgressTask(task)
    }

    private fun updateTimeProgress(justCompletedTask: DisplayedTask) {
        currentTimeProcess?.let {
            view?.setTimeProgress(
                getTimeProgress(
                    it.timeAllocatedToday,
                    it.timeCompletedToday + justCompletedTask.lengthMins
                )
            )
        }
    }

    private fun updateInProgressTask(completedOrDeferredTask: DisplayedTask) {
        inProgressTask?.let {
            if (completedOrDeferredTask == inProgressTask) {
                inProgressTask = null
                taskTimerManager.cancelTimer()
                view?.setTasksProgressState(TaskProgressState.READY)
            }
        }
    }

    fun deferTask(task: DisplayedTask) {
        if (tasksRepo.taskIsPreviousDay(task)) {
            refreshTaskData()
            toaster.showToast("New day has started since last task refresh. Updating tasks now...")
            return
        }
        mainScope.launch {
            tasksRepo.deferTaskFromStore(
                TasksRepository.TaskUpdateInfo(
                    task.id,
                    task.subtaskId,
                    task.service,
                    null,
                    updateType = "defer"))
                .collect { deferralResult ->
                    when (deferralResult) {
                        is StoreResponse.Loading -> toaster.showToast("Deferring task")
                        is StoreResponse.Data -> {
                            if (deferralResult.value.result == "success")
                                toaster.showToast("Deferred task to tomorrow: ${task.name}")
                            else {
                                view?.showError(deferralResult.value.message)
                                refreshTaskData()
                            }
                        }
                        is StoreResponse.Error -> {
                            toaster.showToast("Failed to defer ${task.name}, reloading tasks...")
                            view?.showError(deferralResult.error.message)
                            refreshTaskData() // ensure data is consistent with current task state
                        }
                    }
                }
        }
        updateInProgressTask(task)
    }

    fun refreshTaskData() {
        mainScope.launch {
            tasksRepo.refreshTaskDataFromStore()
        }
    }

    fun startTask(task: DisplayedTask) {
        view?.setInProgressTask(task)
        inProgressTask = task
        taskTimerManager.startTimer(task)
    }

    fun pauseTask(task: DisplayedTask) {
        view?.setTasksProgressState(TaskProgressState.READY)
        inProgressTask?.let {
            if (task == inProgressTask) {
                inProgressTask = null
                taskTimerManager.cancelTimer()
            }
        }
    }

    data class DisplayedTask(
        val id: String,
        val subtaskId: String? = null,
        val name: String,
        val service: String,
        val lengthMins: Int,
        val isSubtask: Boolean,
        val showDivider: Boolean,
        var progressState: TaskProgressState
    )

    enum class TaskProgressState {
        READY,
        IN_PROGRESS,
        WAITING
    }

}
