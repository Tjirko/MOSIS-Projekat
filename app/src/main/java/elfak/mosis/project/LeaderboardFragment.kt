package elfak.mosis.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import elfak.mosis.project.data.SpotsData
import elfak.mosis.project.data.UserData
import elfak.mosis.project.databinding.FragmentLeaderboardBinding
import elfak.mosis.project.model.SpotsViewModel
import elfak.mosis.project.model.UserDataModel

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private val userDataModel: UserDataModel by activityViewModels()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userDataModel.users.clear()
                for (doc in result!!) {
                    userDataModel.users.add(
                        UserData(
                            doc.getString("displayName")!!,
                            doc.get("score") as Long
                        )
                    )
                }
                    binding.lvUsersList.adapter = ArrayAdapter(
                        view.context,
                        android.R.layout.simple_list_item_1,
                        userDataModel.users.sortedBy { s -> s.score })
            }

    }

}