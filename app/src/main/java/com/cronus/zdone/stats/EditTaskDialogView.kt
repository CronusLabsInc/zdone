package com.cronus.zdone.stats

import android.app.Activity
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.view.View
import android.widget.FrameLayout
import com.cronus.zdone.R
import kotlinx.android.synthetic.main.edit_task_dialog.view.*
import kotlinx.android.synthetic.main.task_event_item.view.*
import kotlinx.android.synthetic.main.task_event_item.view.task_duration_mins
import kotlinx.android.synthetic.main.task_event_item.view.task_end_time
import kotlinx.android.synthetic.main.task_item.view.*
import kotlinx.android.synthetic.main.task_item.view.task_name
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat

class EditTaskDialogView(context: Context, private val taskEvent: TaskEvent, private val listener: EditActionListener): FrameLayout(context) {

    init {
        inflate(context, R.layout.edit_task_dialog, this)
        task_name.text = taskEvent.taskName
        task_duration_mins.text =
            DecimalFormat("#.#")
                .format(taskEvent.durationSecs.toDouble() / 60)
        task_end_time.text = taskEvent.getEndTime()
        save_task_event.setOnClickListener {
            listener.saveEdit(
                newName = task_name.text.toString(),
                newDurationMinsString = task_duration_mins.text.toString(),
                newEndTimeString = task_end_time.text.toString(),
                oldTaskEvent = taskEvent
            ) }
        cancel_edit_task_event.setOnClickListener {
            listener.cancelEdit()
        }
        delete_task_event.setOnClickListener {
            listener.deleteTask(taskEvent)
        }
    }
}

interface EditActionListener {

    fun cancelEdit()

    fun saveEdit(
        newName: String,
        newDurationMinsString: String,
        newEndTimeString: String,
        oldTaskEvent: TaskEvent)

    fun deleteTask(taskEvent: TaskEvent)

}

private fun TaskEvent.getEndTime(): String = getFormattedTimeString(completedAtMillis)

private fun getFormattedTimeString(startedAtMillis: Long): String {
    val localDateTime = LocalDateTime(startedAtMillis)
    val timeFormatter = DateTimeFormat.forPattern("HH:mm")
    return timeFormatter.print(localDateTime)
}
