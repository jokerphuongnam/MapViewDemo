package pnam.joker.mapviewdemo.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import pnam.joker.mapviewdemo.model.usecase.MainUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val useCase: MainUseCase) : ViewModel() {
    private val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private val _locationLiveData: MutableLiveData<LatLng> by lazy {
        MutableLiveData<LatLng>()
    }
    val locationLiveData: MutableLiveData<LatLng> get() = _locationLiveData
    private val locationObservable: Consumer<LatLng> by lazy {
        Consumer<LatLng> { latLng ->
            _locationLiveData.postValue(latLng)
        }
    }

    fun getLocationByName(query: String) {
        val locationDispose: Disposable =
            useCase.getLocationByName(query).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(locationObservable)
        compositeDisposable.remove(locationDispose)
        compositeDisposable.add(locationDispose)
    }


    private val _addressesLiveData: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>()
    }
    val addressesLiveData: MutableLiveData<List<String>> get() = _addressesLiveData
    val addresses: List<String> get() = _addressesLiveData.value!!
    private val addressesObservable: Consumer<List<String>> by lazy {
        Consumer<List<String>> { addresses ->
            _addressesLiveData.postValue(addresses)
        }
    }

    fun getAddressesByName(query: String) {
        val addressesDispose: Disposable =
            useCase.getAddressesByName(query)
                .delay(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(addressesObservable)
        compositeDisposable.remove(addressesDispose)
        compositeDisposable.add(addressesDispose)
    }

    fun onPause() {
        compositeDisposable.dispose()
    }
}