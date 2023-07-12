package elfak.mosis.project

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import elfak.mosis.project.databinding.FragmentEditSpotBinding
import elfak.mosis.project.databinding.FragmentLoginBinding
import elfak.mosis.project.databinding.FragmentRegisterBinding
import elfak.mosis.project.model.LocationViewModel
import java.io.File
import java.text.DateFormat
import java.util.Calendar
import java.util.Date


class EditSpotFragment : Fragment() {

    private var _binding: FragmentEditSpotBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private var imageUriTemp: Uri? = null
    private lateinit var pickMedia: ActivityResultLauncher<Intent>
    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val locationViewModel : LocationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditSpotBinding.inflate(inflater, container, false)

        pickMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    if(result.data!=null && result.data!!.data !=null){
                        imageUri = result.data!!.data as Uri
                        binding.userImageRegister.setImageURI(imageUri)
                    }else{
                        imageUri = imageUriTemp
                        binding.userImageRegister.setImageURI(imageUriTemp)
                    }
                    Log.d("PhotoPicker", "Selected URI: $imageUri")
                }
                else -> {
                    Log.d("PhotoPicker", "Otkazano")
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val latObserver = Observer<String>{ newValue ->
            binding.etLatEditSpot.setText(newValue)
        }
        locationViewModel.latitude.observe(viewLifecycleOwner, latObserver)

        val longObserver = Observer<String>{ newValue ->
            binding.etLongEditSpot.setText(newValue)
        }
        locationViewModel.longitude.observe(viewLifecycleOwner, longObserver)

        binding.btnSetCurrEditSpot.setOnClickListener{
            locationViewModel.setLocation = true
            findNavController().navigate(R.id.action_EditSpotFragment_to_MapFragment)
        }

        val spotCategories = resources.getStringArray(R.array.spot_categories)
        val adapter = ArrayAdapter(requireContext(), R.layout.category_dropdown_item, spotCategories)
        binding.edmCategoryEditSpot.setAdapter(adapter)

        binding.btnBackEditSpot.setOnClickListener{
            locationViewModel.setLocation("","")
            findNavController().popBackStack()

        }
        binding.userImageRegister.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                val perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                requestMultiplePermissionsLauncher.launch(perms)
            }
            else {
                getImage()
            }
        }
        binding.btnAddEditSpot.setOnClickListener{
            val spotId = firestore.collection("posts").document().id
                val spotHM = hashMapOf(
                    "location" to GeoPoint(binding.etLatEditSpot.text.toString().toDouble(),binding.etLongEditSpot.text.toString().toDouble()),
                    "name" to binding.etNameEditSpot.text.toString(),
                    "description" to binding.etDescEditSpot.text.toString(),
                    "category" to binding.edmCategoryEditSpot.text.toString(),
                    "imageUri" to "",
                    "author" to firestore.collection("users").document(auth.currentUser!!.uid),
                    "updated" to Calendar.getInstance().time
                )

                firestore.collection("spots").document(spotId).set(spotHM).addOnSuccessListener {
                    firestore.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener{
                        firestore.collection("users").document(auth.currentUser!!.uid).update("score",
                            it.data!!["score"] as Int + 1)
                    }
                    storageRef =
                        Firebase.storage.getReference("Images/SpotImages/$spotId")
                    imageUri?.let { it1 -> storageRef.putFile(it1) }?.addOnSuccessListener {
                        storageRef.downloadUrl.addOnCompleteListener {
                            firestore.collection("spots").document(spotId).update("imageUri", it.result).addOnCompleteListener{task ->
                                if(!task.isSuccessful){
                                    Log.d("UpdateSpotImage",task.exception.toString())
                                    Log.d("UpdateSpotImage",task.result.toString())
                                }
                            }
                        }
                    locationViewModel.setLocation("","")
                    findNavController().popBackStack()
                }

            }
        }

    }
    private fun getImage(){

        val selectImageIntent = Intent(Intent.ACTION_PICK)
        selectImageIntent.type = "image/*"
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val path = File(requireActivity().filesDir, "Pictures")
        if (!path.exists()) path.mkdirs()
        val image = File(path, "img_${
            DateFormat.getDateTimeInstance().format(Date())
        }.jpg")
        imageUriTemp = FileProvider.getUriForFile(requireActivity(), "com.elfak.mosis.project.fileprovider", image)
        captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriTemp)

        val pickIntent = Intent(Intent.ACTION_CHOOSER)
        pickIntent.putExtra(Intent.EXTRA_TITLE, "Get picture from:")
        pickIntent.putExtra(Intent.EXTRA_INTENT, selectImageIntent)
        val intentArray = arrayOf(captureImageIntent)
        pickIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        pickMedia.launch(pickIntent)

    }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                isGranted ->
            if (!isGranted.containsValue(false)){
                getImage()
            }
        }

}