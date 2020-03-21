package com.cronus.zdone.api

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.api.TasksRepository.*
import com.cronus.zdone.api.model.*
import com.cronus.zdone.stats.DailyStatsScreen
import com.cronus.zdone.stats.TaskEvent
import com.cronus.zdone.stats.TaskEventsDao
import com.cronus.zdone.stats.TaskUpdateType
import com.dropbox.android.external.store4.*
import io.reactivex.Observable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealTasksRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val zdoneService: ZdoneService,
    private val taskEventsDao: TaskEventsDao
) : TasksRepository {

    private val taskInfoStore: Store<Unit, Tasks>
    private val taskUpdateStore: Store<TaskUpdateInfo, UpdateDataResponse>
    private var lastRequestTime: Long = 0

    init {
        taskInfoStore = StoreBuilder
            .fromNonFlow<Unit, Tasks> { zdoneService.getTaskInfoFlow() }
            .build()
        taskUpdateStore = StoreBuilder.fromNonFlow<TaskUpdateInfo, UpdateDataResponse>
        { taskUpdateInfo ->
            zdoneService.updateTaskAsync(
                TaskStatusUpdate(
                    id = taskUpdateInfo.id,
                    subtaskId = taskUpdateInfo.subtaskId,
                    update = taskUpdateInfo.updateType.toApiUpdateType(),
                    service = taskUpdateInfo.service,
                    duration_seconds = taskUpdateInfo.actualDurationSeconds
                )
            )
        }
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

    override fun areTasksFromPreviousDay(): Boolean {
        return DateTime(
            lastRequestTime,
            DateTimeZone.getDefault()
        ).dayOfYear().get() < DateTime.now().dayOfYear().get()
    }

    private fun <T> observe(observable: Observable<T>): Observable<T> {
        return observable
            .subscribeOn(appExecutors.network())
            .observeOn(appExecutors.mainThread())
    }

}

private fun TaskUpdateType.toApiUpdateType() = when (this) {
    TaskUpdateType.COMPLETED -> "complete"
    TaskUpdateType.DEFERRED -> "defer"
}
