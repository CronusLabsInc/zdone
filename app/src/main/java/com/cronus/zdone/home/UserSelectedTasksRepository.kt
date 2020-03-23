package com.cronus.zdone.home

import com.cronus.zdone.api.model.Task
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject
import javax.inject.Singleton

interface UserSelectedTasksRepository {

    val selectedTasks: Flow<List<String>>

    fun userSelectedTask(taskId: String)

}

@Singleton
class RealUserSelectedTasksRepository @Inject constructor() : UserSelectedTasksRepository {

    private val _userSelectedTasks = mutableListOf<String>()
    private val _dataPublisher = BehaviorSubject.createDefault(_userSelectedTasks.toList())
    override val selectedTasks =
        _dataPublisher.hide()
            .toFlowable(BackpressureStrategy.BUFFER)
            .asFlow()

    override fun userSelectedTask(taskId: String) {
        if (_userSelectedTasks.contains(taskId))
            _userSelectedTasks.remove(taskId)
        else
            _userSelectedTasks.add(taskId)
        _dataPublisher.onNext(_userSelectedTasks.toList())
    }

}
