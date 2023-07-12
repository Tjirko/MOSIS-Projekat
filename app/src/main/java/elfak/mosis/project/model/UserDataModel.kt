package elfak.mosis.project.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import elfak.mosis.project.data.SpotsData
import elfak.mosis.project.data.UserData

class UserDataModel : ViewModel() {
    var users = ArrayList<UserData>()
}