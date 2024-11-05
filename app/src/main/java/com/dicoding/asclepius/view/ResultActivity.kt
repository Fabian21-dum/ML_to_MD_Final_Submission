package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from Intent

        val confidence = intent.getFloatExtra(EXTRA_CONFIDENCE, 0f)
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)

        // Display data
        imageUriString?.let {
            val imageUri = Uri.parse(it) // Convert string back to Uri
            binding.resultImage.setImageURI(imageUri) // Display the image in ImageView
        } ?: run {
            // Optionally handle the case where the image URI is null
            // You can set a placeholder image if desired
            binding.resultImage.setImageResource(R.drawable.ic_place_holder) // Replace with your placeholder
        }
        binding.resultText.text = "Cancer: ${"%.2f".format(confidence * 100)}%"

    }

    companion object {

        const val EXTRA_CONFIDENCE = "extra_confidence"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
    }


