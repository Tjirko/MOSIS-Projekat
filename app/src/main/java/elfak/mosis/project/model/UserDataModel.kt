package elfak.mosis.project.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserDataModel : ViewModel() {
    var fauth : FirebaseAuth = Firebase.auth
}