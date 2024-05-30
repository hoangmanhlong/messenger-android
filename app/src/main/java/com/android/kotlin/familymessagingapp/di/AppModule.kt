package com.android.kotlin.familymessagingapp.di

import android.app.Application
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.data_store.dataStore
import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailServiceImpl
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleServiceImpl
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
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
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideMessengerRepository(
        firebaseGoogleService: FirebaseGoogleService,
        firebaseEmailService: FirebaseEmailService
    ): FirebaseAuthenticationRepository =
        FirebaseAuthenticationRepository(firebaseGoogleService, firebaseEmailService)

    @Provides
    @Singleton
    fun provideDataMemoryRepository(appDataStore: AppDataStore): DataMemoryRepository =
        DataMemoryRepository(appDataStore)

    @Provides
    @Singleton
    fun provideSignInClient(application: Application): SignInClient =
        Identity.getSignInClient(application)

    @Provides
    @Singleton
    fun provideAppDataStore(application: Application): AppDataStore =
        AppDataStore(application.dataStore)

    @Provides
    @Singleton
    fun provideGoogleService(
        auth: FirebaseAuth,
        application: Application,
        signInClient: SignInClient,
        appDataStore: AppDataStore
    ): FirebaseGoogleService =
        FirebaseGoogleServiceImpl(auth, application, signInClient, appDataStore)

    @Singleton
    @Provides
    fun provinceRealtimeDatabaseReference(): DatabaseReference =
        FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(
        application: Application,
        auth: FirebaseAuth,
        appDataStore: AppDataStore
    ): FirebaseEmailService =
        FirebaseEmailServiceImpl(application, auth, appDataStore)
}