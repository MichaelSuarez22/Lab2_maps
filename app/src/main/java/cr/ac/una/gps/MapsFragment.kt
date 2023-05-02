package cr.ac.una.gps

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil


class MapsFragment : Fragment(),OnMapReadyCallback {
    private lateinit var polygon: Polygon
    private val handler = Handler()
    private val updateInterval = 10000L // actualización cada 10 segundos
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationReceiver: BroadcastReceiver
    private var locations = arrayOf(
        LatLng(37.4219999,-122.0840575), // ubicación 1
        LatLng(37.422035,-122.0841162), // ubicación 2
        LatLng(6.2443677,-75.6636144), // ubicación 3}
        LatLng( 40.4168,3.7038)

    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync{
                googleMap ->
            map = googleMap
            getLocation()
            scheduleUpdate()
        }

    }
    override fun onStart() {
        super.onStart()

        // Inicia el sevicio
        val intent = Intent(activity, LocationService::class.java)
        requireActivity().startService(intent)
    }
    override fun onResume() {
        super.onResume()
        val intent = Intent(activity, LocationService::class.java)
        requireActivity().startService(intent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val intent = Intent(activity, LocationService::class.java)
        requireActivity().stopService(intent)
    }
    private fun createPolygon(): Polygon {

        val polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(10.1778124,-84.3994571))
        polygonOptions.add(LatLng( 10.115629,-84.4825412))
        polygonOptions.add(LatLng( 9.9438865,-84.4008304))
        polygonOptions.add(LatLng( 9.9891975,-84.1282322))
        polygonOptions.add(LatLng( 10.1926805,-84.1515781))
        polygonOptions.add(LatLng( 10.1778124,-84.3994571))
        return map.addPolygon(polygonOptions)


    }

    private fun isLocationInsidePolygon(location: LatLng): Boolean {
        return polygon != null && PolyUtil.containsLocation(location, polygon?.points, true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(latitud: Double, longitud:Double)
    }
    private var listener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context debe implementar OnFragmentInteractionListener")
        }
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }


//    private fun iniciaServicio() {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
//        } else {
//            val intent = Intent(context, LocationService::class.java)
//            context?.startService(intent)
//        }
//    }
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Ubicación obtenida con éxito
                if (location != null) {

                    // Agrega un marcador para la ubicación actual
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación actual"))

                    // Agrega un marcador para cada ubicación en el array
                    for (loc in locations) {
                        map.addMarker(MarkerOptions().position(loc).title("Ubicación"))
                        listener?.onFragmentInteraction(loc.latitude,loc.longitude)
                    }

                    // Mueve la cámara al centro de todas las ubicaciones
                    val builder = LatLngBounds.Builder()
                    builder.include(currentLatLng)
                    for (loc in locations) {
                        builder.include(loc)
                    }
                    val bounds = builder.build()
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }
        }

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera

        val sydney = LatLng(-14.0095923, 108.8152324)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        polygon = createPolygon()
        if (isLocationInsidePolygon(sydney)){
            println("+++++++++++++++++++++++++++++++++sidney esta en el mapa" )
        }

        val costaRica = LatLng(9.87466235556157, -83.97806864895828)
        if (!isLocationInsidePolygon(costaRica)){
            println("+++++++++++++++++++++++++++++++++CR no  esta en el mapa" )
        }


    }
    private fun updateLocations() {
        val randomLocations = Array(locations.size) {
            val randomLat = -90.0 + Math.random() * 180.0
            val randomLng = -180.0 + Math.random() * 360.0
            LatLng(randomLat, randomLng)
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación actual"))
            }
        }
        9
        // Agrega un marcador para cada ubicación en el nuevo array
        for (loc in randomLocations) {
            map.addMarker(MarkerOptions().position(loc).title("Ubicación"))
            listener?.onFragmentInteraction(loc.latitude,loc.longitude)
        }

        // Mueve la cámara al centro de todas las ubicaciones
        val builder = LatLngBounds.Builder()
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                builder.include(currentLatLng)
            }
        }
        for (loc in randomLocations) {
            builder.include(loc)
        }
        val bounds = builder.build()
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun scheduleUpdate(){
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateLocations()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    getLocation()
                }
            } else {
                // Permiso denegado, maneja la situación de acuerdo a tus necesidades
            }
        }
    }



}