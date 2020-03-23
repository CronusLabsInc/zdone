package com.cronus.zdone.api

import com.cronus.zdone.api.TasksRepository.TaskUpdateInfo
import com.cronus.zdone.api.model.AddTaskInfo
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.api.model.Tasks
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.stats.TaskEvent
import com.cronus.zdone.stats.TaskEventsDao
import com.cronus.zdone.stats.TaskUpdateType
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealTasksRepository @Inject constructor(
    private val zdoneService: ZdoneService,
    private val taskEventsDao: TaskEventsDao
) : TasksRepository {

    private val taskInfoStore: Store<Unit, Tasks>
    private val taskUpdateStore: Store<TaskUpdateInfo, UpdateDataResponse>
    private val taskAddStore: Store<AddTaskInfo, UpdateDataResponse>
    private var lastRequestTime: Long = 0

    init {
        taskInfoStore = StoreBuilder
            .fromNonFlow<Unit, Tasks> { zdoneService.getTaskInfoFlow() }
            .build()
        taskUpdateStore = StoreBuilder.fromNonFlow<TaskUpdateInfo, UpdateDataResponse> { taskUpdateInfo ->
            zdoneService.updateTaskAsync(
                TaskStatusUpdate(
                    id = taskUpdateInfo.id,
                    subtaskId = taskUpdateInfo.subtaskId,
                    update = taskUpdateInfo.updateType.toApiUpdateType(),
                    service = taskUpdateInfo.service,
                    duration_seconds = taskUpdateInfo.actualDurationSeconds)) }
            .build()
        taskAddStore = StoreBuilder.fromNonFlow<AddTaskInfo, UpdateDataResponse> { addTaskInfo ->
            zdoneService.addTaskAsync(addTaskInfo) }
            .disableCache()
            .build()
    }

    override suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>> =
        getCachedData().mapToSubField(Tasks::tasksToDo)

    override suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>> =
        getCachedData().mapToSubField(Tasks::timeProgress)

    private suspend fun getCachedData(): Flow<StoreResponse<Tasks>> = coroutineScope {
        lastRequestTime = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            taskInfoStore.stream(StoreRequest.cached(Unit, refresh = false))
        }
    }

    private fun <T> Flow<StoreResponse<Tasks>>.mapToSubField(fieldGetter: Tasks.() -> T): Flow<StoreResponse<T>> {
        return map { extractFieldFromTasks(it, fieldGetter) }
    }


    private fun <T> extractFieldFromTasks(
        response: StoreResponse<Tasks>,
        extractField: Tasks.() -> T
    ): StoreResponse<T> {
        return when (response) {
            is StoreResponse.Loading -> StoreResponse.Loading(response.origin)
            is StoreResponse.Data -> StoreResponse.Data(
                response.value.extractField(),
                response.origin
            )
            is StoreResponse.Error -> StoreResponse.Error(response.error, response.origin)
        }
    }

    override suspend fun updateTask(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> =
        withContext(Dispatchers.IO) {
            launch {
                insertTaskEventIntoDao(taskUpdateInfo)
            }
            sendTaskUpdateToApi(taskUpdateInfo)
        }

    override suspend fun addTask(addTaskInfo: AddTaskInfo) =
        withContext(Dispatchers.IO) {
            taskAddStore.stream(StoreRequest.fresh(addTaskInfo))
        }

    private fun sendTaskUpdateToApi(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> {
        return taskUpdateStore.stream(StoreRequest.fresh(taskUpdateInfo))
            .map {
                it.dataOrNull()?.let { _ ->
                    // if we are finishing a task
                    refreshTaskDataFromStore()
                }
                it
            }
    }

    private fun insertTaskEventIntoDao(taskUpdateInfo: TaskUpdateInfo) {
        taskUpdateInfo.actualDurationSeconds?.let { taskDurationSecs ->
            taskEventsDao.addTaskEvent(
                TaskEvent(
                    taskID = taskUpdateInfo.id,
                    taskName = taskUpdateInfo.name,
                    taskResult = taskUpdateInfo.updateType,
                    expectedDurationSecs = taskUpdateInfo.expectedDurationSeconds,
                    durationSecs = taskDurationSecs,
                    completedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun refreshTaskDataFromStore() {
        taskInfoStore.fresh(Unit)
    }

    override suspend fun flushCacheFromStore() {
        taskInfoStore.clearAll()
    }

    override suspend fun getTaskById(taskId: String): Task {
        return taskInfoStore.get(Unit)
            .tasksToDo
            .filter { it.id == taskId }
            .first()
    }

    override fun areTasksFromPreviousDay(): Boolean {
        return DateTime(
            lastRequestTime,
            DateTimeZone.getDefault()
        ).dayOfYear().get() < DateTime.now().dayOfYear().get()
    }

}

private fun TaskUpdateType.toApiUpdateType() = when (this) {
    TaskUpdateType.COMPLETED -> "complete"
    TaskUpdateType.DEFERRED -> "defer"
}
