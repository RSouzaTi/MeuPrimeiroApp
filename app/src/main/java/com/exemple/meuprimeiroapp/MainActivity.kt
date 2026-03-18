package com.exemple.meuprimeiroapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meuprimeiroapp.database.DatabaseBuilder
import com.example.meuprimeiroapp.database.model.UserLocation
import com.exemple.meuprimeiroapp.adapter.ItemAdapter
import com.exemple.meuprimeiroapp.databinding.ActivityMainBinding
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
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
        setupView()
        requestLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_loggout -> {
                onLoggout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLoggout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.addCta.setOnClickListener {
            navigateToNewItem()
        }
        binding.message.setOnClickListener {
            fetchItems()
        }
    }

    private fun navigateToNewItem() {
        startActivity(NewItemActivity.newIntent(this))
    }

    private fun requestLocationPermission() {
        // Inicializa o FusedLocationPermission
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura o ActivityResultLauncher para solicitar a permissão de localização
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show()
            }
        }

        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED -> {
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
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val latitude = location.latitude
                val longitude = location.longitude
                CoroutineScope(Dispatchers.IO).launch {
                    val userLocation = UserLocation(
                        latitude = latitude,
                        longitude = longitude
                    )
                    DatabaseBuilder.getInstance(applicationContext)
                        .userLocationDao()
                        .insert(userLocation)
                }
            } else {
                Toast.makeText(this, getString(R.string.cannot_get_location), Toast.LENGTH_SHORT).show()
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
                    is Result.Error -> handleOnError()
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

    companion object {

        fun newIntent(context: Context) =
            Intent(context, MainActivity::class.java)

    }
}