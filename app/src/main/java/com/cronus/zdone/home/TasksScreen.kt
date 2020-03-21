package com.cronus.zdone.home

import android.content.Context
import com.cronus.zdone.R
import com.cronus.zdone.Toaster
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.stats.TaskUpdateType
import com.cronus.zdone.timer.TaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.dropbox.android.external.store4.StoreResponse
import com.wealthfront.magellan.rx2.RxScreen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TasksScreen @Inject constructor(
    val tasksRepo: TasksRepository,
    val taskExecutionManager: TaskExecutionManager,
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
        CoroutineScope(Dispatchers.Main).launch {
            taskExecutionManager.currentTaskExecutionData
                .filterIsInstance<TaskExecutionState.AllTasksCompleted>()
                .collect {
                    // Tasks can be started and completed from multiple locations.
                    // If you start a single task from this screen and then finish it from
                    // the notification, we need to update the state of this screen to
                    // clear out the in progress task status. That happens here.
                    inProgressTask = null
                    view?.setTasksProgressState(TaskProgressState.READY)
                }
        }
    }

    internal fun requestTaskData() {
        mainScope.launch {
            tasksRepo.getTasksFromStore()
                .collect { response ->
                    when (response) {
                        is StoreResponse.Loading -> { toaster.showToast("Loading tasks") }
                        is StoreResponse.Error -> view?.showError(response.error.message)
                        is StoreResponse.Data -> {
                            val displayedTasks = getDisplayedTasks(response.value)
                            view?.setTasks(displayedTasks)
                        }
                    }
                }
        }
        mainScope.launch {
            tasksRepo.getTimeDataFromStore()
                .collect { response ->
                    when (response) {
                        is StoreResponse.Loading -> { }
                        is StoreResponse.Error -> { }
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
                }
        }
    }

    private fun getTimeProgress(timeAllocatedToday: Int, timeCompletedToday: Int) =
        (100 * timeCompletedToday.toDouble() / (timeAllocatedToday + timeCompletedToday)).toInt()

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
        if (tasksRepo.areTasksFromPreviousDay()) {
            refreshTaskData()
            toaster.showToast("New day has started since last task refresh. Updating tasks now...")
            return
        }
        updateTimeProgress(task)
        mainScope.launch {
            tasksRepo.updateTask(
                TasksRepository.TaskUpdateInfo(
                    id = task.id,
                    name = task.name,
                    subtaskId = task.subtaskId,
                    service = task.service,
                    expectedDurationSeconds = task.lengthMins * 60L,
                    actualDurationSeconds = null,
                    updateType = TaskUpdateType.COMPLETED))
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
                taskExecutionManager.cancelTasks()
                view?.setTasksProgressState(TaskProgressState.READY)
            }
        }
    }

    fun deferTask(task: DisplayedTask) {
        if (tasksRepo.areTasksFromPreviousDay()) {
            refreshTaskData()
            toaster.showToast("New day has started since last task refresh. Updating tasks now...")
            return
        }
        mainScope.launch {
            tasksRepo.updateTask(
                TasksRepository.TaskUpdateInfo(
                    id = task.id,
                    name = task.name,
                    subtaskId = task.subtaskId,
                    service = task.service,
                    expectedDurationSeconds = task.lengthMins * 60L,
                    actualDurationSeconds = null,
                    updateType = TaskUpdateType.DEFERRED))
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
        toaster.showToast("Starting task ${task.name}")
        mainScope.launch {
            val tasks = tasksRepo.getTasksFromStore()
                .map { it.dataOrNull() }
                .filterNotNull()
                .first()
            val apiTask = tasks.filter { it.id == task.id }
            taskExecutionManager.startTasks(apiTask)
        }
    }

    fun pauseTask(task: DisplayedTask) {
        view?.setTasksProgressState(TaskProgressState.READY)
        inProgressTask?.let {
            if (task == inProgressTask) {
                inProgressTask = null
                taskExecutionManager.cancelTasks()
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
