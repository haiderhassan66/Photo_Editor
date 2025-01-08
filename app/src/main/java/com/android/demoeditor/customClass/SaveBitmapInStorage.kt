package com.android.demoeditor.customClass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.android.demoeditor.R
import com.android.demoeditor.enums.ImgExtension
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SaveBitmapInStorage(private val context: Context) {

    companion object {
        private val TAG = SaveBitmapInStorage::class.java.simpleName
        var imagePath = ""
    }

    var compressionQuality = 100
        set(value) {
            if (value in 0..100) {
                field = value
            } else {
                throw Exception("compressionQuality value must be between 0 to 100")
            }
        }

    private var imgExtension = ImgExtension.PNG.name.lowercase()

    fun changeImgExtension(imgType: ImgExtension) {
        imgExtension = imgType.name.lowercase()
    }

    private val dirName = context.getString(R.string.app_name)

    private val appName = dirName

//    private val folder =    File(context.getExternalFilesDir(null)?.path + dirName)
//    private val folder =  File("${context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}${File.separator}$dirName" )
//    private val folder = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),dirName )

    private val folder =
        File(context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.path + File.separator + appName)


    // It must be execute on background thread
    fun save(bitmap: Bitmap, isSaveInCacheDir: Boolean = false): Uri {
        Log.d(TAG, " bitmap :-> $bitmap")
        if (!folder.isDirectory) {
            folder.mkdir()
            Log.d(TAG, " file created :-> $folder")
        }


        val imgFile = if (isSaveInCacheDir) File(context.cacheDir, getCacheImgFile)
        else File(folder, "$appName${System.currentTimeMillis()}.$imgExtension")


        try {

            val fileOutputStream = FileOutputStream(imgFile)

            checkImgFormat(bitmap, fileOutputStream)

            fileOutputStream.apply {
                flush();
                close()
            }


        } catch (e: IOException) {
            e.printStackTrace();

        } finally {
//            context.sendBroadcast( Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgFile))); <-- this is deprecated

            // this is notify the gallery a file is added
            MediaScannerConnection.scanFile(context, arrayOf(imgFile.toString()), null, null)

        }

        return Uri.fromFile(imgFile)


    }

    fun saveImage(
        activity2: Activity,
        bitmap: Bitmap,
        z: Boolean,
        quality: Int
    ): String {
        val str: String
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "/Foto Editor"
            )
            //            File file = new File(getApplicationContext().getFilesDir(), "/ABLLogo Maker");
            if (!file.exists() && !file.mkdirs()) {
                Log.d("", "Can't create directory to save image.")
                //  Toast.makeText(activity2, activity2.getResources().getString(R.string.create_dir_err), Toast.LENGTH_LONG).show();
                return ""
            }
            val str2 = "Photo_" + System.currentTimeMillis()
            str = if (z) {
                "$str2.jpg"
            } else {
                "$str2.png"
            }
            imagePath = file.path + File.separator + str
            val file2: File = File(imagePath)
            try {
                if (!file2.exists()) {
                    file2.createNewFile()
                }
                val fileOutputStream = FileOutputStream(file2)
                if (!z) {
//
                    val createBitmap =
                        Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
                    val canvas: Canvas = Canvas(createBitmap)
                    canvas.drawColor(-1)
                    canvas.drawBitmap(bitmap, 0.0f, 0.0f, null as Paint?)
                    bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream)
                    createBitmap.recycle()
                } else {
                    val createBitmap =
                        Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
                    val canvas: Canvas = Canvas(createBitmap)
                    canvas.drawColor(-1)
                    canvas.drawBitmap(bitmap, 0.0f, 0.0f, null as Paint?)
                    createBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
                    createBitmap.recycle()
                }
                fileOutputStream.flush()
                fileOutputStream.close()
                MediaScannerConnection.scanFile(
                    activity2,
                    arrayOf<String>(file2.absolutePath),
                    null as Array<String?>?
                ) { str, uri ->
                    Log.i("ExternalStorage", "Scanned $str:")
                    val sb = StringBuilder()
                    sb.append("-> uri=")
                    sb.append(uri)
                    Log.i("ExternalStorage", sb.toString())
                }
//                activity2.sendBroadcast(
//                    Intent(
//                        "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
//                        Uri.fromFile(file2)
//                    )
//                )
                return imagePath
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return ""
            }
        } catch (e2: java.lang.Exception) {
            e2.printStackTrace()
            Log.i("testing", "Exception" + e2.message)
            return ""
        }
    }


    private fun checkImgFormat(bitmap: Bitmap, fos: FileOutputStream) {
        when (imgExtension) {
            ImgExtension.PNG.name.lowercase() -> {
                bitmap.compress(Bitmap.CompressFormat.PNG, compressionQuality, fos)
            }

            ImgExtension.JPG.name.lowercase() -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, fos)
            }


        }

    }


    private val getCacheImgFile = "${context.getString(R.string.cache_img_name)}.$imgExtension"

    // This should be in background thread ...
    fun savePhotoInCacheDir(bitmap: Bitmap): Uri {
        return save(bitmap, true)

    }


    private fun isUriExist(uri: Uri):Boolean{
        var bool = false
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.close()
            bool = true
        } catch (e: java.lang.Exception) {
            Log.w(TAG, "File corresponding to the uri does not exist $uri")
        }
        return bool
    }


}