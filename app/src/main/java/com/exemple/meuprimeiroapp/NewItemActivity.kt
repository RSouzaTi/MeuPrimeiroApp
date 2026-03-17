package com.exemple.meuprimeiroapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.databinding.ActivityNewItemBinding
import com.exemple.meuprimeiroapp.model.ItemLocation
import com.exemple.meuprimeiroapp.model.ItemValue
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewItemActivity : BaseMapActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding

    private lateinit var mMap: GoogleMap

    private var selectedMarker: Marker? = null

    private lateinit var imageUri: Uri


    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        requestLocationPermission()
        setupGoogleMap()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("lat: ${latLng.latitude}, long: ${latLng.longitude}")
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    loadCurrentLocation()

                } else {
                    Toast.makeText(
                        this,
                        R.string.location_permission_required,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()

                }
            }
        }

    }

    private fun setupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener { saveItem() }
        binding.takePictureCta.setOnClickListener { takePicture() }
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) { openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }

    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir)

        return FileProvider.getUriForFile(
            this,
            "com.exemple.meuprimeiroapp.fileprovider",
            imageFile
        )
    }

    private val cameraLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                binding.imageUrl.setText("imagem capturada")

            }
        }


    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //Se o usuario permitir a localização, obtenha a ultima localização, caso contrário, seguimos sem localização exata
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                if (::mMap.isInitialized) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
                }
            }
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun getDeviceLocation() {
        // verifica se a permissão de localização já foi concedida
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            loadCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        if (::mMap.isInitialized) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isCompassEnabled = true
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

        }

    }

    private fun saveItem() {
        if (!validateForm()) return
        val name = binding.name.text.toString()
        val itemPosition = selectedMarker?.position?.let {
            ItemLocation(
                name = name,
                it.latitude,
                it.longitude
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val itemValue = ItemValue(
                id = id,
                name = name,
                surname = binding.surname.text.toString(),
                age = binding.age.text.toString().toInt(),
                imageUrl = binding.imageUrl.text.toString(),
                profession = binding.profession.text.toString(),
                location = itemPosition
            )
            val result = safeApiCall { RetrofitClient.itemApiService.addItem(itemValue) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleOnSuccess()
                    is Result.Error -> handleONError()

                }
            }

        }

    }


    private fun handleONError() {
        Toast.makeText(
            this@NewItemActivity,
            R.string.error_add_item,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(
            this@NewItemActivity,
            R.string.success_add_item,
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }


    private fun validateForm(): Boolean {
        var hasError = false
        if (binding.name.text.toString().isBlank()) {
            binding.name.error = getString(R.string.requerid_field)
            hasError = true
        }

        if (binding.surname.text.toString().isBlank()) {
            binding.surname.error = getString(R.string.requerid_field)
            hasError = true

        }

        if (binding.age.text.toString().isBlank()) {
            binding.age.error = getString(R.string.requerid_field)
            hasError = true

        }

        if (binding.imageUrl.text.toString().isBlank()) {
            binding.imageUrl.error = getString(R.string.requerid_field)
            hasError = true
        }

        if (binding.profession.text.toString().isBlank()) {
            binding.profession.error = getString(R.string.requerid_field)
            hasError = true
        }
        return if (hasError) false else true
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002


        fun newIntent(context: Context): Intent {
            return Intent(context, NewItemActivity::class.java)
        }
    }
}
