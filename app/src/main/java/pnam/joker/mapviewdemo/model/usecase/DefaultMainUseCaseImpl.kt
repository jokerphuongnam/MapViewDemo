package pnam.joker.mapviewdemo.model.usecase

import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import pnam.joker.mapviewdemo.model.repository.LocationRepository
import javax.inject.Inject

class DefaultMainUseCaseImpl @Inject constructor(override val repository: LocationRepository) :
    MainUseCase {
    override fun getLocationByName(query: String): Single<LatLng> =
        repository.getLocationByName(query)

    override fun getAddressesByName(query: String): Single<List<String>> =
        repository.getAddressesByName(query)
}