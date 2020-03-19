package com.cronus.zdone.timer

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cronus.zdone.MainActivity
import com.cronus.zdone.R
import com.cronus.zdone.ZdoneApplication
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.home.TasksScreen
import com.cronus.zdone.timer.TimerState.*
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskTimerManager @Inject constructor(private val context: Context, private val taskTimerFactory: TaskTimerFactory) {

    val workManager = WorkManager.getInstance(context)
    internal var taskTimerWorker: OneTimeWorkRequest? = null
    var timer: Flow<Long> = emptyFlow()
    var _taskTimerState = BehaviorSubject.createDefault(Stopped())
    var taskTimerState: Flow<TimerState> = _taskTimerState.hide().toFlowable(BackpressureStrategy.BUFFER).asFlow()

    fun cancelTimer() {
        taskTimerWorker?.let {
            workManager.cancelWorkById(it.id)
            taskTimerWorker = null
        }
        context.stopService(Intent(context, TaskTimerForegroundService::class.java))
    }

    fun startTimer(task: TasksScreen.DisplayedTask) {
        CoroutineScope(Dispatchers.Main).launch {
            timer = taskTimerFactory.ofFlow(task.lengthMins)
        }
        val intent = Intent(context, TaskTimerForegroundService::class.java)
        intent.addTask(task)
        context.startService(intent)
        val taskTimerWorkData = workDataOf(
                TaskTimerWorker.TASK_NAME_KEY to task.name
        )
        taskTimerWorker = OneTimeWorkRequestBuilder<TaskTimerWorker>()
                .setInputData(taskTimerWorkData)
                .setInitialDelay(task.lengthMins.toLong(), TimeUnit.MINUTES)
                .build()
        workManager.enqueue(taskTimerWorker!!)
    }

}

private fun Intent.addTask(task: TasksScreen.DisplayedTask) {
    putExtra("TASK_ID", task.id)
    putExtra("TASK_NAME", task.name)
    putExtra("TASK_SERVICE", task.service)
    putExtra("TASK_SUBTASK_ID", task.subtaskId)
}

class TaskTimerForegroundService : Service() {

    private val NOTIFICATION_ID = 142579

    @Inject
    lateinit var taskTimerManager: TaskTimerManager
    private lateinit var timerJob: Job
    private lateinit var taskId: String
    private lateinit var taskName: String
    private lateinit var taskService: String
    private var taskSubtaskId: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val result =  super.onStartCommand(intent, flags, startId)
        intent.extractTaskDetails()
        (application as ZdoneApplication).component.inject(this)
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            taskTimerManager.timer?.collect { secondsRemaining ->
                updateNotification(secondsRemaining)
            }
        }

        return result
    }

    private fun Intent.extractTaskDetails() {
        taskId = getStringExtra("TASK_ID")!!
        taskName = getStringExtra("TASK_NAME")!!
        taskService = getStringExtra("TASK_SERVICE")!!
        taskSubtaskId = getStringExtra("TASK_SUBTASK_ID")
    }

    private fun updateNotification(timeRemainingSecs: Long) {
        val updateIntent = Intent(applicationContext, UpdateTaskService::class.java)
        updateIntent.putExtra("TASK_ID", taskId)
        updateIntent.putExtra("TASK_NAME", taskName)
        updateIntent.putExtra("TASK_SERVICE", taskService)
        updateIntent.putExtra("TASK_SUBTASK_ID", taskSubtaskId)
        val builder =
            NotificationCompat.Builder(applicationContext, ZdoneApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_zdone_notification_logo_2)
                .setContentTitle(getTitleForRemainingTime(timeRemainingSecs))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(0, "Completed", PendingIntent.getService(
                    applicationContext,
                    UpdateTaskService.RequestCodes.COMPLETED.code,
                    updateIntent,
                    0))
                .addAction(0, "Defer", PendingIntent.getService(
                    applicationContext,
                    UpdateTaskService.RequestCodes.DEFERRED.code,
                    updateIntent,
                    0))
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        MainActivity.getLaunchIntent(applicationContext),
                        0
                    )
                )
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, builder.build())
    }

    private fun getTitleForRemainingTime(timeRemainingSecs: Long): CharSequence? =
        if (timeRemainingSecs > 0) "Time remaining for ${abbreviatedTaskName(taskName)}: ${formattedTimeString(timeRemainingSecs)}" else "Time's up for ${abbreviatedTaskName(taskName)}! Complete or defer now!"

    private fun abbreviatedTaskName(taskName: String): String {
        return if (taskName.length < 25) taskName else taskName.substring(0, 26) + "..."
    }

    private fun formattedTimeString(timeRemainingSecs: Long): String {
        return if (timeRemainingSecs > 0) "${timeRemainingSecs / 60}:${String.format("%02d", timeRemainingSecs % 60)}" else "Time's Up!"
    }

    override fun onDestroy() {
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_ID)
        timerJob.cancel()
        super.onDestroy()
    }

}

class UpdateTaskService : Service(){

    enum class RequestCodes(val code: Int) {
        COMPLETED(4224),
        DEFERRED(4225)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
