package cr.ac.una.gps

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationView
import cr.ac.una.gps.adapter.UbicacionAdapter
import cr.ac.una.gps.dao.UbicacionDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, MapsFragment.OnFragmentInteractionListener{

    lateinit var drawerLayout : DrawerLayout
    private lateinit var map: GoogleMap
    private lateinit var ubicacionDao: UbicacionDao

    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)

        var toogle = ActionBarDrawerToggle(

            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawerclose

        )
        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

//        createMapFragment()

        ubicacionDao = AppDatabase.getInstance(this).ubicacionDao()

//        val buttonInsert = findViewById<Button>(R.id.buttonInsert)
//        buttonInsert.setOnClickListener {
//            val entity = Ubicacion(
//                id = null,
//                latitud = 10.0755367,
//                longitud = -84.3237592,
//                fecha = Date()
//            )
//            insertEntity(entity)
//        }

        val listView = findViewById<ListView>(R.id.listUbicaciones)
        var ubicaciones: List<Ubicacion>


        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
                val adapter = UbicacionAdapter(this@MainActivity, ubicaciones)
                listView.adapter = adapter
            }

        }


        }








    override fun onFragmentInteraction(latitud: Double, longitud: Double) {

        val buttonInsert = findViewById<Button>(R.id.buttonInsert)
        buttonInsert.setOnClickListener {
            val entity = Ubicacion(
                id = null,
                latitud = latitud,
                longitud = longitud,
                fecha = Date()
//                area = true



            )
            insertEntity(entity)
        }
    }

    private fun insertEntity(entity: Ubicacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicacionDao.insert(entity)
            }
        }

    }



    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
           REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               if (ActivityCompat.checkSelfPermission(
                       this,
                       android.Manifest.permission.ACCESS_FINE_LOCATION
                   ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                       this,
                       android.Manifest.permission.ACCESS_COARSE_LOCATION
                   ) != PackageManager.PERMISSION_GRANTED
               ) {
                   // TODO: Consider calling
                   //    ActivityCompat#requestPermissions
                   // here to request the missing permissions, and then overriding
                   //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                   //                                          int[] grantResults)
                   // to handle the case where the user grants the permission. See the documentation
                   // for ActivityCompat#requestPermissions for more details.
                   return
               }
               map.isMyLocationEnabled = true
           }else{
               Toast.makeText(this,"ajuste localizacion",Toast.LENGTH_SHORT).show()
           }else -> {}


       }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        lateinit var fragment : Fragment
        when(item.itemId){
            R.id.home -> {
                fragment = homeFragment.newInstance("string1","string2")
            }
            R.id.maps -> {
                fragment = MapsFragment()
            }
            R.id.conf -> {
                fragment = conf_fragment.newInstance("string1","string2")
            }
            R.id.tel -> {
                fragment = conf_fragment.newInstance("string1","string2")
            }
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_content, fragment)
            .commit()
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMapReady(p0: GoogleMap) {
      map = p0
        createMarker()
//        enableLocation()
    }

    private fun createMarker(){

    }

//    private fun createMapFragment(){
//        val mapFragment: SupportMapFragment = supportFragmentManager
//            .findFragmentById(R.id.map2) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

//    override fun onFragmentInteraction(latitud: Double, longitud: Double) {
//        val buttonInsert = findViewById<Button>(R.id.buttonInsert)
//        buttonInsert.setOnClickListener {
//            val entity = Ubicacion(
//                id = null,
//                latitud = 10.0755367,
//                longitud = -84.3237592,
//                fecha = Date()
//            )
//            insertEntity(entity)
//        }
//    }

//    private fun enableLocation(){
//        if(!::map.isInitialized) return
//        if(isLocationPermissionGranted()){
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            map.isMyLocationEnabled = true
//        }else{
////            requestLocationPermission()
//        }
//    }

//    private fun requestLocationPermission(){
//        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
//            Toast.makeText(this,"acepta permiso",Toast.LENGTH_SHORT).show()
//        }else{
//            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_CODE_LOCATION)
//        }
//    }

}
