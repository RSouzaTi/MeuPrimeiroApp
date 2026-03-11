package com.exemple.meuprimeiroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exemple.meuprimeiroapp.databinding.ActivityItemDetailBinding
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.ui.loadUrl


class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding

    private lateinit var item: Item




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SetupViews()
        loadItem()


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
            val result = RetrofitClient.itemApiService.getItems(itemId)
            safeApiCall { result }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success<*>  -> {
                        item = result.data as Item
                        handleOnSuccess()
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

    private fun handleOnSuccess() {
        binding.name.text = item.value.fullName
        binding.age.text = getString(R.string.item_age, item.value.age)
        binding.profession.setText(item.value.profession)
        binding.image.loadUrl(item.value.imageUrl)

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