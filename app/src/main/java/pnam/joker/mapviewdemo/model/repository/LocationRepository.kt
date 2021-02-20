package pnam.joker.mapviewdemo.model.repository

import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import javax.inject.Singleton

@Singleton
interface LocationRepository {
    val network: LocationNetwork
    val local: LocationLocal
}