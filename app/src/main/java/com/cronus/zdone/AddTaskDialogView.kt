package com.cronus.zdone

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.cronus.zdone.api.model.AddTaskInfo
import kotlinx.android.synthetic.main.add_task_dialog.view.*
import kotlinx.android.synthetic.main.task_item.view.*
import kotlinx.android.synthetic.main.task_item.view.task_name
import java.lang.IllegalStateException

class AddTaskDialogView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attributeSet, defStyleAttr) {

    var onSubmitListener: ((String, Long) -> Unit)? = null

    init {
        View.inflate(context, R.layout.add_task_dialog, this)
        post {
            task_name.requestFocus()
        }
    }

    fun onSubmitClicked() {
        onSubmitListener?.invoke(task_name.text.toString(), getSelectedDuration())
    }

    private fun getSelectedDuration(): Long {
        return when (duration_options.checkedRadioButtonId) {
            R.id.five_mins -> 5
            R.id.fifteen_mins -> 15
            R.id.thirty_mins -> 30
            R.id.sixty_mins -> 60
            else -> throw IllegalStateException("Duration not selected when trying to save new task")
        }
    }

}
