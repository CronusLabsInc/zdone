package com.cronus.zdone.stats.log

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.cronus.zdone.R
import com.cronus.zdone.stats.TaskEvent
import com.cronus.zdone.stats.TaskUpdateType
import com.cronus.zdone.util.Do
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.daily_stats.view.*
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat

class DailyStatsLogView(context: Context) : BaseScreenView<DailyStatsLogScreen>(context) {

    private val taskEventsListAdapter: TaskEventsListAdapter

    init {
        inflate(context, R.layout.daily_stats, this)
        taskEventsListAdapter = TaskEventsListAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        task_events.layoutManager = linearLayoutManager
        task_events.adapter = taskEventsListAdapter
        task_events.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
        taskEventsListAdapter.editTaskListener = {
            screen.editItem(it)
        }
    }

    fun setState(state: DailyStatsLogScreen.ViewState) {
        clearState()
        Do exhaustive when (state) {
            DailyStatsLogScreen.ViewState.Loading -> showLoading()
            is DailyStatsLogScreen.ViewState.Data -> {
                Log.d("ZDONE_EVENT_ITEMS", state.events.joinToString("\n"))
                showTaskEvents(state.events)
            }
            is DailyStatsLogScreen.ViewState.Error -> showError(state.message)
        }
    }

    private fun showLoading() {
        stats_loading.visibility = View.VISIBLE
    }

    private fun showTaskEvents(events: List<TaskEvent>) {
        task_events.visibility = View.VISIBLE
        task_event_item_header.visibility = View.VISIBLE
        taskEventsListAdapter.events = events.toList()
    }

    private fun showError(message: String?) {

    }

    private fun clearState() {
        stats_loading.visibility = GONE
        task_events.visibility = GONE
        task_event_item_header.visibility = View.GONE
    }


}

private class TaskEventsListAdapter :
    ListAdapter<TaskEvent, TaskEventsListAdapter.TaskEventViewHolder>(
        TaskEventDiffer()
    ) {
    var editTaskListener: ((TaskEvent) -> Unit)? = null
    var events: List<TaskEvent> = emptyList()
        set(value) {
            field = value
            submitList(field)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskEventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.task_event_item, parent, false)
        return TaskEventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskEventViewHolder, position: Int) {
        holder.bind(events[position])
        holder.editTaskListener = { editTaskListener?.invoke(it) }
    }

    private class TaskEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var editTaskListener: ((TaskEvent) -> Unit)? = null

        private var taskDuration: TextView
        private var taskEndTime: TextView
        private var taskStartTime: TextView
        private val taskName: TextView
        private val editTaskEvent: Button?
        private var taskEventItem: TaskEvent? = null

        init {
            taskName = view.findViewById(R.id.task_name)
            taskStartTime = view.findViewById(R.id.task_start_time)
            taskEndTime = view.findViewById(R.id.task_end_time)
            taskDuration = view.findViewById(R.id.task_duration_mins)

            editTaskEvent = view.findViewById(R.id.edit_task_event)
            editTaskEvent?.setOnClickListener {
                taskEventItem?.let {
                    editTaskListener?.invoke(it)
                }
            }
        }

        fun bind(taskEvent: TaskEvent) {
            taskEventItem = taskEvent
            taskName.text = taskEvent.taskName
            val textColorResId =
                if (taskEvent.taskResult == TaskUpdateType.COMPLETED) android.R.color.holo_green_dark else R.color.textPrimary
            taskName.setTextColor(taskName.context.getColor(textColorResId))
            taskStartTime.text = taskEvent.getTaskStartTime()
            taskEndTime.text = taskEvent.getEndTime()
            taskDuration.text =
                DecimalFormat("#.#").format(taskEvent.durationSecs.toDouble() / 60)
        }

        private fun TaskEvent.getEndTime(): String =
            getFormattedTimeString(completedAtMillis)

        private fun TaskEvent.getTaskStartTime(): String {
            val startedAtMillis = completedAtMillis - (durationSecs * 1000)
            return getFormattedTimeString(startedAtMillis)
        }

        private fun getFormattedTimeString(startedAtMillis: Long): String {
            val localDateTime = LocalDateTime(startedAtMillis)
            val timeFormatter = DateTimeFormat.forPattern("HH:mm")
            return timeFormatter.print(localDateTime)
        }

    }

    private class TaskEventDiffer : DiffUtil.ItemCallback<TaskEvent>() {
        override fun areItemsTheSame(oldTask: TaskEvent, newTask: TaskEvent): Boolean {
            return oldTask.id == newTask.id
        }

        override fun areContentsTheSame(oldTask: TaskEvent, newTask: TaskEvent): Boolean {
            return oldTask == newTask
        }

    }

}
