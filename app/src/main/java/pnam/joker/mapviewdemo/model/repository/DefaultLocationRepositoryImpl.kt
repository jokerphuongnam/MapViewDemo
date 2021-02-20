package pnam.joker.mapviewdemo.model.repository

import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import javax.inject.Inject

class DefaultLocationRepositoryImpl @Inject constructor(
    override val network: LocationNetwork,
    override val local: LocationLocal
) : LocationRepository