package elfak.mosis.project

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import elfak.mosis.project.databinding.FragmentMapBinding
import elfak.mosis.project.model.UserDataModel
import org.osmdroid.views.MapView
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import elfak.mosis.project.model.LocationViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow.closeAllInfoWindowsOn
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val userDataModel: UserDataModel by viewModels()
    private lateinit var map: MapView
    private val locationViewModel : LocationViewModel by activityViewModels()
    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var contx = activity?.applicationContext
        org.osmdroid.config.Configuration.getInstance().load(contx,
            contx?.let { PreferenceManager.getDefaultSharedPreferences(it) })
        map = requireView().findViewById(R.id.map)

        map.setMultiTouchControls(true)
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            setMyLocationOverlay()
            setOnMapClickOverlay()
        }
        map.controller.setZoom(15.0)
        val startPoint = GeoPoint(43.3209, 21.8958)
        map.controller.setCenter(startPoint)

        firestore.collection("spots")
            .get()
            .addOnSuccessListener { result ->
                /*if (e != null) {
                    Log.w("Listen failed.", e)
                    return@addSnapshotListener
                }*/
                for (doc in result!!) {
                    val marker = Marker(map)
                    val loc = doc.getGeoPoint("location")
                    if (loc != null) {
                        marker.position = GeoPoint(loc.latitude, loc.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = doc.getString("name")
                        marker.snippet = doc.getString("category")
                        marker.subDescription = doc.getString("description")
                        doc.reference.id
                        if (doc.getString("imageUri")?.isNotEmpty() == true){
                            /*val inputStream =  requireActivity().contentResolver.openInputStream((Uri.parse(doc.getString("imageUri"))))
                            val drawable = Drawable.createFromStream(inputStream, doc.getString("imageUri"))
                            marker.image = drawable*/
                        }

                        map.overlays.add(marker)
                    }


                }
            }
        }

    private fun setMyLocationOverlay(){
        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)
    }
    private fun setOnMapClickOverlay(){
        var receive =object : MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                closeAllInfoWindowsOn(map)
                if (locationViewModel.setLocation){
                    var lat = p?.latitude.toString()
                    var long = p?.longitude.toString()
                    locationViewModel.setLocation(lat,long)
                    findNavController().popBackStack()
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        var overlayEvents = MapEventsOverlay(receive)
        map.overlays.add(overlayEvents)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if (isGranted){
                setMyLocationOverlay()
                setOnMapClickOverlay()
            }
        }
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}