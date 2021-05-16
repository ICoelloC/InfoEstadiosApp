package com.icoello.myapplication.Utilidades

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Clase de Fotografías
 */
object Fotos {

    /**
     * Función para obtener el nombre del fichero en base a un prefijo y una extensión
     */
    fun crearNombreFoto(prefijo: String, extension: String): String {
        // Si no sabemos el nombre
        return prefijo + "-" + UUID.randomUUID().toString() + extension
    }

    /**
     * Salva un fichero en un directorio
     */
    fun salvarFoto(path: String, nombre: String, context: Context): File? {
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }
        try {
            val f = File(dirFotos, nombre)
            f.createNewFile()
            return f
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return null
    }

    /**
     * Copia un bitmap en un path determinado
     */
    fun copiarFoto(bitmap: Bitmap, nombre: String, path: String, compresion: Int, context: Context): File {
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }

        val fichero =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + path + File.separator + nombre
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compresion, bytes)
        val fo = FileOutputStream(fichero)
        fo.write(bytes.toByteArray())
        fo.close()
        return File(fichero)
    }

    /**
     * Comprime una imagen
     */
    fun comprimirFoto(fichero: File, bitmap: Bitmap, compresion: Int) {
        // Recuperamos el Bitmap
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compresion, bytes)
        val fo = FileOutputStream(fichero)
        fo.write(bytes.toByteArray())
        fo.close()
    }

    /**
     * Elimina fotos del directorio
     * @param context Context
     */
    fun deleteFotoDir(context: Context) {
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + "/MisLugares")
        Log.i("File", dirFotos.absolutePath)
        dirFotos.deleteRecursively()
    }

    fun crearNombreFichero(): String {
        return "camera-" + UUID.randomUUID().toString() + ".jpg"
    }

    fun salvarImagen(path: String, nombre: String, context: Context): File? {
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }
        try {
            val f = File(dirFotos, nombre)
            f.createNewFile()
            return f
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return null
    }

    fun scaleImage(foto: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (maxHeight > 0 && maxWidth > 0) {
            val width: Int = foto.width
            val height: Int = foto.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }

            return Bitmap.createScaledBitmap(foto, finalWidth, finalHeight, false)

        } else {
            return foto
        }
    }

    fun redondearFoto(imagen: ImageView) {
        val originalDrawable: Drawable = imagen.drawable
        var originalBitmap: Bitmap = (originalDrawable as BitmapDrawable).bitmap

        if (originalBitmap.width > originalBitmap.height) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.height, originalBitmap.height);
        } else if (originalBitmap.width < originalBitmap.height) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.width);
        }
        val roundedDrawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(Resources.getSystem(), originalBitmap)
        roundedDrawable.cornerRadius = originalBitmap.width.toFloat()
        imagen.setImageDrawable(roundedDrawable)
    }
}