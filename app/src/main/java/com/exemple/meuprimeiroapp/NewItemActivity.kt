package com.exemple.meuprimeiroapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.exemple.meuprimeiroapp.databinding.ActivityNewItemBinding
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.model.ItemLocation
import com.exemple.meuprimeiroapp.model.ItemValue // AJUSTE: Importamos o ItemValue
import com.exemple.meuprimeiroapp.service.Result
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private lateinit var imageUri: Uri
    private var imageFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.imageUrl.setText("Imagem capturada (pronta para upload)")
            // Como takePictureCta é um ImageView no seu XML, não usamos .text
        } else {
            imageFile = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions().position(latLng).title("Localização Selecionada")
            )
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.saveCta.setOnClickListener { saveItem() }
        binding.takePictureCta.setOnClickListener { takePicture() }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // --- CÂMERA E LOCALIZAÇÃO ---
    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile!!)
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        if (!::mMap.isInitialized) return
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }

    // --- SALVAMENTO ---
    private fun saveItem() {
        if (!validateForm()) return
        if (imageFile != null) uploadImageToFirebase() else saveData()
    }

    private fun uploadImageToFirebase() {
        imageFile?.let { file ->
            onLoadImage(true)
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        binding.imageUrl.setText(uri.toString())
                        onLoadImage(false)
                        saveData()
                    }
                }
                .addOnFailureListener {
                    onLoadImage(false)
                    Toast.makeText(this, "Falha no upload", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveData() {
        val name = binding.name.text.toString()
        val itemPosition = selectedMarker?.position?.let {
            ItemLocation(name = name, latitude = it.latitude, longitude = it.longitude)
        }

        lifecycleScope.launch {
            binding.saveCta.isEnabled = false

            // AJUSTE CRÍTICO: Gerando o ID e o ItemValue para bater com o seu Node
            val generatedId = SecureRandom().nextInt(1000).toString().padStart(3, '0')

            val newItemValue = ItemValue(
                id = generatedId,
                name = name,
                surname = binding.surname.text.toString(),
                profession = binding.profession.text.toString(),
                age = binding.age.text.toString().toIntOrNull() ?: 0,
                imageUrl = binding.imageUrl.text.toString(),
                location = itemPosition
            )

            // Criamos o objeto Item que contém o value (conforme seu Logcat)
            val newItem = Item(id = generatedId, value = newItemValue)

            val result = withContext(Dispatchers.IO) {
                safeApiCall { RetrofitClient.itemApiService.addItem(newItem) }
            }

            when (result) {
                is Result.Success -> handleOnSuccess()
                is Result.Error -> handleOnError()
            }
            binding.saveCta.isEnabled = true
        }
    }

    private fun onLoadImage(isLoading: Boolean) {
        binding.loadImageProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveCta.isEnabled = !isLoading
    }

    private fun handleOnError() {
        Toast.makeText(this, "Erro ao adicionar item", Toast.LENGTH_SHORT).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(this, "Sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun validateForm(): Boolean {
        var hasError = false
        val fields = listOf(binding.name, binding.surname, binding.age, binding.imageUrl, binding.profession)
        fields.forEach { if (it.text.isNullOrBlank()) { it.error = "Campo obrigatório"; hasError = true } }
        return !hasError
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
    }
}