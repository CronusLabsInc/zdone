package com.cronus.zdone

import androidx.lifecycle.Transformations.map
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.ZdoneService
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.danlew.android.joda.JodaTimeAndroid.init
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WorkTimeManager @Inject constructor(
    tasksRepository: TasksRepository,
    val zdoneService: ZdoneService
) {

    val _currentWorkTime = PublishSubject.create<Int>()
    val currentWorkTime = _currentWorkTime.hide()
    private val workTimeStore = StoreBuilder.fromNonFlow<Int, UpdateDataResponse> { newWorkMins ->
        zdoneService.updateWorkTimeAsync(mapOf("maximum_minutes_per_day" to newWorkMins))
    }
        .disableCache()
        .build()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            tasksRepository.getTimeDataFromStore().filter { it is StoreResponse.Data }
                .map { (it as StoreResponse.Data).value }
                .collect { _currentWorkTime.onNext(it.maximumMinutesPerDay) }
        }
    }

    suspend fun setMaxWorkMins(maxWorkMins: Int): Flow<StoreResponse<UpdateDataResponse>> =
        coroutineScope {
            workTimeStore.stream(StoreRequest.fresh(maxWorkMins))
        }
}
