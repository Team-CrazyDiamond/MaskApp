package com.websarva.wings.android.maskapp.screens.detection

import android.annotation.SuppressLint
import android.app.ActionBar
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.websarva.wings.android.maskapp.R
import com.websarva.wings.android.maskapp.databinding.FragmentDetectionBinding
import kotlinx.android.synthetic.main.fragment_detection.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DetectionFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewSize: Size
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDetectionBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_detection, container, false
        )

        val arguments = DetectionFragmentArgs.fromBundle(requireArguments())

        val viewModelFactory = arguments.language?.let { DetectionViewModelFactory(it) }
        val detectionViewModel = viewModelFactory?.let { ViewModelProvider(this, it) }?.get(DetectionViewModel::class.java)

        // mediaPlayerの初期化
        mediaPlayer = if (arguments.language == "Japanese") {
            MediaPlayer.create(requireContext(), R.raw.japanese_ver)
        } else {
            MediaPlayer.create(requireContext(), R.raw.mask)
        }

        binding.lifecycleOwner = this
        binding.detectionViewModel = detectionViewModel

        // カメラ処理
        startCamera(binding = binding)

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }


    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    private fun startCamera(binding: FragmentDetectionBinding) {

        // カメラのライフサイクルをライフサイクルの所有者にバインド
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // カメラのライフサイクルをLifecycleOwnerアプリケーションのプロセス内にバインドするために使用
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // xmlのviewFinderにpreviewを設定
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                        val machineLearning = MachineLearning(
                            context = requireContext(),
                            binding = binding,
                            mediaPlayer = mediaPlayer
                        )
                        machineLearning.faceDetection(
                            imageProxy = imageProxy,
                            previewSize = previewSize
                        )
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // カメラのライフサイクルにLifecycleOwnerアプリケーションをバインド
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                previewSize = preview.attachedSurfaceResolution ?: Size(0, 0)
                Log.i("previewSize", "width: ${previewSize.width}, height: ${previewSize.height}")
            } catch (exc: Exception) {
                Log.e("CameraXBasic", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mediaPlayer.release()
    }

}