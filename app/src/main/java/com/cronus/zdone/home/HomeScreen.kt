package com.cronus.zdone.home

import android.content.Context
import android.content.DialogInterface
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import com.cronus.zdone.AddTaskDialogView
import com.cronus.zdone.OptionItemSelectedListener
import com.cronus.zdone.R
import com.cronus.zdone.Toaster
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.AddTaskInfo
import com.cronus.zdone.timer.TimerScreen
import com.cronus.zdone.util.Do
import com.dropbox.android.external.store4.StoreResponse
import com.wealthfront.magellan.DialogCreator
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.ScreenGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import javax.inject.Inject

class HomeScreen @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val toaster: Toaster,
    timerScreen: TimerScreen,
    tasksScreen: TasksScreen) :
    ScreenGroup<Screen<*>, HomeView>(listOf(timerScreen, tasksScreen)), OptionItemSelectedListener {

    override fun createView(context: Context): HomeView = HomeView(context, screens)

    override fun onShow(context: Context?) {
        super.onShow(context)
        screens.map {
            view.addTabView(it.getView())
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.app_name)
    }

    override fun onUpdateMenu(menu: Menu) {
        menu.findItem(R.id.settings).setVisible(true)
        menu.findItem(R.id.dailyStatsMenuItem).setVisible(true)
        menu.findItem(R.id.addTaskMenuItem).setVisible(true)
    }

    override fun optionItemSelected(id: Int) {
        when (id) {
            R.id.addTaskMenuItem -> showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        showDialog {
            val builder = AlertDialog.Builder(activity)
            val addTaskDialogView = AddTaskDialogView(activity)
            addTaskDialogView.onSubmitListener = { taskName, durationMins ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (!taskName.isEmpty()) {
                        tasksRepository.addTask(
                            AddTaskInfo(
                                taskName,
                                LocalDate.now().toString(),
                                durationMins))
                            .collect {
                                Do exhaustive when (it) {
                                    is StoreResponse.Loading -> toaster.showToast("Sending add task to server")
                                    is StoreResponse.Data -> {
                                        toaster.showToast("Successfully added task")
                                        tasksRepository.refreshTaskDataFromStore()
                                    }
                                    is StoreResponse.Error -> toaster.showToast("Failed to add task, please try again")
                                }
                            }
                    }
                }
            }
            builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
            builder.setPositiveButton("Add", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    addTaskDialogView.onSubmitClicked()
                    dialog.dismiss()
                }

            })
            builder.setView(addTaskDialogView)
            builder.create()
        }
    }
}