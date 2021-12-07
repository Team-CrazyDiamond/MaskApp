package com.websarva.wings.android.maskapp.screens.detection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.ml.custom.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.websarva.wings.android.maskapp.databinding.FragmentDetectionBinding
import java.io.ByteArrayOutputStream

class MachineLearning(val context: Context, val binding: FragmentDetectionBinding, val mediaPlayer: MediaPlayer) {

    private val viewFinder: PreviewView = binding.viewFinder

    companion object {
        private val localModel = FirebaseCustomLocalModel.Builder()
            .setAssetFilePath("mask_detector.tflite")
            .build()

        //インタープリター
        private val options = FirebaseModelInterpreterOptions.Builder(localModel).build()
        val interpreter = FirebaseModelInterpreter.getInstance(options)

        //インタープリターの入力と出力の形式，サイズを指定
        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 2))
            .build()
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun faceDetection(imageProxy: ImageProxy, previewSize: Size) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val detector = FaceDetection.getClient()
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.size > 0) {
                        // Preview上に描画している線を消す
                        if (viewFinder.childCount > 1) {
                            for (i in 1 until viewFinder.childCount) {
                                viewFinder.removeViewAt(1)
                            }
                        }
                        faces.forEach { face ->
                            val bounds = face.boundingBox
                            val imageProxy = imageProxy
                            val originalBitmap = Bitmap.createScaledBitmap(
                                rotateBitmap(imageProxy.toBitmap(), 90),
                                imageProxy.height,
                                imageProxy.width,
                                false
                            )
                            if (bounds.left < 0) { bounds.left = 0 }
                            if (bounds.top < 0) { bounds.top = 0 }
                            if (bounds.left + bounds.width() > originalBitmap.width) { return@forEach }
                            if (bounds.top + bounds.height() > originalBitmap.height) { return@forEach }
                            val faceBitmap = Bitmap.createBitmap(
                                originalBitmap,
                                bounds.left,
                                bounds.top,
                                bounds.width(),
                                bounds.height()
                            )
                            maskDetection(
                                bounds = face.boundingBox,
                                previewSize = previewSize,
                                faceBitmap = faceBitmap,
                                faceImage = imageProxy
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.i("testing", "Failure")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }


    private fun maskDetection(
        faceBitmap: Bitmap,
        faceImage: ImageProxy,
        bounds: Rect,
        previewSize: Size
    ) {

        val inputBitmap = Bitmap.createScaledBitmap(
            rotateBitmap(faceBitmap, 90), 224, 224, true
        )

        val batchNum = 0
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (x in 0..223) {
            for (y in 0..223) {
                val pixel = inputBitmap.getPixel(x, y)
                input[batchNum][x][y][0] = (Color.red(pixel)) / 255.0f
                input[batchNum][x][y][1] = (Color.green(pixel)) / 255.0f
                input[batchNum][x][y][2] = (Color.blue(pixel)) / 255.0f
            }
        }

        val inputs = FirebaseModelInputs.Builder()
            .add(input)
            .build()

        interpreter?.run(inputs, inputOutputOptions)?.addOnSuccessListener { result ->
            val output = result.getOutput<Array<FloatArray>>(0)
            val notMasked = output[0][1]
            val masked = output[0][0]
            val element: DrawMask

            if (masked > notMasked) {
                element = DrawMask(
                    context = context,
                    rect = bounds,
                    color = Color.GREEN,
                    previewSize = previewSize,
                    analysisImage = faceImage,
                    text = String.format("Masked: %1.1f", masked * 100)
                )
            } else {
                element = DrawMask(
                    context = context,
                    rect = bounds,
                    color = Color.RED,
                    previewSize = previewSize,
                    analysisImage = faceImage,
                    text = String.format("Not masked: %1.1f", notMasked * 100)
                )
                playerStart()
            }
            viewFinder.addView(element, 1)
        }
    }


    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    private fun rotateBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun playerStart() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

}