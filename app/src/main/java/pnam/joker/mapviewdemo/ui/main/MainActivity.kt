package pnam.joker.mapviewdemo.ui.main

import android.app.SearchManager
import android.content.Context
import android.database.MatrixCursor
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
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
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.databinding.DataBindingUtil.setContentView
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
import java.io.IOException
import java.util.Locale

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


    private val settingBottomSheet: SettingBottomSheet by lazy {
        SettingBottomSheet(
            this,
            binding.bottomSheet,
            binding.appbar,
            binding.toolbar,
            changeStyleMap
        ).apply {
            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hidekeyboard()
                    setCollapsedBottomSheet()
                    query?.toLowerCase(Locale.getDefault())?.let { requireQuery ->
                        searchLocation(requireQuery)?.let { addresses ->
                            val address = if (addresses.isEmpty()) {
                                return true
                            } else {
                                addresses[0]
                            }
                            val latLng = LatLng(address.latitude, address.longitude)
                            map.addMarker(MarkerOptions().position(latLng).title("Current"))
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (settingBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        return true
                    }
                    newText?.toLowerCase(Locale.getDefault())?.let { query ->
                        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, ADDRESS))
                        if (query.isNotEmpty()) {
                            searchLocation(query)?.let { addresses ->
                                if (addresses.isNotEmpty()) {
                                    val names = addresses.toStringNames()
                                    for (index in 0 until addresses.size) {
                                        cursor.addRow(arrayOf(index, names[index]))
                                    }
                                }
                            }
                        }
                        adapter.changeCursor(cursor)
                    }
                    return true
                }

                fun MutableList<Address>.toStringNames(): MutableList<String> {
                    val mutableListNames = mutableListOf<String>()
                    for (name in this) {
                        mutableListNames.add(name.getAddressLine(0))
                    }
                    return mutableListNames
                }

                private fun searchLocation(query: String): MutableList<Address>? =
                    if (query != "") {
                        try {
                            Geocoder(this@MainActivity, Locale.getDefault()).getFromLocationName(
                                query,
                                1
                            )
                        } catch (e: IOException) {
                            null
                        }
                    } else {
                        null
                    }
            }
        }
    }

    private val changeStyleMap: (id: Int) -> Unit by lazy {
        { id ->
            if (id < 6) {
                map.mapType = id
            }
        }
    }

    private fun setUpBottomSheet() {
        settingBottomSheet.search.setSearchableInfo(
            (getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(
                componentName
            )
        )
        settingBottomSheet.search.suggestionsAdapter = adapter
        settingBottomSheet.show()
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
}