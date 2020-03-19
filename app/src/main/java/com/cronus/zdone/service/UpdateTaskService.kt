package com.cronus.zdone.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cronus.zdone.Toaster
import com.cronus.zdone.ZdoneApplication
import com.cronus.zdone.api.TasksRepository
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

    enum class RequestCodes(val code: Int, val updateType: String) {
        COMPLETED(4224, "complete"),
        DEFERRED(4225, "defer")
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

    private fun updateCurrentTask(updateType: String) {
        if (job == null) {
            job = CoroutineScope(Dispatchers.Main).launch {
                val (task, secsRemaining) = taskExecutionManager.currentTaskExecutionData
                    .filterIsInstance<TaskExecutionState.TaskRunning>()
                    .first()
                tasksRepository.updateTask(TasksRepository.TaskUpdateInfo(
                    task.id,
                    null,
                    task.service,
                    task.lengthMins * 60 - secsRemaining,
                    updateType
                )).collect {
                    when (it) {
                        is StoreResponse.Loading -> toaster.showToast("update ${task.name}: $updateType")
                        is StoreResponse.Data -> {
                            var message = ""
                            if (it.value.result == "success") {
                                message = "Updated ${task.name} to: $updateType"
                                taskExecutionManager.startNextTask()
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
        }
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

}