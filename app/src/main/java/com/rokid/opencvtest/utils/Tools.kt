package com.rokid.opencvtest.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import androidx.camera.core.ImageProxy
import com.rokid.opencvtest.RokidApplication
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Tools {
    fun getPixelsRGB(image: Bitmap): ByteArray {
        // calculate how many bytes our image consists of
        val bytes = image.byteCount
        //LogUtil.i("", "#######getPixelsRGB bytes:$bytes  ")
        val buffer = ByteBuffer.allocate(bytes) // Create a new buffer
        //LogUtil.i("", "#######getPixelsRGB buffer:$buffer  ")
        image.copyPixelsToBuffer(buffer) // Move the byte data to the buffer
        val temp = buffer.array() // Get the underlying array containing the data.
        //LogUtil.i("", "#######getPixelsRGB temp:$temp  ")
        val pixels = ByteArray(temp.size / 4 * 3) // Allocate for BGR

        // Copy pixels into place
        for (i in 0 until temp.size / 4) {
            pixels[i * 3] = temp[i * 4] //R
            pixels[i * 3 + 1] = temp[i * 4 + 1] //G
            pixels[i * 3 + 2] = temp[i * 4 + 2] //B
        }
        //image.recycle()
        return pixels
    }


    fun YUV_420_888toNV21(image: ImageProxy): ByteArray? {
        var startTimeStamp = System.currentTimeMillis()
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        var rowStride = image.planes[0].rowStride
        assert(image.planes[0].pixelStride == 1)
        var pos = 0
        if (rowStride == width) { // likely
            yBuffer[nv21, 0, ySize]
            pos += ySize
        } else {
            var yBufferPos = (width - rowStride).toLong() // not an actual position
            while (pos < ySize) {
                yBufferPos += (rowStride - width).toLong()
                yBuffer.position(yBufferPos.toInt())
                yBuffer[nv21, pos, width]
                pos += width
            }
        }
        rowStride = image.planes[2].rowStride
        val pixelStride = image.planes[2].pixelStride
        assert(rowStride == image.planes[1].rowStride)
        assert(pixelStride == image.planes[1].pixelStride)
        if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
            val savePixel = vBuffer[1]
            vBuffer.put(1, 0.toByte())
            if (uBuffer[0].toInt() == 0) {
                vBuffer.put(1, 255.toByte())
                if (uBuffer[0].toInt() == 255) {
                    vBuffer.put(1, savePixel)
                    vBuffer[nv21, ySize, uvSize]
                    return nv21 // shortcut
                }
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel)
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer[vuPos]
                nv21[pos++] = uBuffer[vuPos]
            }
        }
        return nv21
    }


    fun getNoMoreThanTwoDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

    fun saveBytes(data: ByteArray?, tofile: String?): Boolean {
        try {
            val file = File(tofile)
            if (!file.parentFile.exists()) {
                file.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
            val fos = FileOutputStream(tofile)
            fos.write(data)
            fos.flush()
            fos.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    //文件按文件名排序
    fun sortFiles(files: Array<String?>): List<String> {
        try {
            val fileList = Arrays.asList(*files)
            Collections.sort(fileList) { o1, o2 ->

                val index1 = o1?.split("\\.")!!.toTypedArray()[0].toInt()
                val index2 = o2?.split("\\.")!!.toTypedArray()[0].toInt()
                index1 - index2
            }
            return fileList as List<String>
        } catch (e: Exception) {
            return ArrayList<String>()
        }
    }

    fun float2int(f: Float): Int {
        return f.toInt()
    }

    //获取分辨率
    val windowViewPix: WindowViewPix
        get() {
            //获取分辨率
            var width = 0
            var height = 0
            val windowManager = RokidApplication.appContext!!
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                width = windowMetrics.bounds.width() - insets.left - insets.right
                height = windowMetrics.bounds.height() - insets.top - insets.bottom
            } else {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                width = displayMetrics.widthPixels
                height = displayMetrics.heightPixels
            }
            return WindowViewPix(width, height)
        }

    // HH:mm:ss
    val currDateString: String
        get() {
            val simpleDateFormat =
                SimpleDateFormat("yyyyMMddHHmmss") // HH:mm:ss
            val date = Date(System.currentTimeMillis())
            return simpleDateFormat.format(date)
        }

    // untested function
    fun getNV21(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray? {
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
        //scaled.recycle()
        return yuv
    }

    fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }
}