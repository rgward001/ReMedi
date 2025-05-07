package com.example.remedi

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import java.io.File
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.ImageButton
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// Activity to handle camera preview and photo capture
class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView // UI element to display camera preview
    private lateinit var imageCapture: ImageCapture // Object to capture images
    private lateinit var outputDirectory: File // Directory to save captured images
    private val cameraPermission = Manifest.permission.CAMERA
    private val requestCode = 10 // Request code for permission handling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        outputDirectory = getOutputDirectory()

        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            startCamera() // Start camera if permission granted
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCode)
        }

        val captureButton: ImageButton = findViewById(R.id.capture_button)
        captureButton.setOnClickListener {
            takePhoto() // Capture photo on button click
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate back to home activity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Starts the camera preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // Use back camera

            try {
                cameraProvider.unbindAll() // Unbind any previously bound use cases
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture) // Bind camera lifecycle
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera initialization failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Captures a photo and sends its URI to the next activity
    private fun takePhoto() {
        val photoFile = File(outputDirectory, "IMG_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@CameraActivity, "Photo saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()

                    val imageUri = Uri.fromFile(photoFile) // Convert file to URI

                    // Launch PrescriptionFormActivity and pass image URI
                    val intent = Intent(this@CameraActivity, PrescriptionFormActivity::class.java)
                    intent.putExtra("imageUri", imageUri.toString()) // Pass URI as string
                    startActivity(intent)
                    finish()
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Returns the directory to store captured photos
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "CameraApp").apply { mkdirs() } // Create subdirectory if it doesn't exist
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    // Handles the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == this.requestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera() // Permission granted, start camera
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
