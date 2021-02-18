package pnam.joker.mapviewdemo

import android.content.Context
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil.setContentView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pnam.joker.mapviewdemo.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
                    query?.toLowerCase()?.let { requireQuery ->
                        searchLocation(requireQuery)?.let { addresses ->
                            val address = if(addresses.isEmpty()){
                                return true
                            }else{
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
                    newText?.toLowerCase()?.let {query ->
                        searchLocation(query)?.let { addresses ->

                        }
                    }
                    return true
                }

                private fun searchLocation(query: String): MutableList<Address>? =
                    if (query != "") {
                        try {
                            Geocoder(this@MainActivity).getFromLocationName(query, 10)
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
        settingBottomSheet.show()
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
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(
                    currentFocus?.windowToken,
                    0
                )
        } else {
            when (settingBottomSheet.state) {
                BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_EXPANDED -> {
                    settingBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                else -> {
                    super.onBackPressed()
                }
            }
        }
    }

    private val isShowKeyBoard: Boolean
        get() = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).isAcceptingText

    private fun unFocus() {
        currentFocus?.clearFocus()
    }

    private val actionBarSize: Int by lazy {
        theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            .getDimension(0, 0f).toInt()
    }
}