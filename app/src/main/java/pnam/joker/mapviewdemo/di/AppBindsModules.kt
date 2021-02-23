package pnam.joker.mapviewdemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pnam.joker.mapviewdemo.model.database.local.GeocoderLocationLocalImpl
import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import pnam.joker.mapviewdemo.model.database.network.GoogleMapsLocationNetworkImpl
import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import pnam.joker.mapviewdemo.model.repository.DefaultLocationRepositoryImpl
import pnam.joker.mapviewdemo.model.repository.LocationRepository
import pnam.joker.mapviewdemo.model.usecase.DefaultMainUseCaseImpl
import pnam.joker.mapviewdemo.model.usecase.MainUseCase

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModules {

    @Binds
    abstract fun getNetworkLocation(network: GoogleMapsLocationNetworkImpl): LocationNetwork

    @Binds
    abstract fun getLocalLocation(local: GeocoderLocationLocalImpl): LocationLocal

    @Binds
    abstract fun getMainUseCase(mainUseCase: DefaultMainUseCaseImpl): MainUseCase

    @Binds
    abstract fun getLocationRepository(locationRepository: DefaultLocationRepositoryImpl): LocationRepository
}