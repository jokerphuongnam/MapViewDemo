package pnam.joker.mapviewdemo.model.usecase

import pnam.joker.mapviewdemo.model.repository.LocationRepository
import javax.inject.Singleton

@Singleton
interface MainUseCase {
    val repository: LocationRepository
}