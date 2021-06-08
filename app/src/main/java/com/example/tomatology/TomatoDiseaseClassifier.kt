package com.example.tomatology

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TomatoDiseaseClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    /** Executor to run inference task in the background */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model
    private var inputImageChannels: Int = 0 // will be inferred from TF Lite model
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model
    private val classes: Array<String> = arrayOf("Bacterial Spot",
        "Early Blight",
        "Healthy",
        "Late Blight",
        "Leaf Mold",
        "Mosaic Virus",
        "Septoria Leaf Spot",
        "Target Spot",
        "Two Spotted Spider Mite",
        "Yellow Leaf Curl Virus")

    fun initialize(model: Any): Task<Void> {
        return call(
            executorService,
            {
                initializeInterpreter(model)
                null
            }
        )
    }

    private fun initializeInterpreter(model: Any) {
        // Initialize TF Lite Interpreter with NNAPI enabled
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter: Interpreter = if (model is ByteBuffer) {
            Interpreter(model, options)
        } else {
            Interpreter(model as File, options)
        }
        // Read input shape from model file
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        inputImageChannels = inputShape[3]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * inputImageChannels * PIXEL_SIZE

        // Finish interpreter initialization
        this.interpreter = interpreter
        isInitialized = true
        Log.d(TAG, "Initialized TFLite interpreter.")
    }

    private fun classify(bitmap: Bitmap): ArrayList<Prediction> {
        if (!isInitialized) {
            throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
        }

        var elapsedTime: Long

        // Preprocessing: resize the input
        var startTime: Long = System.nanoTime()
        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)
        elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

        startTime = System.nanoTime()
        val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        interpreter?.run(byteBuffer, result)
        elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Inference time = " + elapsedTime + "ms")

        return getOutputList(result[0])
    }

    fun classifyAsync(bitmap: Bitmap): Task<ArrayList<Prediction>> {
        return call(executorService, { classify(bitmap) })
    }

    fun close() {
        call(
            executorService,
            {
                interpreter?.close()
                Log.d(TAG, "Closed TFLite interpreter.")
                null
            }
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

//        val pixels = IntArray(inputImageWidth * inputImageHeight)
//        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//        for (pixelValue in pixels) {
//            val r = (pixelValue shr 16 and 0xFF)
//            val g = (pixelValue shr 8 and 0xFF)
//            val b = (pixelValue and 0xFF)
//
//            // Convert RGB to grayscale and normalize pixel value to [0..1]
//            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
//            byteBuffer.putFloat(normalizedPixelValue)
//        }

        for (y in 0 until inputImageWidth) {
            for (x in 0 until inputImageHeight) {
                val px = bitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                byteBuffer.putFloat(rf)
                byteBuffer.putFloat(gf)
                byteBuffer.putFloat(bf)
            }
        }

        return byteBuffer
    }

    private fun getOutputList(output: FloatArray): ArrayList<Prediction> {
//        val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
//
//        return classes[maxIndex]+" - %.2f%%".format(output[maxIndex]*100)
        val data: ArrayList<Prediction> = ArrayList()
        output.indices.forEach { data.add(Prediction(classes[it],it,output[it])) }

        return data
    }

    companion object {
        private const val TAG = "TomatoDiseaseClassifier"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 10
    }
}
