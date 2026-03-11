package com.exemple.meuprimeiroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exemple.meuprimeiroapp.databinding.ActivityItemDetailBinding
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.safeApiCall
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityItemDetailBinding

    private lateinit var item: Item

    private lateinit var mMap: GoogleMap




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SetupViews()
        loadItem()
        setupGoogleMap()


    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }
    private fun SetupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

    }
    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.getItem(itemId) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        item = result.data
                        handleSuccess()
                    }
                    is Result.Error -> {
                            Toast.makeText(
                            this@ItemDetailActivity,
                            "Erro ao buscar o getItem(itemId)",
                            Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync (this)
    }

    private fun handleSuccess() {
        binding.name.text = item.value.fullName
        binding.age.text = getString(R.string.item_age, item.value.age)
        binding.profession.setText(item.value.profession)
        binding.image.loadUrl(item.value.imageUrl)
        loadMapInGoogleMap()

    }

    private fun loadMapInGoogleMap(){
        item.value.location?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.latitude, it.longitude)
            mMap.addMarker(
                MarkerOptions()
                .position(location)
                .title(it.name)
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                location,
                15f
            ))

        }

   }


    companion object {
        const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }
}