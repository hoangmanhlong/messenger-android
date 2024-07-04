package com.android.kotlin.familymessagingapp.utils

object Constant {

    const val USER_DATA_KEY = "USER_DATA_KEY"

    const val FCM_TOKEN_KEY = "FCM_TOKEN_KEY"

    const val BASE_URL = "https://developer.android.com/"

    const val DURATION_TIMEOUT = 60

    const val ROOM_DATABASE_NAME = "app_database"

    const val TOKEN_KEY = "TOKEN_KEY"

    const val VIETNAM_COUNTRY_CODE = "vi"

    const val ENGLISH_COUNTRY_CODE = "en-US"

    const val IS_AUTHENTICATE_BY_EMAIL_KEY = "IS_AUTHENTICATE_BY_EMAIL_KEY"

    const val ARE_NOTIFICATION_ENABLED = "ARE_NOTIFICATION_ENABLED"

    const val IS_THE_ENGLISH_LANGUAGE_DISPLAYED = "IS_THE_ENGLISH_LANGUAGE_DISPLAYED"

    const val IS_THE_FIRST_LAUNCH = "IS_THE_FIRST_LAUNCH"

    const val REALTIME_DATABASE_USER_REF_NAME = "users_information_data"

    const val FIREBASE_STORAGE_USER_DATA_REF_NAME = "user_data"

    const val FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME = "${FIREBASE_STORAGE_USER_DATA_REF_NAME}/user_avatar_images"

    const val FIREBASE_STORAGE_CHAT_ROOM_REF_NAME = "chat_room"

    const val FIREBASE_STORAGE_CHAT_ROOM_IMAGE_REF_NAME = "$FIREBASE_STORAGE_CHAT_ROOM_REF_NAME/message_images"

    const val REALTIME_DATABASE_CHAT_ROOM_REF = "chatrooms"

    const val REALTIME_DATABASE_MESSAGES_REF = "$REALTIME_DATABASE_USER_REF_NAME/messages"

    const val CHAT_ROOM_KEY = "CHAT_ROOM_KEY"

    val number_of_images_can_be_selected = 1
}


