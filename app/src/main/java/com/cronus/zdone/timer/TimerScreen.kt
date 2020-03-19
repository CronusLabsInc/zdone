package com.cronus.zdone.timer

import android.content.Context
import com.cronus.zdone.CoroutineScreen
import com.cronus.zdone.R
import com.cronus.zdone.Toaster
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.UpdateDataResponse
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.flow.*
import java.lang.Math.abs
import javax.inject.Inject

class TimerScreen @Inject constructor(
    val tasksRepository: TasksRepository,
    val toaster: Toaster,
    val taskExecutionManager: TaskExecutionManager
) : CoroutineScreen<TimerView>() {

    override fun createView(context: Context): TimerView =
        TimerView(context)

    override fun onShow(context: Context?) {
        safeLaunch {
            taskExecutionManager.currentTaskExecutionData
                .collect {
                    val viewState = when (it) {
                        is TaskExecutionState.WaitingForTasks -> ViewState(
                            "Time to get to work",
                            0,
                            0,
                            false,
                            TimerState.INITIAL
                        )
                        is TaskExecutionState.AllTasksCompleted -> ViewState(
                            "Great work, stud. Let's get the next one going.",
                            0,
                            0,
                            false,
                            TimerState.INITIAL
                        )
                        is TaskExecutionState.TaskRunning -> {
                            ViewState(
                                it.task.name,
                                abs(it.secsRemaining) / 60,
                                abs(it.secsRemaining) % 60,
                                it.secsRemaining < 0,
                                TimerState.RUNNING
                            )
                        }
                    }
                    view?.setState(viewState)
                }
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.timer)
    }

    fun startTasks() {
        toaster.showToast("Starting standard tasks")
        safeLaunch {
            val tasks = tasksRepository.getTasksFromStore()
                .map { it.dataOrNull() }
                .filterNotNull()
                .first()
            taskExecutionManager.startTasks(tasks)
        }
    }

    fun completeTask() {
        udpateCurrentTask("complete")
        safeLaunch {
            taskExecutionManager.startNextTask()
        }
    }

    fun deferTask() {
        udpateCurrentTask("defer")
        safeLaunch {
            taskExecutionManager.startNextTask()
        }
    }

    private fun udpateCurrentTask(updateType: String) {
        safeLaunch {
            val (currentTask, timeRemaining) = taskExecutionManager.currentTaskExecutionData.first() as TaskExecutionState.TaskRunning
            tasksRepository.updateTask(
                TasksRepository.TaskUpdateInfo(
                    currentTask.id,
                    null,
                    currentTask.service,
                    currentTask.lengthMins * 60 - timeRemaining,
                    updateType = updateType
                )
            )
                .collect { handleUpdateTaskResponse(it) }
        }
    }

    private fun handleUpdateTaskResponse(it: StoreResponse<UpdateDataResponse>) {
        when (it) {
            is StoreResponse.Loading -> toaster.showToast("Sending completed task message to server")
            is StoreResponse.Data -> toaster.showToast("Result of completing task on server: ${it.value.result}")
            is StoreResponse.Error -> toaster.showToast("Error when completing task, task will be back in your list")
        }
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
