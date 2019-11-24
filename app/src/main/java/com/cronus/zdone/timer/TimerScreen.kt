package com.cronus.zdone.timer

import android.content.Context
import android.widget.Toast
import com.cronus.zdone.AppExecutors
import com.cronus.zdone.R
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.wealthfront.magellan.rx2.RxScreen
import io.reactivex.disposables.Disposable
import java.lang.Math.abs
import javax.inject.Inject

class TimerScreen @Inject constructor(
    val tasksRepository: TasksRepository,
    val timer: TaskTimer,
    val appExecutors: AppExecutors
) :
    RxScreen<TimerView>() {

    var currentTaskDurationSecs = 0L
    var taskTimerDisposable: Disposable? = null
    lateinit var remainingTasks: MutableList<Task>

    override fun createView(context: Context): TimerView =
        TimerView(context)

    override fun onSubscribe(context: Context?) {
        autoDispose(tasksRepository.getTasks()
            .take(1)
            .subscribe {
                remainingTasks = it.toMutableList()
                view?.setState(
                    ViewState(
                        "Time to get to work",
                        0,
                        0,
                        false,
                        TimerState.INITIAL
                    )
                )
            })
        view?.setState(
            ViewState(
                "Time to get to work",
                0,
                0,
                false,
                TimerState.LOADING
            )
        )
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
        taskTimerDisposable = timer.of(lengthMins)
            .subscribeOn(appExecutors.io())
            .observeOn(appExecutors.mainThread())
            .map {
                currentTaskDurationSecs++
                it
            }
            .subscribe {
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
        autoDispose(taskTimerDisposable)

        Toast.makeText(
            activity,
            "Starting task for $lengthMins mins",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun completeTask() {
        val completedTask = remainingTasks.removeAt(0)
        tasksRepository.taskCompleted(
            TasksRepository.TaskUpdateInfo(
                completedTask.id,
                null,
                completedTask.service,
                currentTaskDurationSecs
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
                currentTaskDurationSecs
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
