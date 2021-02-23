package pnam.joker.mapviewdemo.model.database.local

import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import javax.inject.Singleton

@Singleton
interface LocationLocal {
    fun getLocationByName(query: String): Single<LatLng>
    fun getAddressesByName(query: String): Single<List<String>>
}
