package elfak.mosis.project.data

data class SpotsData(val spotId: String, val name: String, val desc: String,
                     val category: String, val lat: Double, val long: Double, val uri: String){
    override fun toString(): String = name
}

