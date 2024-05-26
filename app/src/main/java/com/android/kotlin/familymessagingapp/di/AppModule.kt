package com.android.kotlin.familymessagingapp.di

import android.app.Application
import com.android.kotlin.familymessagingapp.firebase_services.email_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.email_services.FirebaseEmailServiceImpl
import com.android.kotlin.familymessagingapp.firebase_services.google_services.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.firebase_services.google_services.FirebaseGoogleServiceImpl
import com.android.kotlin.familymessagingapp.repository.MessengerRepository
import com.android.kotlin.familymessagingapp.repository.MessengerRepositoryImpl
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
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

    @Provides
    @Singleton
    fun provideMessengerRepository(
        firebaseGoogleService: FirebaseGoogleService,
        firebaseEmailService: FirebaseEmailService
    ): MessengerRepository =
        MessengerRepositoryImpl(firebaseGoogleService, firebaseEmailService)

    @Provides
    @Singleton
    fun provideSignInClient(application: Application): SignInClient =
        Identity.getSignInClient(application)


    @Provides
    @Singleton
    fun provideGoogleService(application: Application, signInClient: SignInClient): FirebaseGoogleService =
        FirebaseGoogleServiceImpl(application, signInClient)

    @Singleton
    @Provides
    fun provinceDataReference(): DatabaseReference = FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(): FirebaseEmailService = FirebaseEmailServiceImpl()
}