package com.android.kotlin.familymessagingapp.di

import com.android.kotlin.familymessagingapp.firebase_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.FirebaseEmailServiceImpl
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provinceConstant(databaseReference: DatabaseReference): Constant =
        Constant(databaseReference)

    @Singleton
    @Provides
    fun provinceDataReference(): DatabaseReference = FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(constant: Constant): FirebaseEmailService =
        FirebaseEmailServiceImpl(constant)
}