package com.cronus.zdone.timer

import android.content.Context
import android.widget.Toast
import com.cronus.zdone.AppExecutors
import com.cronus.zdone.CoroutineScreen
import com.cronus.zdone.R
import com.cronus.zdone.Toaster
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.dropbox.android.external.store4.StoreResponse
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.subscribe
import kotlinx.coroutines.flow.take
import java.lang.Math.abs
import javax.inject.Inject

class TimerScreen @Inject constructor(
    val tasksRepository: TasksRepository,
    val timer: TaskTimer,
    val toaster: Toaster
) :
    CoroutineScreen<TimerView>() {

    var currentTaskDurationSecs = 0L
    var taskTimerDisposable: Disposable? = null
    lateinit var remainingTasks: MutableList<Task>

    override fun createView(context: Context): TimerView =
        TimerView(context)

    override fun onShow(context: Context?) {
        safeLaunch {
            tasksRepository.getTasksFromStore()
                .collect { response ->
                    val state = when (response) {
                        is StoreResponse.Loading ->
                            ViewState(
                                "Time to get to work",
                                0,
                                0,
                                false,
                                TimerState.LOADING)
                        is StoreResponse.Data -> {
                            remainingTasks = response.value.toMutableList()
                            ViewState(
                                "Time to get to work",
                                0,
                                0,
                                false,
                                TimerState.INITIAL)
                        }
                        is StoreResponse.Error ->
                            ViewState(
                                "There was an error",
                                0,
                                0,
                                false,
                                TimerState.INITIAL)
                    }
                    view?.setState(state)
                }
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.timer)
    }

    fun startNextTask() {
        if (remainingTasks.isEmpty()) {
            view?.setState(
                ViewState(
                    "It's done",
                    0L,
                    0L,
                    false,
                    TimerState.FINISHED
                )
            )
            return
        }

        var nextTask = remainingTasks[0]
        currentTaskDurationSecs = 0L
        taskTimerDisposable?.dispose()
        startTimer(nextTask.lengthMins, nextTask.name)
    }

    private fun startTimer(lengthMins: Int, taskName: String) {
        safeLaunch {
            timer.ofFlow(lengthMins)
                .collect {
                    view?.setState(
                        ViewState(
                            taskName,
                            abs(it) / 60,
                            abs(it) % 60,
                            it < 0,
                            TimerState.RUNNING
                        )
                    )
                }
        }
        toaster.showToast("Starting task for $lengthMins mins")
    }

    fun completeTask() {
        val completedTask = remainingTasks.removeAt(0)
        tasksRepository.taskCompleted(
            TasksRepository.TaskUpdateInfo(
                completedTask.id,
                null,
                completedTask.service,
                currentTaskDurationSecs,
                updateType = "complete"
            )
        ).subscribe()
        startNextTask()
    }

    fun deferTask() {
        val defferedTask = remainingTasks.removeAt(0)
        tasksRepository.deferTask(
            TasksRepository.TaskUpdateInfo(
                defferedTask.id,
                null,
                defferedTask.service,
                currentTaskDurationSecs,
                updateType = "defer"
            )
        ).subscribe()
        startNextTask()
    }

    data class ViewState(
        val taskName: String,
        val minsRemaining: Long,
        val secsRemaining: Long,
        val isLate: Boolean,
        val timerState: TimerState
    )

    enum class TimerState {
        LOADING,
        INITIAL,
        RUNNING,
        FINISHED
    }
}
