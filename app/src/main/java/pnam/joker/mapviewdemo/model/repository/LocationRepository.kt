package pnam.joker.mapviewdemo.model.repository

import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import javax.inject.Singleton

@Singleton
interface LocationRepository {
    val network: LocationNetwork
    val local: LocationLocal
    fun getLocationByName(query: String): Single<LatLng>
    fun getAddressesByName(query: String): Single<List<String>>
}