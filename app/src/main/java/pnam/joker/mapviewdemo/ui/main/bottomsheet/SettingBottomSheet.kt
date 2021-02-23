package pnam.joker.mapviewdemo.ui.main.bottomsheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pnam.joker.mapviewdemo.R.dimen.*
import pnam.joker.mapviewdemo.R.drawable.top_corner
import pnam.joker.mapviewdemo.R.drawable.uncorner
import pnam.joker.mapviewdemo.databinding.BottomSheetSettingBinding


@SuppressLint("ClickableViewAccessibility")
class SettingBottomSheet(
    private val context: Context,
    private val binding: BottomSheetSettingBinding,
    private val appbar: AppBarLayout,
    private val toolbar: Toolbar,
    private val onChangeStyle: (id: Int) -> Unit
) {

    private val ratioExpandedHalf by lazy {
        0.6f
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

    private val bottomSheetBehavior: BottomSheetBehavior<NestedScrollView> by lazy {
        BottomSheetBehavior.from(binding.bottomSheet)
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
                when(newState){
                    BottomSheetBehavior.STATE_HALF_EXPANDED ->{
                        binding.layoutComboBox.showView(comboBoxHeight.toInt())
                        binding.stylesComboBox.changePosition(0F)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED ->{
                        bottomSheetBehavior.isDraggable = false
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }

            private val lessThanHalfExpandHandle: Event by lazy {
                Event()
            }

            private val greaterThanHalfHandle: Event by lazy {
                Event()
            }

            private fun changeAppBarSize(slideOffset: Float) {
                val currentActionBarSize =
                    context.actionBarSize * ((slideOffset - ratioExpandedHalf) /(1 -ratioExpandedHalf))
                toolbar.changePosition(currentActionBarSize - context.actionBarSize)
                appbar.showView(currentActionBarSize.toInt())
            }

            private fun changeDragViewSize(slideOffset: Float) {
                if (slideOffset == 1F) {
                    greaterThanHalfHandle.reset {
                        binding.view.hideView()
                        binding.dragView.showView(8)
                        binding.bottomSheet.setBackgroundResource(uncorner)
                    }
                } else {
                    greaterThanHalfHandle.treatIfNotProcess {
                        binding.dragView.showView(dragViewSize.toInt())
                        binding.view.showView(viewSize.toInt())
                        binding.bottomSheet.setBackgroundResource(top_corner)
                    }
                }
            }

            private val comboBoxHeight by lazy {
                context.resources.getDimension(combo_box_height)
            }

            private val radioGroupHeight by lazy {
                context.resources.getDimension(radio_group_height)
            }

            private fun changeChildSize(slideOffset: Float) {
                val currentRatio = (1 - slideOffset - ratioExpandedQuarter) / ratioExpandedQuarter
                when (slideOffset) {
                    1F -> {
                        binding.radioButtonStyles.showView(radioGroupHeight.toInt())
                    }
                    in (1 - ratioExpandedQuarter)..1F -> {
                        val radioSize = radioGroupHeight * (-currentRatio)
                        binding.radioButtonStyles.showView(radioSize.toInt())
                    }
                    1 - ratioExpandedQuarter -> {
                        binding.layoutComboBox.hideView()
                        binding.stylesComboBox.changePosition(-comboBoxHeight)
                    }
                    in ratioExpandedHalf..(1 - ratioExpandedQuarter) -> {
                        val comboBoxSize = comboBoxHeight * currentRatio
                        binding.layoutComboBox.showView(comboBoxSize.toInt())
                        binding.stylesComboBox.changePosition(comboBoxHeight - comboBoxSize)
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= ratioExpandedHalf) {
                    lessThanHalfExpandHandle.reset()
                    changeAppBarSize(slideOffset)
                    changeDragViewSize(slideOffset)
                    changeChildSize(slideOffset)
                } else {
                    lessThanHalfExpandHandle.treatIfNotProcess {
                        appbar.hideView()
                        binding.radioButtonStyles.hideView()
                    }
                    val comboBoxSize = comboBoxHeight * (slideOffset / ratioExpandedHalf)
                    binding.layoutComboBox.showView(comboBoxSize.toInt())
                    binding.stylesComboBox.changePosition(comboBoxHeight - comboBoxSize)
                }
            }
        }
    }

    private val dragViewSize: Float by lazy {
        context.resources.getDimension(height_layout_drag_view)
    }


    private val viewSize: Float by lazy {
        context.resources.getDimension(height_drag_view)
    }

    private fun View.hideView() {
        val params = layoutParams
        params.height = 0
        layoutParams = params
    }

    private fun View.changePosition(yPosition: Float) {
        y = yPosition
    }

    private fun View.showView(size: Int) {
        val params = layoutParams
        params.height = size
        layoutParams = params
    }

    var state: Int
        get() = bottomSheetBehavior.state
        set(value) {
            bottomSheetBehavior.state = value
        }

    val search: SearchView by lazy {
        binding.search
    }

    private val Context.actionBarSize: Int by lazy {
        context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            .getDimension(0, 0f).toInt()
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

//        fun treatIfNotProcess() = treatIfNotProcess(null)

        fun reset(handle: (() -> Unit)?) {
            hasBeenHandled = false
            handle?.invoke()
        }

        fun reset() = reset(null)
    }
}