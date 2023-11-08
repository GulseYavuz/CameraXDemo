package com.yavuz.cameraxdemo

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.yavuz.cameraxdemo.databinding.FragmentCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var binding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = binding!!
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var mediaStoreUtils: MediaStoreUtils
    private lateinit var broadcastManager: LocalBroadcastManager

   /* private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }*/
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    fragmentCameraBinding.root,
                    "The camera permission is required",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.id) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)

       /* (activity as MainActivity?)!!.takePhoto()
        (activity as MainActivity?)!!.animateFlash()
*/


        return fragmentCameraBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        broadcastManager = LocalBroadcastManager.getInstance(view.context)
        mediaStoreUtils = MediaStoreUtils(requireContext())

        mediaStoreUtils = MediaStoreUtils(requireContext())
       // displayManager.registerDisplayListener(displayListener, null)
        cameraPermissionResult.launch(android.Manifest.permission.CAMERA)


        fragmentCameraBinding.buttonCapture.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateFlash()
            }

        }
    }
    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(fragmentCameraBinding.preview.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }catch (e: Exception){
                Log.d(TAG, "Use case binding failed.")
            }
        }, ContextCompat.getMainExecutor(this.requireContext()))

    }
     fun takePhoto() {
         val imageCapture = imageCapture ?: return

            val name = SimpleDateFormat(FILENAME, Locale.US)
                .format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    val appName = requireContext().resources.getString(R.string.app_name)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
                }
            }
            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(requireContext().contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions, imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri
                        if(savedUri != null){
                            val imagePath = savedUri.path
                            Log.d(TAG, "Photo capture succeeded: $imagePath")

                        }else{
                            Log.d(TAG, "Resim kaydedilirken bir hata oluştu.")
                        }
                 /*
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            // Suppress deprecated Camera usage needed for API level 23 and below
                            @Suppress("DEPRECATION")
                            requireActivity().sendBroadcast(
                                Intent(Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }*/
                    }
                })

    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun animateFlash(){
        fragmentCameraBinding.root.postDelayed({
            fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
            fragmentCameraBinding.root.postDelayed({
                fragmentCameraBinding.root.foreground = null
            }, 50)
        }, 100)
    }
    companion object{
        private const val TAG = "CameraFragment"
        private const val PHOTO_TYPE = "image/jpeg"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"

    }
}