package com.cronus.zdone.home

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cronus.zdone.R
import com.cronus.zdone.home.HomeScreen.DisplayedTask
import com.cronus.zdone.home.HomeScreen.TaskProgressState.*
import com.wealthfront.blend.Blend
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.home.view.*

class HomeView(context: Context?) : BaseScreenView<HomeScreen>(context) {

    val tasksListAdapter: TasksListAdapter

    init {
        View.inflate(context, R.layout.home, this)
        tasksListView.layoutManager = LinearLayoutManager(context)
        tasksListAdapter = TasksListAdapter()
        tasksListView.adapter = tasksListAdapter
        tasksListAdapter.onTaskCompletedListener = { task ->
            screen.taskCompleted(task)
            setTimeProgress(timeComplete.progress)
        }
        tasksListAdapter.onTaskDeferredListener = { task ->
            screen.deferTask(task)
        }
        tasksListAdapter.onTaskStartedListener = { task ->
            screen.startTask(task)
        }
        tasksListAdapter.onTaskPausedListener = { task ->
            screen.pauseTask(task)
        }
        tasksSwipeRefresh.setOnRefreshListener {
            screen.refreshTaskData()
        }
        View.inflate(context, R.layout.error, this)
        errorTryAgain.setOnClickListener {
            tasksContent.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            screen.refreshTaskData()
        }
        errorView.visibility = View.GONE
    }

    fun setTasks(tasks: List<DisplayedTask>) {
        loading.hide()
        errorView.visibility = View.GONE
        tasksListAdapter.tasksList = tasks.toMutableList()
    }

    fun setTimeProgress(newProgress: Int) {
        val prevProgress = timeComplete.progress
        timeComplete.animate()
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setUpdateListener {
                    timeComplete.progress =
                        prevProgress + (it.animatedFraction * (newProgress - prevProgress)).toInt()
                }
                .start()
    }

    fun finishedRefreshing() {
        tasksSwipeRefresh.isRefreshing = false
    }

    fun showError(message: String?) {
        message?.let {
            errorInfo.visibility = View.VISIBLE
            errorInfo.text = it
        }
        val blend = Blend()
        blend {
            duration(200)
            target(tasksContent).animations {
                fadeOut()
            }
        }.then {
            duration(200)
            target(errorView).animations {
                fadeIn()
            }
        }.start()
        errorView.visibility = View.VISIBLE
    }

    fun showLoading() {
        errorView.visibility = View.GONE
        Blend().invoke {
            target(loading).animations {
                fadeIn()
            }
        }.start()
    }

    fun setInProgressTask(task: DisplayedTask) {
        tasksListAdapter.setInProgressTask(task)
    }

    fun setTasksProgressState(taskProgressState: HomeScreen.TaskProgressState) {
        tasksListAdapter.setTasksProgressState(taskProgressState)
    }

