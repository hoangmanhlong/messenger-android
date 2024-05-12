package com.android.kotlin.familymessagingapp.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class Constant @Inject constructor(private val databaseReference: DatabaseReference) {
    private val USER_REF_NAME: String = "user_personal_information"
    val userdataRef = databaseReference.child(USER_REF_NAME)
}
