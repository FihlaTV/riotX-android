/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.multipicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import im.vector.riotx.multipicker.entity.MultiPickerImageType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraPicker(val requestCode: Int) {

    fun startWithExpectingFile(activity: Activity): Uri? {
        val photoUri = createPhotoUri(activity)
        val intent = createIntent().apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        activity.startActivityForResult(intent, requestCode)
        return photoUri
    }

    fun startWithExpectingFile(fragment: Fragment): Uri? {
        val photoUri = createPhotoUri(fragment.requireContext())
        val intent = createIntent().apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        fragment.startActivityForResult(intent, requestCode)
        return photoUri
    }

    fun getTakenPhoto(context: Context, requestCode: Int, resultCode: Int, photoUri: Uri): MultiPickerImageType? {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            val projection = arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE
            )

            context.contentResolver.query(
                    photoUri,
                    projection,
                    null,
                    null,
                    null
            )?.use { cursor ->
                val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

                if (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)

                    var orientation = 0

                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, photoUri))
                    } else {
                        context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }

                    context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                        try {
                            ExifInterface(inputStream).let {
                                orientation = it.rotationDegrees
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    return MultiPickerImageType(
                            name,
                            size,
                            context.contentResolver.getType(photoUri),
                            photoUri,
                            bitmap?.width ?: 0,
                            bitmap?.height ?: 0,
                            orientation
                    )
                }
            }
        }
        return null
    }

    private fun createIntent(): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    private fun createPhotoUri(context: Context): Uri {
        val file = createImageFile(context)
        val authority = context.packageName + ".multipicker.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    private fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = context.filesDir
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }
}