package pnam.joker.mapviewdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
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
    private val toolbar: Toolbar
) {

    private val ratioHalfExpanded by lazy {
        0.5f
    }

    @Suppress("DEPRECATION")
    fun show() {
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallBack)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_SETTLING
        bottomSheetBehavior.isDraggable = false
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = ratioHalfExpanded
        binding.dragView.setOnTouchListener(onDragView)
    }

    private val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> by lazy {
        BottomSheetBehavior.from(activity.findViewById(R.id.bottom_sheet))
    }

    private val onDragView: View.OnTouchListener by lazy {
        View.OnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                bottomSheetBehavior.isDraggable = true
            }
            true
        }
    }

    private val bottomSheetCallBack: BottomSheetBehavior.BottomSheetCallback by lazy {
        @SuppressLint("UseCompatLoadingForDrawables")
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("aaaaaaaaaaaaaaaaa", newState.toString())
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED ->{
                        bottomSheetBehavior.isDraggable = false
                    }
                }
            }

            private var isChanged = false
            private var isHalfExpanded = false

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.d("ccccccccccccccccccc", slideOffset.toString())
                if (slideOffset >= 0.5) {
                    val currentActionBarSize = actionBarSize * (slideOffset - ratioHalfExpanded) * (1/ ratioHalfExpanded)
                    showAppBar(toolbar, currentActionBarSize - actionBarSize)
                    showView(appbar, currentActionBarSize.toInt())
                    isHalfExpanded = false
                    if (slideOffset == 1.0F) {
                        isChanged = true
                        isHalfExpanded = false
//                        hideView(binding.dragView)
                        hideView(binding.view)
                        showView(binding.dragView, 8)
                        binding.bottomSheet.setBackgroundResource(uncorner)
                    } else if (isChanged) {
                        showView(binding.dragView, dragViewSize.toInt())
                        showView(binding.view, viewSize.toInt())
                        isChanged = false
                        binding.bottomSheet.setBackgroundResource(top_corner)
                    }
                } else {
                    if (!isHalfExpanded) {
                        isHalfExpanded = true
                        hideView(appbar)
                    }
                }
            }
        }
    }

    private val dragViewSize:Float by lazy {
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
}