package elfak.mosis.project.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel: ViewModel() {
    private val _category = MutableLiveData<String>("")
    val category: LiveData<String>
        get() = _category

    private val _radius = MutableLiveData<Int>(10)
    val radius: LiveData<Int>
        get() = _radius

    fun setFilter(category: String, radius: Int){
        _category.value = category
        _radius.value = radius
    }
}