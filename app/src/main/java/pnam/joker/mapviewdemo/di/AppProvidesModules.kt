package pnam.joker.mapviewdemo.di

import android.content.Context
import android.location.Geocoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pnam.joker.mapviewdemo.model.database.local.GeocoderLocationLocalImpl
import pnam.joker.mapviewdemo.model.database.local.LocationLocal
import pnam.joker.mapviewdemo.model.database.network.GoogleMapsLocationNetworkImpl
import pnam.joker.mapviewdemo.model.database.network.LocationNetwork
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModules {

    @Provides
    @Singleton
    fun provideLocationLocal(@ApplicationContext context: Context): LocationLocal =
        GeocoderLocationLocalImpl(Geocoder(context, Locale.getDefault()))

    @Provides
    @Singleton
    fun provideLocationNetwork(): LocationNetwork = GoogleMapsLocationNetworkImpl(OkHttpClient())
}