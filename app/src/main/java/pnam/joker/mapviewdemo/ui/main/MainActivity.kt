package pnam.joker.mapviewdemo.ui.main

import android.app.SearchManager
import android.content.Context
import android.database.MatrixCursor
import android.graphics.Rect
import android.os.Bundle
import android.provider.BaseColumns
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView.OnSuggestionListener
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import pnam.joker.mapviewdemo.R
import pnam.joker.mapviewdemo.databinding.ActivityMainBinding
import pnam.joker.mapviewdemo.ui.main.bottomsheet.SettingBottomSheet
import pnam.joker.mapviewdemo.utils.Constants.ADDRESS

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var map: GoogleMap
    private var market: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        setUpActionBar()
        setUpGoogleMap()
        setUpBottomSheet()
        setUpViewModel()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun setUpViewModel() {
        viewModel.locationLiveData.observe(Observer {latLng ->
            map.addMarker(MarkerOptions().position(latLng).title("Current"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
        })
        viewModel.addressesLiveData.observe(Observer {addresses->
            val cursor = MatrixCursor(arrayOf(BaseColumns._ID, ADDRESS))
            for (index in addresses.indices) {
                cursor.addRow(arrayOf(index, addresses[index]))
            }
            adapter.changeCursor(cursor)
        })
    }

    private fun <T> LiveData<T>.observe(observer: Observer<T>) {
        observe(this@MainActivity, observer)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        title = null
        val actionBar = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpGoogleMap() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { googleMap ->
            map = googleMap
            map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(220.2, 231.0)))
            map.setOnMapClickListener { latlng ->
                market?.remove()
                market = map.addMarker(
                    MarkerOptions().position(latlng).title("Current")
                )
            }
            map.setOnPoiClickListener { point ->
                Toast.makeText(
                    this, """Clicked: ${point.name}
                        Place ID:${point.placeId}
                        Latitude:${point.latLng.latitude} Longitude:${point.latLng.longitude}""",
                    Toast.LENGTH_SHORT
                ).show()
            }
            map.mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    private val changeStyleMap: (id: Int) -> Unit by lazy {
        { id: Int ->
            if (id < 6) {
                map.mapType = id
            }
        }
    }

    private val settingBottomSheet: SettingBottomSheet by lazy {
        SettingBottomSheet(
            this,
            binding.bottomSheet,
            binding.appbar,
            binding.toolbar,
            changeStyleMap
        )
    }

    private fun setUpBottomSheet() {
        settingBottomSheet.search.apply {
            setSearchableInfo(
                (getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(
                    componentName
                )
            )
            suggestionsAdapter = adapter
            setOnQueryTextListener(queryCallback)
            setOnSuggestionListener(clickItemCallBack)
        }
        settingBottomSheet.show()
    }

    private val clickItemCallBack: OnSuggestionListener by lazy {
        object : OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                settingBottomSheet.search.setQuery(viewModel.addresses[position], true)
                return true
            }
        }
    }

    private val queryCallback: OnQueryTextListener by lazy {
        object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hidekeyboard()
                setCollapsedBottomSheet()
                query ?: return true
                if(query.isNotEmpty()){
                    viewModel.getLocationByName(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (settingBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    return true
                }
                newText?: return true
                if(newText.isNotEmpty()){
                    viewModel.getAddressesByName(newText)
                }
                return true
            }
        }
    }

    private val adapter by lazy {
        SimpleCursorAdapter(
            this,
            R.layout.search_item,
            null,
            arrayOf(ADDRESS),
            arrayOf(R.id.suggest_line).toIntArray(),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        if (isShowKeyBoard) {
            hidekeyboard()
        } else {
            setCollapsedBottomSheet()
        }
    }

    private fun setCollapsedBottomSheet() {
        when (settingBottomSheet.state) {
            BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_EXPANDED -> {
                settingBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private val isShowKeyBoard: Boolean
        get() = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).isAcceptingText

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hidekeyboard() {
        currentFocus?.let {
            hideKeyboard(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.executePendingBindings()
    }
}