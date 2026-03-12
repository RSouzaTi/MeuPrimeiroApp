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
    private var item: Item? = null
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        loadItem()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (item != null) {
            // Se  o item já estiver carregado, carregueo no mapa
            loadItemInGoogleMap()
        }
    }
    private fun setupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
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
                            "Erro ao buscar o item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess() {
        val currentItem = item ?: return
        binding.name.text = currentItem.value.fullName
        binding.age.text = getString(R.string.item_age, currentItem.value.age)
        binding.profession.setText(currentItem.value.profession)
        binding.image.loadUrl(currentItem.value.imageUrl)

        loadItemInGoogleMap()
    }

    private fun loadItemInGoogleMap() {
        item?.value?.location?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.latitude, it.longitude)
            mMap?.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(it.name)
            )
            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    15f
                )
            )
        }
    }
    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.deleteItem(item!!.value.id) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success ->  handleSuccessDelete()
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            "Erro ao deletar o item",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()

                    }
                }
            }
        }
    }

    private fun handleSuccessDelete() {
        Toast.makeText(
            this@ItemDetailActivity,
            "Item deletado com sucesso",
            Toast.LENGTH_SHORT
        ).show()
        finish()
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
