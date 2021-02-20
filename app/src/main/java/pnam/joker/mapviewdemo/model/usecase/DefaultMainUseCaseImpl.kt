package pnam.joker.mapviewdemo.model.usecase

import pnam.joker.mapviewdemo.model.repository.LocationRepository
import javax.inject.Inject

class DefaultMainUseCaseImpl @Inject constructor(override val repository: LocationRepository) : MainUseCase{

}