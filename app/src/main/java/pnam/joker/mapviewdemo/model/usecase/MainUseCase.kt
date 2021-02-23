package pnam.joker.mapviewdemo.model.usecase

import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import pnam.joker.mapviewdemo.model.repository.LocationRepository
import javax.inject.Singleton

@Singleton
interface MainUseCase {
    val repository: LocationRepository
    fun getLocationByName(query: String): Single<LatLng>
    fun getAddressesByName(query: String): Single<List<String>>
}