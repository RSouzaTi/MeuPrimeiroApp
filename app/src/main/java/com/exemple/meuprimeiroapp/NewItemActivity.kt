package com.exemple.meuprimeiroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.exemple.meuprimeiroapp.service.Result
import com.exemple.meuprimeiroapp.databinding.ActivityNewItemBinding
import com.exemple.meuprimeiroapp.model.ItemValue
import com.exemple.meuprimeiroapp.service.RetrofitClient
import com.exemple.meuprimeiroapp.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom


class NewItemActivity : BaseMapActivity() {

    private lateinit var binding: ActivityNewItemBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()


    }

    private fun setupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.saveCta.setOnClickListener {
            saveItem()
        }
    }

    private fun saveItem() {
        if (validateForm()) return

        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val itemValue = ItemValue(
                id = id,
                name = binding.name.text.toString(),
                surname = binding.surname.text.toString(),
                age = binding.age.text.toString().toInt(),
                imageUrl = binding.imageUrl.text.toString(),
                profession = binding.profession.text.toString(),
                location = null,
            )
            val result = safeApiCall { RetrofitClient.itemApiService.addItem(itemValue) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success ->  handleOnSuccess()
                    is Result.Error ->    handleONError()

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

        fun newIntent(context: Context): Intent {
            return Intent(context, NewItemActivity::class.java)
        }
    }
}
