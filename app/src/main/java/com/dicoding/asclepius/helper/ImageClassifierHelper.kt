package com.dicoding.asclepius.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResults: Int = 3,
    val modelName: String = "cancer_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private lateinit var classifier: ImageClassifier

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            results: List<Classifications>?,
            inferenceTime: Long
        )
    }

    init {
        setupImageClassifier() // Initialize the classifier upon object creation
    }

    private fun setupImageClassifier() {
        try {
            // Initialize BaseOptions for configuring the classifier
            val baseOptions = BaseOptions.builder()
                .setNumThreads(4) // Set the number of threads to be used for inference
                .build()

            // Configure the ImageClassifier options
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(maxResults) // Limit to top N results
                .setScoreThreshold(threshold) // Minimum confidence threshold
                .build()

            // Create the ImageClassifier with the specified options
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                options
            )

            println("Image Classifier setup successfully.")

        } catch (e: Exception) {
            classifierListener?.onError("Error setting up image classifier: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    fun classifyStaticImage(imageUri: Uri, contentResolver: ContentResolver) {
        try {
            // Load the image from the URI
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            // Resize bitmap to the correct input size (e.g., 224x224 for MobileNetV3)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // Create a TensorImage from the resized bitmap
            val tensorImage = TensorImage.fromBitmap(resizedBitmap)

            // Run inference using the classifier
            val classifications = classifier.classify(tensorImage)

            // Assuming only one classification is done, extract the result
            val topResult = classifications.firstOrNull()?.categories?.firstOrNull()
            val label = topResult?.label ?: "Unknown"
            val confidence = topResult?.score ?: 0f

            println("Predicted Cancer: $label, Confidence: ${confidence * 100}%")

            // Notify the listener of the results
            classifierListener?.onResults(classifications, 0) // Pass results as needed

        } catch (e: Exception) {
            classifierListener?.onError("Error classifying image: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}