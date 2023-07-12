package elfak.mosis.project

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import elfak.mosis.project.databinding.FragmentRegisterBinding
import elfak.mosis.project.databinding.FragmentShowSpotBinding
import elfak.mosis.project.model.SpotsViewModel


class ShowSpotFragment : Fragment() {

    private var _binding: FragmentShowSpotBinding? = null
    private val spotsViewModel: SpotsViewModel by activityViewModels()
    private lateinit var storageRef: StorageReference

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentShowSpotBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNameShowSpot.text = spotsViewModel.nameSelectedSpot.value
        binding.tvCategoryShowSpot.text = spotsViewModel.categorySelectedSpot.value
        binding.tvDescShowSpot.text = spotsViewModel.descSelectedSpot.value


        binding.ivImageShowSpot.load(Uri.parse(spotsViewModel.imageUriSelectedSpot.value))

        binding.btnCloseShowSpot.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}