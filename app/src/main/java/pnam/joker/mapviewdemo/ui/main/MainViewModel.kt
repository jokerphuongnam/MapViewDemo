package pnam.joker.mapviewdemo.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import pnam.joker.mapviewdemo.model.usecase.MainUseCase

class MainViewModel @ViewModelInject constructor(private val useCase: MainUseCase): ViewModel() {
}