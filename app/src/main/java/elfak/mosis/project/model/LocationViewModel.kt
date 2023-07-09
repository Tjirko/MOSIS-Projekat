package elfak.mosis.project.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    private val _latitude = MutableLiveData<String>("")
    val latitude: LiveData<String>
        get() = _latitude

    private val _longitude = MutableLiveData<String>("")
    val longitude: LiveData<String>
        get() = _longitude

    var setLocation: Boolean = false
    fun setLocation(lat:String,lon:String){
        _latitude.value = lat
        _longitude.value = lon
        setLocation = false
    }
}