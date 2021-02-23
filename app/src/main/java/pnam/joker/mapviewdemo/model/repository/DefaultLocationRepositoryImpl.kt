package pnam.joker.mapviewdemo.model.repository

import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single
import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import javax.inject.Inject

class DefaultLocationRepositoryImpl @Inject constructor(
    override val network: LocationNetwork,
    override val local: LocationLocal
) : LocationRepository {
    override fun getLocationByName(query: String): Single<LatLng> =
        local.getLocationByName(query)

    override fun getAddressesByName(query: String): Single<List<String>> =
        local.getAddressesByName(query)
}