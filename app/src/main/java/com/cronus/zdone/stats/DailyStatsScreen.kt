package com.cronus.zdone.stats

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.cronus.zdone.CoroutineScreen
import com.cronus.zdone.Toaster
import com.cronus.zdone.util.Do
import com.dropbox.android.external.store4.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import javax.inject.Inject

class DailyStatsScreen @Inject constructor(
    private val taskEventsDao: TaskEventsDao,
    private val toaster: Toaster
) : CoroutineScreen<DailyStatsView>() {

    private val eventStore = StoreBuilder.from<Long, List<TaskEvent>> { millis ->
        taskEventsDao.getTaskEventsSince(millis)
    }
        .disableCache()
        .build()

    override fun getTitle(context: Context?) = "stats"

    override fun createView(context: Context): DailyStatsView = DailyStatsView(context)

    override fun onShow(context: Context?) {
            safeLaunch {
                val todayMillis = LocalDate.now().toDateTimeAtStartOfDay().millis
                eventStore.stream(StoreRequest.cached(todayMillis, false))
                    .collect {
                        Do exhaustive when (it) {
                            is StoreResponse.Loading -> {
                                view?.setState(ViewState.Loading)
                            }
                            is StoreResponse.Data -> {
                                view?.setState(ViewState.Data(it.value))
                            }
                            is StoreResponse.Error -> {
                                view?.setState(ViewState.Error(it.error.message))
                            }
                        }

                    }
            }
    }

    fun editItem(taskEvent: TaskEvent) {
        showDialog {
            val builder = AlertDialog.Builder(it)
            val dialogView = EditTaskDialogView(activity, taskEvent,
                object : EditActionListener {
                    override fun cancelEdit() {
                        dialog.cancel()
                    }

                    override fun saveEdit(
                        newName: String,
                        newDurationMinsString: String,
                        newEndTimeString: String,
                        oldTaskEvent: TaskEvent
                    ) {
                        saveChanges(
                            TaskEventUpdate(
                                newName,
                                newDurationMinsString,
                                newEndTimeString,
                                oldTaskEvent))
                        dialog.cancel()
                    }

                    override fun deleteTask(taskEvent: TaskEvent) {
                        deleteTask(taskEvent)
                        dialog.cancel()
                    }

                })
            builder.setView(dialogView)
            builder.create()
        }
    }

    fun saveChanges(update: TaskEventUpdate) {
        if (update.valid()) {
            safeLaunch {
                withContext(Dispatchers.IO) {
                    val updatedTaskEvent = update.getNewTaskEvent()
                    taskEventsDao.updateEvent(updatedTaskEvent)
                }
            }
        } else {
            when {
                update.newName.isEmpty() -> toaster.showToast("Please enter a task name")
                !update.isValidNewDuration() -> toaster.showToast("Please enter a valid duration, like \"1.1\"")
                !update.isValidNewEndTime() -> toaster.showToast("Please enter a valid end time, like 08:30")
            }
        }
    }

    private fun deleteTask(taskEvent: TaskEvent) {
        safeLaunch {
            withContext(Dispatchers.IO) {
                taskEventsDao.deleteEvent(taskEvent)
            }
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Data(val events: List<TaskEvent>) : ViewState()
        data class Error(val message: String?) : ViewState()
    }

    data class TaskEventUpdate(
        val newName: String,
        val newDurationMins: String,
        val newEndTime: String,
        val currentItem: TaskEvent
    )

}

private fun DailyStatsScreen.TaskEventUpdate.getNewTaskEvent(): TaskEvent {
    val newDurationSecs = parseDuration(newDurationMins)
    val newCompletedAtMillis = parseCompletedAt(newEndTime)
    return TaskEvent(
        id = currentItem.id,
        taskID = currentItem.taskID,
        taskName = newName,
        taskResult = currentItem.taskResult,
        expectedDurationSecs = currentItem.expectedDurationSecs,
        durationSecs = newDurationSecs,
        completedAtMillis = newCompletedAtMillis
    )
}

private fun parseCompletedAt(str: String): Long {
    // srt will look like "XX:XX"
    val timeParts = str.split(":")
    return LocalDate.now().toDateTimeAtStartOfDay().plusHours(timeParts[0].toInt())
        .plusMinutes(timeParts[1].toInt())
        .millis
}

private fun parseDuration(str: String): Long {
    // str will look something like "1" or "2.5", at most 1 number after decimal
    var result = 0L
    var multiple = 60
    for (part in str.split(".")) {
        result += (part.toInt() * multiple)
        multiple /= 10
    }
    return result
}

private fun DailyStatsScreen.TaskEventUpdate.valid(): Boolean {
    return newName.isNotEmpty() &&
            isValidNewDuration() &&
            isValidNewEndTime()
}

private fun DailyStatsScreen.TaskEventUpdate.isValidNewEndTime() =
    newEndTime.matches(Regex("\\d\\d:\\d\\d"))

private fun DailyStatsScreen.TaskEventUpdate.isValidNewDuration() =
    newDurationMins.matches(Regex("^\\d+(\\.\\d)?$"))
