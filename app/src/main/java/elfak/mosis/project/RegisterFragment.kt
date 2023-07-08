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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import elfak.mosis.project.databinding.FragmentRegisterBinding
import elfak.mosis.project.model.UserDataModel
import java.io.File
import java.text.DateFormat
import java.util.Date

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val userDataModel: UserDataModel by viewModels()
    private lateinit var pickMedia: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private var imageUriTemp: Uri? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

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


        binding.btnRegister.setOnClickListener {
            val email = view.findViewById<TextInputEditText>(R.id.username_register).text.toString()
            val password = view.findViewById<TextInputEditText>(R.id.password_register).text.toString()
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("EmailPassword", "createUserWithEmail:success")
                        findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("EmailPassword", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            context,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

        }
    }

    override fun onStart() {
        super.onStart()
        binding.userImageRegister.setOnClickListener{
            /*if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                val perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                requestMultiplePermissionsLauncher.launch(perms)
            }
            else {*/
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
            //}


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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}