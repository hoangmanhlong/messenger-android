package com.android.kotlin.familymessagingapp.services.firebase_services.storage

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FirebaseStorageService(private val application: Application) {

    private val storageRef = Firebase.storage.reference

    val userAvatarRef = storageRef.child(Constant.FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME)

    val chatRoomRef = storageRef.child(Constant.FIREBASE_STORAGE_CHAT_ROOM_REF_NAME)

    val chatroomMessageRef = storageRef.child(Constant.FIREBASE_STORAGE_CHAT_ROOM_MESSAGES_IMAGE_REF_NAME)

    suspend fun createDownloadUrlFromImageUrl(
        imageUrl: String,
        storageRef: StorageReference
    ): String? {
        return withContext(Dispatchers.IO) {
            var localFile: File? = null
            try {
                localFile = File.createTempFile("avatar", "jpg")
                val bitmap = MediaUtils.convertImageUrlToBitmap(application, imageUrl)
                    ?: return@withContext null
                FileOutputStream(localFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                val avatarUri = Uri.fromFile(localFile)
                storageRef.putFile(avatarUri).await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                null
            } finally {
                localFile?.delete()
            }
        }
    }

    suspend fun putUriToStorage(
        uri: Uri,
        storageRef: StorageReference
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                storageRef.putFile(uri).await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                e.stackTrace
                null
            }
        }
    }

    suspend fun deleteUserData(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                userAvatarRef.child(uid).delete().await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun initializeChatRoomImageRefInStorage(chatroomId: String): StorageReference {
        return chatRoomRef.child(chatroomId)
            .child(Constant.FIREBASE_STORAGE_CHAT_ROOM_IMAGE_REF_NAME)
            .child(chatroomId)
    }
}