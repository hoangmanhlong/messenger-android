package com.android.kotlin.familymessagingapp.firebase_services.realtime_database

import android.app.Application
import android.net.Uri
import android.util.Log
import com.android.kotlin.familymessagingapp.firebase_services.storage.AppFirebaseStorage
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppRealtimeDatabaseService(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val appFirebaseStorage: AppFirebaseStorage
) {

    private val databaseReference = Firebase.database.reference

    companion object {
        const val TAG = "AppRealtimeDatabaseService"
    }

    val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    val chatroomsRef = databaseReference.child(Constant.REALTIME_DATABASE_CHAT_ROOM_REF)

    val currentUserDataFlow: Flow<UserData?>
        get() = callbackFlow {
            auth.currentUser?.let {
                val currentUserRef: DatabaseReference = userDataRef.child(it.uid)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        this@callbackFlow.trySend(snapshot.getValue(UserData::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        currentUserRef.removeEventListener(this)

                    }

                }
                currentUserRef.addValueEventListener(listener)
                awaitClose { currentUserRef.removeEventListener(listener) }
            } ?: close()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatroomsFlow: Flow<List<ChatRoom>>
        get() = currentUserDataFlow.flatMapLatest { userData ->
            if (userData?.chatrooms.isNullOrEmpty()) {
                flowOf(emptyList()) // Emit null for empty list
            } else {
//                val chatroomFlows = userData?.chatrooms?.map { getChatRoomFlow(it) }
//                combine(chatroomFlows) { chatrooms ->
//                    chatrooms.filterNotNull().toList() // Filter out null ChatRooms
//                }
                flowOf(listOf(
                    ChatRoom(
                        chatRoomId = "fuyfuh",
                        members = listOf("1", "2"),
                        messages = null,
                        time = 67476476474,
                        isActive = true,
                        chatroomName = "Hello",
                        lastMessage = "alo"
                    ),
                    ChatRoom(
                        chatRoomId = "message 2",
                        members = listOf("1", "2"),
                        messages = null,
                        time = 674764764744,
                        isActive = false,
                        chatroomName = "Hello",
                        lastMessage = "alo"
                    )
                ))
            }
        }

    private fun getChatRoomFlow(chatroomId: String): Flow<ChatRoom?> = callbackFlow {
        val chatroomRef = chatroomsRef.child(chatroomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@callbackFlow.trySend(snapshot.getValue(ChatRoom::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatroomRef.addValueEventListener(listener)
        awaitClose { chatroomRef.removeEventListener(listener) }
    }

    suspend fun saveUserData(userData: UserData, imageUri: Uri?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var updatedUserData = userData
                if (imageUri != null) {
                    val downloadUrl = appFirebaseStorage.putUserAvatarUriToStorage(
                        application,
                        imageUri,
                        appFirebaseStorage.userAvatarRef.child(userData.uid!!)
                    )
                    updatedUserData = userData.copy(userAvatar = downloadUrl)
                }
                userDataRef.child(userData.uid!!).setValue(updatedUserData).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun deleteUserData(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                userDataRef.child(uid).removeValue().await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Problem: Với trường username
     * Chỉ tìm kiếm được được khi keyword giống 100% username
     * Ví dụ: keyword = "long", username trên server = "Long" => kết quả sẽ sai
     * Solution: thêm trường usernameLowercase vào UserData
     * usernameLowercase = username.toLoweCase
     */
    suspend fun search(keyword: String): List<UserData> {
        val list = mutableListOf<UserData>()
        return try {
            val dataSnapshot = if (StringUtils.isValidEmail(keyword)) {
                userDataRef.orderByChild(UserData.EMAIL).equalTo(keyword).get().await()
            } else if (StringUtils.isNumber(keyword)) {
                userDataRef.orderByChild(UserData.PHONE_NUMBER).equalTo(keyword).get().await()
            } else {
                userDataRef.orderByChild(UserData.USERNAME).equalTo(keyword).get().await()
            }

            if (dataSnapshot.exists()) {
                for (snapshot in dataSnapshot.children) {
                    snapshot.getValue(UserData::class.java)?.let {
                        if (it.uid != auth.uid) list.add(it)
                    }
                }
                list
            } else {
                list
            }
        } catch (e: Exception) {
            Log.d(TAG, "search: $e")
            list
        }
    }

}