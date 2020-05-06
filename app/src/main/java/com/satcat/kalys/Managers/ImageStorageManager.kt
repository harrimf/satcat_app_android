package com.satcat.kalys.Managers

import android.R
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.satcat.kalys.KalysApplication
import java.io.*


open class ImageStorageManager{

    companion object {
        @JvmStatic
        val shared = ImageStorageManager()

    }

    fun convertStringToBitmap(data: String): Bitmap {
        val imageAsBytes: ByteArray = Base64.decode(data.toByteArray(), Base64.DEFAULT)
        val image = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        return image
    }

    fun convertBitmapToString(bitmap: Bitmap): String {

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return encoded
    }


    fun saveToInternalStorage(bitmapImage: Bitmap, name: String): String? {
        val cw = ContextWrapper(KalysApplication.getContext())
        // path to /data/data/yourapp/app_data/images
        val directory: File = cw.getDir("images", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "$name.png")
        lateinit var fos: FileOutputStream
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return mypath.absolutePath + "/$name.png"
    }


    fun getImage(name: String) : Bitmap? {
        try {
            val cw = ContextWrapper(KalysApplication.getContext())
            val directory: File = cw.getDir("images", Context.MODE_PRIVATE)
            val f = File(directory, "$name.png")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            return b
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeResource(KalysApplication.getContext().resources, R.drawable.ic_dialog_email)
    }

    fun resizeBitmap(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }

}