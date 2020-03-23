package com.cronus.zdone.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cronus.zdone.Toaster
import com.cronus.zdone.ZdoneApplication
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.stats.TaskUpdateType
import com.cronus.zdone.timer.TaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class UpdateTaskService : Service() {

    companion object {
        val UPDATE_TYPE_INTENT_KEY = "UPDATE_TYPE"
    }

    enum class RequestCodes(val code: Int, val updateType: TaskUpdateType) {
        COMPLETED(4224, TaskUpdateType.COMPLETED),
        DEFERRED(4225, TaskUpdateType.DEFERRED)
    }

    @Inject
    lateinit var toaster: Toaster
    @Inject
    lateinit var tasksRepository: TasksRepository
    @Inject
    lateinit var taskExecutionManager: TaskExecutionManager

    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result =  super.onStartCommand(intent, flags, startId)
        (application as ZdoneApplication).component.inject(this)
        intent?.let {
            val updateType =
                RequestCodes.valueOf(intent.getStringExtra(UPDATE_TYPE_INTENT_KEY)).updateType
            updateCurrentTask(updateType)
        } ?: toaster.showToast("Received null intent")
        return result
    }

    private fun updateCurrentTask(updateType: TaskUpdateType) {
        if (job == null) {
            job = CoroutineScope(Dispatchers.Main).launch {
                launch { taskExecutionManager.startNextTask() }
                launch { updateTaskOnServer(updateType) }
            }
        }
    }

    private suspend fun updateTaskOnServer(updateType: TaskUpdateType) {
        val (task, secsRemaining) = taskExecutionManager.currentTaskExecutionData
            .filterIsInstance<TaskExecutionState.TaskRunning>()
            .first()
        tasksRepository.updateTask(TasksRepository.TaskUpdateInfo(
            id = task.id,
            name = task.name,
            subtaskId = null,
            service = task.service,
            expectedDurationSeconds = task.lengthMins * 60L,
            actualDurationSeconds = task.lengthMins * 60 - secsRemaining,
            updateType = updateType
        )).collect {
            when (it) {
                is StoreResponse.Loading -> toaster.showToast("update ${task.name}: $updateType")
                is StoreResponse.Data -> {
                    var message = ""
                    if (it.value.result == "success") {
                        message = "Updated ${task.name} to: $updateType"
                    } else {
                        message = "Failed to update ${task.name}, please try again later"
                    }
                    job = null
                }
                is StoreResponse.Error -> {
                    toaster.showToast("Failed to update ${task.name}, please try again later")
                    job = null
                }
            }
        }
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

}