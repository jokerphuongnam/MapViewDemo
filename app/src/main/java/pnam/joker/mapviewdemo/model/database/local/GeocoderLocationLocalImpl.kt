package pnam.joker.mapviewdemo.model.database.local

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

class GeocoderLocationLocalImpl @Inject constructor(private val geocoder: Geocoder) :
    LocationLocal {
    override fun getLocationByName(query: String): Single<LatLng> = Single.create { emiter ->
        val addresses =
            geocoder.getFromLocationName(query.toLowerCase(Locale.getDefault()), 1)
        if (addresses.isEmpty()) {
            return@create
        } else {
            val address = addresses[0]
            emiter.onSuccess(LatLng(address.latitude, address.longitude))
        }
    }

    override fun getAddressesByName(query: String): Single<List<String>> =
        Single.create { emiter ->
            val addresses =
                geocoder.getFromLocationName(query.toLowerCase(Locale.getDefault()), 1)
            if (addresses.isEmpty()) {
                return@create
            } else {
                emiter.onSuccess(addresses.toStringNames())
            }
        }


    private fun MutableList<Address>.toStringNames(): List<String> {
        val mutableListNames = mutableListOf<String>()
        for (name in this) {
            mutableListNames.add(name.getAddressLine(0))
        }
        return mutableListNames
    }
}