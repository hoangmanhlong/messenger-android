package com.android.kotlin.familymessagingapp.screen.chatroom_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.screen.Screen
import javax.inject.Inject

class ChatRoomDetailViewModel @Inject constructor(

) : ViewModel() {

    private lateinit var chatroomLiveData: LiveData<ChatRoom?>


}