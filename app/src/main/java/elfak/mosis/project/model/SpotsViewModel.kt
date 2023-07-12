package elfak.mosis.project.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elfak.mosis.project.data.SpotsData

class SpotsViewModel:  ViewModel(){
    var spots = ArrayList<SpotsData>()

    private val _nameSelectedSpot = MutableLiveData<String>("")
    val nameSelectedSpot: LiveData<String>
        get() = _nameSelectedSpot

    private val _descSelectedSpot = MutableLiveData<String>("")
    val descSelectedSpot: LiveData<String>
        get() = _descSelectedSpot

    private val _categorySelectedSpot = MutableLiveData<String>("")
    val categorySelectedSpot: LiveData<String>
        get() = _categorySelectedSpot

    private val _imageUriSelectedSpot = MutableLiveData<String>("")
    val imageUriSelectedSpot: LiveData<String>
        get() = _imageUriSelectedSpot

    fun selectSpot(spotId: String){
        val selectedSpot = spots.find { it.spotId == spotId }
        if (selectedSpot != null) {
            _nameSelectedSpot.value = selectedSpot.name
            _descSelectedSpot.value = selectedSpot.desc
            _categorySelectedSpot.value = selectedSpot.category
            _imageUriSelectedSpot.value = selectedSpot.uri
        }
    }
}