    class TasksListAdapter : ListAdapter<DisplayedTask, TasksListAdapter.TaskViewHolder>(
            TaskDiffer()
    ) {

        var tasksList: MutableList<DisplayedTask> = mutableListOf()
            set(value) {
                field = value
                submitList(field)
            }

        var onTaskCompletedListener: ((DisplayedTask) -> Unit)? = null
        var onTaskDeferredListener: ((DisplayedTask) -> Unit)? = null
        var onTaskStartedListener: ((DisplayedTask) -> Unit)? = null
        var onTaskPausedListener: ((DisplayedTask) -> Unit)? = null

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position).isSubtask) R.layout.subtask_item else R.layout.task_item
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return TaskViewHolder(
                    itemView,
                    isSubtask = viewType == R.layout.subtask_item
            )
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val task = getItem(position)
            val context = holder.taskNameView.context
            holder.taskNameView.text = context.getString(R.string.task_item_name, task.name, task.lengthMins)
            holder.taskCompletedCheckbox.isChecked = false
            holder.taskCompletedCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    optimisticallyRemoveTask(task)
                    onTaskCompletedListener?.invoke(task)
                }
            }
            holder.deferTask?.setOnClickListener {
                optimisticallyRemoveTask(task)
                onTaskDeferredListener?.invoke(task)
            }
            holder.startTask.setOnClickListener {
                when (task.progressState) {
                    IN_PROGRESS -> onTaskPausedListener?.invoke(task)
                    READY -> onTaskStartedListener?.invoke(task)
                    WAITING -> throw IllegalStateException("Start task button should be disabled while other tasks are in progress.")
                }
            }
            val startTaskResId = when (task.progressState) {
                READY -> R.drawable.start_task
                IN_PROGRESS -> R.drawable.pause_task
                WAITING -> R.drawable.start_task
            }
            holder.startTask.setImageResource(startTaskResId)
            holder.startTask.alpha = if (task.progressState == WAITING) .5f else 1f
            holder.startTask.isEnabled = if (task.progressState == WAITING) false else true
            holder.divider.visibility = if (task.showDivider) View.VISIBLE else View.GONE
        }

        private fun optimisticallyRemoveTask(task: DisplayedTask) {
            Handler(getMainLooper())
                    .postDelayed({
                        val location = tasksList.indexOf(task)
                        var subtasksRemoved = 0
                        if (!task.isSubtask) {
                            subtasksRemoved = removeSubTasksForTaskAt(location)
                        }
                        tasksList.remove(task)
                        notifyItemRangeRemoved(location, subtasksRemoved + 1)
                        if (task.isSubtask && task.showDivider) {
                            val upTask = tasksList.get(location - 1)
                            tasksList.set(location - 1, DisplayedTask(upTask.id, upTask.subtaskId, upTask.name, upTask.service, upTask.lengthMins, upTask.isSubtask, showDivider = true, progressState = upTask.progressState))
                            notifyItemChanged(location - 1)
                        }
                    }, 500)
        }

        private fun removeSubTasksForTaskAt(location: Int): Int {
            var subtasksRemoved = 0
            val subTaskLocation = location + 1
            while (subTaskLocation < tasksList.size && tasksList.get(subTaskLocation).isSubtask) {
                tasksList.removeAt(subTaskLocation)
                subtasksRemoved++
            }
            return subtasksRemoved
        }

        fun setTasksProgressState(taskProgressState: HomeScreen.TaskProgressState) {
            tasksList.forEach {
                it.progressState = taskProgressState
            }
            notifyDataSetChanged()
        }

        fun setInProgressTask(task: DisplayedTask) {
            tasksList.find { it.id == task.id }?.let {
                setTasksProgressState(WAITING) // make all other tasks wait
                it.progressState = IN_PROGRESS
                notifyItemChanged(tasksList.indexOf(it))
            }
        }

        class TaskViewHolder(view: View, isSubtask: Boolean) : RecyclerView.ViewHolder(view) {

            val taskNameView = view.findViewById<TextView>(R.id.task_name)
            val taskCompletedCheckbox = view.findViewById<CheckBox>(R.id.task_completed)
            var deferTask: ImageView? = if (!isSubtask) view.findViewById(R.id.defer_task) else null
            var startTask: ImageView = view.findViewById(R.id.start_task)
            val divider = view.findViewById<View>(R.id.divider)

            init {
                val normalPaddingPx = view.resources.getDimensionPixelSize(R.dimen.normalPadding)
                view.post {
                    expandHitTarget(taskCompletedCheckbox, normalPaddingPx)
                    expandHitTarget(deferTask, normalPaddingPx)
                    expandHitTarget(startTask, normalPaddingPx)
                }
            }

            private fun expandHitTarget(view: View?, pixels: Int) {
                view?.let {
                    val taskCompletedRect = Rect()
                    view.getHitRect(taskCompletedRect)
                    taskCompletedRect.top -= pixels
                    taskCompletedRect.left -= pixels
                    taskCompletedRect.bottom += pixels
                    taskCompletedRect.right += pixels
                    view.touchDelegate = TouchDelegate(taskCompletedRect, taskCompletedCheckbox)
                }
            }

        }

        class TaskDiffer : DiffUtil.ItemCallback<DisplayedTask>() {
            override fun areItemsTheSame(oldItem: DisplayedTask, newItem: DisplayedTask): Boolean {
                return oldItem.equals(newItem)
            }

            override fun areContentsTheSame(oldItem: DisplayedTask, newItem: DisplayedTask): Boolean {
                return oldItem.name == newItem.name
            }

        }
    }
}
