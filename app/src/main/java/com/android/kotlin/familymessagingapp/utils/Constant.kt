package com.android.kotlin.familymessagingapp.utils

import com.android.kotlin.familymessagingapp.BuildConfig

object Constant {

    const val USER_DATA_KEY = "USER_DATA_KEY"

    const val FCM_TOKEN_KEY = "FCM_TOKEN_KEY"

    const val ENABLED_AI = "ENABLED_AI"

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

    const val REALTIME_DATABASE_PUBLIC_USER_DATA_REF_NAME = "public_user_data"

    const val FIREBASE_REALTIME_DATABASE_PRIVATE_USER_DATA = "private_user_data"

    const val FIREBASE_REALTIME_DATABASE_SECURE_USER_DATA_REF_NAME = "secure_user_data"

    const val FIREBASE_REALTIME_DATABASE_MOBILE_CONFIG_REF_NAME = "mobileConfig"



    const val FIREBASE_STORAGE_USER_DATA_REF_NAME = "user_data"

    const val FCM_TOKEN = "fcmToken"

    const val FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME =
        "${FIREBASE_STORAGE_USER_DATA_REF_NAME}/user_avatar_images"

    const val FIREBASE_STORAGE_CHAT_ROOM_REF_NAME = "chat_room"

    const val FIREBASE_STORAGE_CHAT_ROOM_IMAGE_REF_NAME =
        "$FIREBASE_STORAGE_CHAT_ROOM_REF_NAME/message_images"

    const val FIREBASE_REALTIME_DATABASE_CHAT_ROOM_REF = "chatrooms"

    const val CHAT_ROOM_KEY = "CHAT_ROOM_KEY"

    val number_of_images_can_be_selected = 1

    // Notification Channel constants

    // Name of Notification Channel for verbose notifications of background work
    @JvmField
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    @JvmField
    val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1

    const val IMAGE_KEY = "IMAGE_KEY"

    const val OUTPUT_PATH = "save_image_temp"

    // The name of the image manipulation work
    const val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"
    const val TAG_OUTPUT = "OUTPUT"

    const val MAX_PINNED_MESSAGE = "MAX_PINNED_MESSAGE"

    const val ONE_SECOND = 1000

    const val BACK_PRESS_INTERVAL = 2000L

    const val FIREBASE_REALTIME_DATABASE_USER_PRIVATE_INFO_REF_NAME = "user_private_info"

    const val FIREBASE_REALTIME_DATABASE_SEARCH_HISTORY_REF_NAME = "search_history"

    const val USER_ONLINE_STATUS_SOCKET_EVENT = "USER_ONLINE_STATUS_SOCKET_EVENT"

    const val FIREBASE_REALTIME_DATABASE_VERIFIED_STATUS_REF_NAME = "verified"

    const val USER_VERIFIED_STATUS_SOCKET_EVENT = "USER_VERIFIED_STATUS_SOCKET_EVENT"

    const val NEW_CHATROOM_SOCKET_EVENT = "NEW_CHATROOM_SOCKET_EVENT"

    const val NEW_MESSAGE_SOCKET_EVENT = "NEW_MESSAGE_SOCKET_EVENT"

    const val APP_BASE_URI = "https://${BuildConfig.APPLICATION_ID}/"

    fun getAppUrl(endpoint: String): String = "$APP_BASE_URI$endpoint"
}


