package com.cronus.zdone.home

import com.cronus.zdone.api.model.Task
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject
import javax.inject.Singleton

interface UserSelectedTasksRepository {

    val selectedTasks: Flow<List<Task>>

    fun userSelectedTask(task: Task)

    fun removeTask(task: Task)

}

@Singleton
class RealUserSelectedTasksRepository @Inject constructor() : UserSelectedTasksRepository {

    private val _userSelectedTasks = mutableListOf<Task>()
    private val _dataPublisher = BehaviorSubject.createDefault(_userSelectedTasks.toList())
    override val selectedTasks =
        _dataPublisher.hide()
            .toFlowable(BackpressureStrategy.BUFFER)
            .asFlow()

    override fun userSelectedTask(task: Task) {
        if (_userSelectedTasks.contains(task))
            _userSelectedTasks.remove(task)
        else
            _userSelectedTasks.add(task)
        _dataPublisher.onNext(_userSelectedTasks.toList())
    }

    override fun removeTask(task: Task) {
        _userSelectedTasks.remove(task)
        _dataPublisher.onNext(_userSelectedTasks.toList())
    }

    fun clearTasks() {
        _userSelectedTasks.clear()
        _dataPublisher.onNext(_userSelectedTasks.toList())
    }
}
