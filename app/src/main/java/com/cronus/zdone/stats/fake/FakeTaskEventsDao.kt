package com.cronus.zdone.stats.fake

import com.cronus.zdone.stats.TaskEvent
import com.cronus.zdone.stats.TaskEventsDao
import com.cronus.zdone.stats.TaskUpdateType
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.joda.time.LocalDate
import kotlin.math.roundToLong
import kotlin.random.Random

class FakeTaskEventsDao : TaskEventsDao {

    private val fakeTaskEventsGenerator = FakeTaskEventsGenerator()
    private val _taskEventsSubject = BehaviorSubject.createDefault(fakeTaskEventsGenerator.taskEvents)
    private val tasksSinceCache = mutableMapOf<Long, Flowable<List<TaskEvent>>>()


    override fun getTaskEventsSince(timestamp: Long): Flow<List<TaskEvent>> {
        val flowable = tasksSinceCache.computeIfAbsent(timestamp) {
            _taskEventsSubject
                .map { it.filter { event -> event.completedAtMillis > timestamp } }
                .toFlowable(BackpressureStrategy.BUFFER)
        }
        return flowable
            .asFlow()
    }

    override fun getTaskEvents(): Flow<List<TaskEvent>> =
        _taskEventsSubject
            .hide()
            .toFlowable(BackpressureStrategy.ERROR)
            .asFlow()

    override fun addTaskEvent(taskEvent: TaskEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEvent(taskEvent: TaskEvent) {
        fakeTaskEventsGenerator.taskEvents
            .mapIndexed { index, event ->
                if (taskEvent.id == event.id) {
                    fakeTaskEventsGenerator.taskEvents[index] = taskEvent
                }
            }
        _taskEventsSubject.onNext(fakeTaskEventsGenerator.taskEvents)
    }

    override fun deleteEvent(taskEvent: TaskEvent) {
        fakeTaskEventsGenerator.taskEvents.remove(taskEvent)
        _taskEventsSubject.onNext(fakeTaskEventsGenerator.taskEvents)
    }
}

internal class FakeTaskEventsGenerator() {
    private val nouns = listOf(
        "The beach",
        "SFMOMA",
        "your room",
        "a spindrift",
        "a coffee",
        "the new york times",
        "the garbage",
        "a spotify playlist",
        "the curtains",
        "a painting",
        "the new speakers",
        "a new pair of common projects",
        "those heavy-ass weights",
        "your kindle fire tablet",
        "a towel",
        "this sweet macbook pro",
        "that dirty carpet",
        "my new bike"
    )

    private val verbs = listOf(
        "Run to",
        "Read",
        "Eat",
        "Play",
        "Paint",
        "Write",
        "Jump for",
        "Clean",
        "Hide",
        "Store",
        "Buy",
        "Call",
        "E-mail"
    )

    private val taskNames = verbs.flatMap { verb ->
        nouns.map { noun -> verb + " " + noun } }
        .shuffled()

    private val durationSecs = List(taskNames.size) { Random.nextInt(30, 3000) }

    private var previousCompletedAtDateTime = LocalDate.now().toDateTimeAtStartOfDay().plusDays(1).minusHours(4)

    private val completedAtMillis = List(taskNames.size) { idx ->
        val result = previousCompletedAtDateTime.millis
        previousCompletedAtDateTime = previousCompletedAtDateTime.minusSeconds(durationSecs[idx])
        previousCompletedAtDateTime = previousCompletedAtDateTime.minusHours(Random.nextInt(1, 20))
        result
    }.sortedBy { it }

    val taskEvents = MutableList(taskNames.size) { idx ->
        TaskEvent(
            id = idx.toLong(),
            taskID =  idx.toString(),
            taskName = taskNames[idx],
            taskResult = if (Random.nextInt(1, 100) > 33) TaskUpdateType.COMPLETED else TaskUpdateType.DEFERRED,
            expectedDurationSecs = (durationSecs[idx] * Random.nextDouble(.75, 1.25)).roundToLong(),
            durationSecs = durationSecs[idx].toLong(),
            completedAtMillis = completedAtMillis[idx]
        )
    }

}