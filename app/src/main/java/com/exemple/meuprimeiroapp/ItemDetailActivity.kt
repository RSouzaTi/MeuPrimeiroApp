package com.exemple.meuprimeiroapp

import com.exemple.meuprimeiroapp.model.Item
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.exemple.meuprimeiroapp.databinding.ActivityItemDetailBinding
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.safeApiCall
import com.exemple.meuprimeiroapp.ui.loadUrl
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
        setupView()
        loadItem()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Se o item já foi carregado pela API antes do mapa ficar pronto, posiciona o marcador
        if (::item.isInitialized) {
            loadItemInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.deleteCTA.setOnClickListener { deleteItem() }
        binding.editCTA.setOnClickListener { editItem() }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                // Buscamos o item individual pelo ID
                safeApiCall { RetrofitClient.itemApiService.getItem(itemId) }
            }

            when (result) {
                is Result.Success -> {
                    item = result.data
                    handleSuccess()
                }
                is Result.Error -> {
                    Toast.makeText(this@ItemDetailActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess() {
        // AJUSTE CRÍTICO: Pegamos os dados de dentro de '.value'
        val data = item.value

        android.util.Log.d("RESPOSTA_API", "Nome: ${data.name}, URL: ${data.imageUrl}")

        // Preenche os campos da UI
        binding.name.text = data.fullName
        binding.profession.setText(data.profession)
        binding.image.loadUrl(data.imageUrl)

        loadItemInGoogleMap()
    }

    private fun loadItemInGoogleMap() {
        if (!::mMap.isInitialized) return

        // AJUSTE: Localização também está dentro de .value
        item.value.location?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(location).title(it.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun deleteItem() {
        lifecycleScope.launch {
            binding.deleteCTA.isEnabled = false
            val result = withContext(Dispatchers.IO) {
                safeApiCall { RetrofitClient.itemApiService.deleteItem(item.id) }
            }

            if (result is Result.Success) {
                Toast.makeText(this@ItemDetailActivity, "Item removido com sucesso", Toast.LENGTH_LONG).show()
                finish()
            } else {
                binding.deleteCTA.isEnabled = true
                Toast.makeText(this@ItemDetailActivity, "Erro ao deletar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editItem() {
        lifecycleScope.launch {
            binding.editCTA.isEnabled = false

            // AJUSTE: Ao editar, criamos uma cópia do ItemValue atualizado
            val updatedValue = item.value.copy(
                profession = binding.profession.text.toString()
            )
            // E montamos o Item novamente com esse value
            val updatedItem = item.copy(value = updatedValue)

            val result = withContext(Dispatchers.IO) {
                safeApiCall { RetrofitClient.itemApiService.updateItem(item.id, updatedItem) }
            }

            if (result is Result.Success) {
                Toast.makeText(this@ItemDetailActivity, "Profissão atualizada!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.editCTA.isEnabled = true
                Toast.makeText(this@ItemDetailActivity, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
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