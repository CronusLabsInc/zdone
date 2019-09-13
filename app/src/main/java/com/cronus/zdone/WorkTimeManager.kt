package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkTimeManager @Inject constructor(val tasksRepository: TasksRepository) {

    val currentWorkTime: Observable<Int>

    init {
        currentWorkTime = tasksRepository.getTimeData().map {
            it.maximumMinutesPerDay
        }
    }

    fun setMaxWorkMins(maxWorkMins: Int) {
        tasksRepository.updateWorkTime(maxWorkMins)
    }


}
