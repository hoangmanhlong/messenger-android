package com.android.kotlin.familymessagingapp.utils

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

    const val REALTIME_DATABASE_USER_REF_NAME = "users_information_data"

    const val FIREBASE_STORAGE_USER_DATA_REF_NAME = "user_data"

    const val FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME = "${FIREBASE_STORAGE_USER_DATA_REF_NAME}/user_avatar_images"

    const val FIREBASE_STORAGE_CHAT_ROOM_REF_NAME = "chat_room"

    const val FIREBASE_STORAGE_CHAT_ROOM_IMAGE_REF_NAME = "$FIREBASE_STORAGE_CHAT_ROOM_REF_NAME/message_images"

    const val REALTIME_DATABASE_CHAT_ROOM_REF = "chatrooms"

    const val REALTIME_DATABASE_MESSAGES_REF = "$REALTIME_DATABASE_USER_REF_NAME/messages"

    const val CHAT_ROOM_KEY = "CHAT_ROOM_KEY"

    val number_of_images_can_be_selected = 1

    // Notification Channel constants

    // Name of Notification Channel for verbose notifications of background work
    @JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    @JvmField val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1

    const val IMAGE_KEY = "IMAGE_KEY"

    const val OUTPUT_PATH = "save_image_temp"

    // The name of the image manipulation work
    const val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"
    const val TAG_OUTPUT = "OUTPUT"
}


