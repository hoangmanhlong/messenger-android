package com.android.kotlin.familymessagingapp.firebase_services.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.android.kotlin.familymessagingapp.utils.Constant
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AppFirebaseStorage {
    private val storageRef = Firebase.storage.reference

    val userAvatarRef = storageRef.child(Constant.FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME)

    suspend fun createDownloadUrlFromImageUrl(
        context: Context,
        imageUrl: String,
        storageRef: StorageReference
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val localFile = File.createTempFile("avatar", "jpg")

                // Load image from URL and save to local file
                Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            FileOutputStream(localFile).use { out ->
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Handle if needed
                        }
                    })

                val avatarUri = Uri.fromFile(localFile)
                storageRef.putFile(avatarUri).await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun putUserAvatarUriToStorage(
        context: Context,
        imageUri: Uri,
        storageRef: StorageReference
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                storageRef.putFile(imageUri).await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                null
            }
        }
    }
}