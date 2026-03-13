package com.exemple.meuprimeiroapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.exemple.meuprimeiroapp.adapter.ItemAdapter
import com.exemple.meuprimeiroapp.databinding.ActivityMainBinding
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        requestLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupViews() {
        binding.swipeRefreshLayout.setOnRefreshListener {

        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.addCta.setOnClickListener {

        }
        binding.message.setOnClickListener {
            fetchItems()
        }
    }

    private fun requestLocationPermissions() {
        //Inicializa o fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Configura o ActivityResultLauncher para solicitar a permissão de localização
        locationPermissionLauncher =
            registerForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT)
                        .show()

                }
            }
        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }

            shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) -> {
                locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
            }

            else -> {
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val latitude = location.latitude
                val longitude = location.longitude
                // Faça algo com a latitude e longitude
                Toast.makeText(
                    this,
                    "Latitude: $latitude, Longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Falha ao obter a localização", Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.getItems() }
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Success -> handleOnSuccess(result.data)
                    is Result.Error -> { handleOnError()

                    }
                }
            }
        }
    }



    private fun handleOnSuccess(items: List<Item>) {
        if (items.isEmpty()) {
            binding.message.visibility = View.VISIBLE
            binding.message.setText(R.string.no_items)
            binding.recyclerView.visibility = View.GONE
            return
        }
        binding.message.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.recyclerView.adapter = ItemAdapter(items) { item ->
            val intent = ItemDetailActivity.newIntent(this, item.id.toString())
            startActivity(intent)

        }
    }


    private fun handleOnError() {
        binding.message.visibility = View.VISIBLE
        binding.message.setText(R.string.generical_error)
        binding.recyclerView.visibility = View.GONE

    }
}