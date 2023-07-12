package elfak.mosis.project

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import elfak.mosis.project.data.SpotsData
import elfak.mosis.project.databinding.FragmentMapBinding
import elfak.mosis.project.model.FilterViewModel
import elfak.mosis.project.model.LocationViewModel
import elfak.mosis.project.model.SpotsViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
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
    private lateinit var map: MapView
    private val locationViewModel : LocationViewModel by activityViewModels()
    private val spotsViewModel: SpotsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private lateinit var firestore: FirebaseFirestore
    private  var location: Location?= null

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
        if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                    isGranted:Boolean->
                if(isGranted){
                    setMyLocationOverlay()
                    setOnMapClickOverlay()
                    LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener {
                        location = it
                    }
                }
            }.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            setMyLocationOverlay()
            setOnMapClickOverlay()
            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener {
                location = it
            }
        }
        map.controller.setZoom(15.0)
        val startPoint = GeoPoint(43.3209, 21.8958)
        map.controller.setCenter(startPoint)

        firestore.collection("spots")
            .get()
            .addOnSuccessListener { result ->
                spotsViewModel.spots.clear()
                for (doc in result!!) {
                    val loc = doc.getGeoPoint("location")
                    val spot = doc.getString("name")?.let { SpotsData(doc.id, it,
                        doc.getString("description")!!, doc.getString("category")!!,
                        loc!!.latitude, loc!!.longitude, doc.getString("imageUri")!!
                    )}
                    if (spot != null) {
                        spotsViewModel.spots.add(spot)
                    }
                }
                for (spot in filterSpots()){
                    val marker = Marker(map)
                    marker.id = spot.spotId
                    marker.position = GeoPoint(spot.lat, spot.long)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = spot.name
                    marker.snippet = spot.category
                    marker.subDescription = spot.desc
                    marker.setOnMarkerClickListener{ mark, _ ->
                        spotsViewModel.selectSpot(mark.id)
                        findNavController().navigate(R.id.action_MapFragment_to_ShowSpotFragment)
                        true
                    }
                    map.overlays.add(marker)
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
    /*private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if (isGranted){
                setMyLocationOverlay()
                setOnMapClickOverlay()
            }
        }*/
    private fun filterSpots() : ArrayList<SpotsData>{
        var filteredSpots = ArrayList<SpotsData>()
        for (spot in spotsViewModel.spots) {
            var distance = FloatArray(1)
            if (location != null) {
                Location.distanceBetween(
                    location!!.latitude,
                    location!!.longitude,
                    spot.lat,
                    spot.long,
                    distance
                )
            }else{
                distance[0] = 0.0F
            }
            if ((filterViewModel.category.value == ""
                        || filterViewModel.category.value  == "Sve"
                        || filterViewModel.category.value == spot.category)
                && distance[0]/1000 < filterViewModel.radius.value!!){
                filteredSpots.add(spot.copy())
            }
        }

        return filteredSpots
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