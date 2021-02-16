package pnam.joker.mapviewdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pnam.joker.mapviewdemo.R.drawable.top_corner
import pnam.joker.mapviewdemo.R.drawable.uncorner
import pnam.joker.mapviewdemo.databinding.BottomSheetSettingBinding


@SuppressLint("ClickableViewAccessibility")
class SettingBottomSheet(
    private val activity: Activity,
    private val binding: BottomSheetSettingBinding,
    private val appbar: AppBarLayout,
    private val toolbar: Toolbar,
    private val onChangeStyle: (id: Int) -> Unit
) {

    private val ratioExpandedHalf by lazy {
        0.5f
    }

    private val ratioExpandedQuarter by lazy {
        ratioExpandedHalf / 2
    }

    @Suppress("DEPRECATION")
    fun show() {
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallBack)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_SETTLING
        bottomSheetBehavior.isDraggable = false
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = ratioExpandedHalf
        binding.dragView.setOnTouchListener(onDragView)
        otherView()
    }


    private fun otherView() {
        binding.radioButtonStyles.setOnCheckedChangeListener(radioButtonEvent)
        binding.stylesComboBox.onItemSelectedListener = selectedListener
    }

    private inline fun <reified V> changeStyle(id: Int, parent: V) {
        when (parent) {
            is AdapterView<*> -> {
                binding.radioButtonStyles.check(id)
            }
            is RadioGroup -> {
                binding.stylesComboBox.setSelection(id)
            }
        }
        onChangeStyle(id)
    }


    private val radioButtonEvent by lazy {
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
            changeStyle(checkedId - 1, group)
        }
    }

    private val selectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                changeStyle(position + 1, parent)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> by lazy {
        BottomSheetBehavior.from(activity.findViewById(R.id.bottom_sheet))
    }

    private val onDragView: View.OnTouchListener by lazy {
        View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                bottomSheetBehavior.isDraggable = true
            }
            true
        }
    }

    private val bottomSheetCallBack: BottomSheetBehavior.BottomSheetCallback by lazy {
        @SuppressLint("UseCompatLoadingForDrawables")
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.isDraggable = false
                }
            }

            private val lessThanHalfExpandHandle: Event by lazy {
                Event()
            }

            private val greaterThanHalfHandle: Event by lazy {
                Event()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= ratioExpandedHalf) {
                    val currentActionBarSize =
                        actionBarSize * (slideOffset - ratioExpandedHalf) * (1 / ratioExpandedHalf)
                    showAppBar(toolbar, currentActionBarSize - actionBarSize)
                    showView(appbar, currentActionBarSize.toInt())
                    lessThanHalfExpandHandle.reset()
                    if (slideOffset == 1.0F) {
                        greaterThanHalfHandle.reset {
                            hideView(binding.view)
                            showView(binding.dragView, 8)
                            binding.bottomSheet.setBackgroundResource(uncorner)
                        }
                    } else {
                        greaterThanHalfHandle.treatIfNotProcess {
                            showView(binding.dragView, dragViewSize.toInt())
                            showView(binding.view, viewSize.toInt())
                            binding.bottomSheet.setBackgroundResource(top_corner)
                        }
                    }
                } else {
                    lessThanHalfExpandHandle.treatIfNotProcess {
                        hideView(appbar)
                    }
                }
            }
        }
    }

    private val dragViewSize: Float by lazy {
        activity.resources.getDimension(R.dimen.height_layout_drag_view)
    }


    private val viewSize: Float by lazy {
        activity.resources.getDimension(R.dimen.height_drag_view)
    }

    private fun hideView(view: View) {
        val params = view.layoutParams
        params.height = 0
        view.layoutParams = params
    }

    private fun showAppBar(view: View, yPosition: Float) {
        view.y = yPosition
    }

    private fun showView(view: View, size: Int) {
        val params = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

    private val actionBarSize: Int by lazy {
        activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            .getDimension(0, 0f).toInt()
    }

    var state: Int
        get() = bottomSheetBehavior.state
        set(value) {
            bottomSheetBehavior.state = value
        }

    class Event {
        var hasBeenHandled: Boolean = false
            private set

        fun treatIfNotProcess(handle: (() -> Unit)?) {
            if (!hasBeenHandled) {
                hasBeenHandled = true
                handle?.invoke()
            }
        }

        fun treatIfNotProcess() = treatIfNotProcess(null)

        fun reset(handle: (() -> Unit)?) {
            hasBeenHandled = false
            handle?.invoke()
        }

        fun reset() = reset(null)
    }
}