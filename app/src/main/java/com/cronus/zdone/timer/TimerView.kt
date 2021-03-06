package com.cronus.zdone.timer

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import com.cronus.zdone.R
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.stats.summary.DailyStatsSummary
import com.cronus.zdone.timer.TimerScreen.TimerState
import com.cronus.zdone.timer.TimerScreen.ViewState
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.timer.view.*

class TimerView(context: Context) : BaseScreenView<TimerScreen>(context) {

    init {
        inflate(context, R.layout.timer, this)
        startTasks.setOnClickListener {
            screen.startTasks()
        }
        completeTaskButton.setOnClickListener {
            screen.completeTask()
        }
        deferTaskButton.setOnClickListener {
            screen.deferTask()
        }
    }

    fun setState(viewState: ViewState) {
        clearState()
        when (viewState.timerState) {
            TimerState.LOADING -> showLoadingState()
            TimerState.INITIAL -> showInitialState(viewState)
            TimerState.RUNNING -> showRunningTask(viewState)
            TimerState.FINISHED -> showStoppedState(viewState)
        }
    }

    private fun showLoadingState() {
        loading.visibility = View.VISIBLE
    }

    private fun showStoppedState(viewState: ViewState) {
        taskName.text = viewState.taskName
        startTasks.visibility = GONE
        completeTaskButton.visibility = GONE
        deferTaskButton.visibility = GONE
        minsRemaining.visibility = GONE
        secsRemaining.visibility = GONE
    }

    private fun showRunningTask(viewState: ViewState) {
        taskName.text = viewState.taskName
        minsRemaining.text = viewState.minsRemaining.toString()
        minsRemaining.visibility = View.VISIBLE
        secsRemaining.text = String.format("%02d", viewState.secsRemaining)
        secsRemaining.visibility = View.VISIBLE
        colon.visibility = View.VISIBLE

        if (viewState.isLate) {
            minsRemaining.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            secsRemaining.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            colon.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else {
            minsRemaining.setTextColor(resources.getColor(R.color.textPrimary))
            secsRemaining.setTextColor(resources.getColor(R.color.textPrimary))
            colon.setTextColor(resources.getColor(R.color.textPrimary))
        }

        completeTaskButton.visibility = View.VISIBLE
        deferTaskButton.visibility = View.VISIBLE
    }

    private fun showInitialState(viewState: ViewState) {
        taskName.text = viewState.taskName
        startTasks.visibility = VISIBLE
        showDailyStats()
        showUpcomingTasks()
    }

    private fun showUpcomingTasks() {
        upcomingTasksTitle.visibility = View.VISIBLE
        upcomingTasksList.visibility = View.VISIBLE
    }

    private fun showDailyStats() {
        dailyStatsViews.map { it.visibility = View.VISIBLE }
    }

    private fun clearState() {
        taskName.text = ""
        minsRemaining.text = ""
        minsRemaining.visibility = GONE
        secsRemaining.text = ""
        secsRemaining.visibility = GONE
        colon.visibility = View.GONE
        minsRemaining.setTextColor(resources.getColor(R.color.textPrimary))
        secsRemaining.setTextColor(resources.getColor(R.color.textPrimary))
        startTasks.visibility = GONE
        completeTaskButton.visibility = GONE
        deferTaskButton.visibility = GONE
        loading.visibility = GONE
        clearDailyStatsState()
        clearUpcomingStatsState()
    }

    private fun clearUpcomingStatsState() {
        upcomingTasksTitle.visibility = View.GONE
        upcomingTasksList.visibility = View.GONE
    }

    private val dailyStatsViews = arrayOf(
        dailyStatsTitle,
        dailyStatsActualMinsWorked,
        dailyStatsExpectedMinsWorked,
        dailyStatsTasksCompleted,
        dailyStatsTasksDeferred
    )

    private fun clearDailyStatsState() {
        dailyStatsViews.map { it.visibility = GONE }
    }

    fun setDailyStats(statsSummary: DailyStatsSummary) {
        dailyStatsActualMinsWorked.text = "Mins worked: ${statsSummary.actualSecondsWorked / 60}"
        dailyStatsExpectedMinsWorked.text = "Expected: ${statsSummary.expectedSecondsWorked / 60}"
        dailyStatsTasksCompleted.text = "Completed: ${statsSummary.numTasksCompleted}"
        dailyStatsTasksDeferred.text = "Deferred: ${statsSummary.numTasksDeferred}"
    }

    fun setSelectedTasks(it: List<Task>) {
        upcomingTasksTitle.text = context.getString(if (it.isEmpty())
            R.string.upcoming_tasks_empty_title
        else
            R.string.upcoming_tasks)
        upcomingTasksList.adapter = ArrayAdapter<String>(context, R.layout.upcoming_tasks_list_item, it.map { it.name })
    }

}
