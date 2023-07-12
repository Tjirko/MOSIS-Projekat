package elfak.mosis.project.data

import com.google.firebase.auth.FirebaseAuth

data class UserData(val displayname: String, val score: Long){
    override fun toString(): String {
        return "$displayname      $score"
    }
}
