package com.android.kotlin.familymessagingapp.di

import android.app.Application
import androidx.room.Room
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.data_store.dataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.remote.AppRetrofitClient
import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.BackendApiService
import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailServiceImpl
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleServiceImpl
import com.android.kotlin.familymessagingapp.firebase_services.realtime.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    fun provideAppRepository(appService: BackendApiService): AppRepository =
        AppRepository(appService)

    @Provides
    @Singleton
    fun provideAppApiService(application: Application): BackendApiService =
        AppRetrofitClient(application).retrofit

    @Provides
    @Singleton
    fun provideAppRoomDatabase(application: Application): AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        Constant.ROOM_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseAuthenticationRepository(
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
        AppDataStore(application, application.dataStore)

    @Provides
    @Singleton
    fun provideGoogleService(
        auth: FirebaseAuth,
        application: Application,
        signInClient: SignInClient,
        appDataStore: AppDataStore,
        databaseReference: DatabaseReference,
        storageReference: StorageReference
    ): FirebaseGoogleService =
        FirebaseGoogleServiceImpl(
            auth,
            application,
            signInClient,
            appDataStore,
            databaseReference,
            storageReference
        )

    @Singleton
    @Provides
    fun provinceAppRealtimeDatabaseReference(
        authenticationRepository: FirebaseAuthenticationRepository,
        databaseReference: DatabaseReference
    ): AppRealtimeDatabaseService =
        AppRealtimeDatabaseService(authenticationRepository, databaseReference)

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference = FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(
        auth: FirebaseAuth,
        appDataStore: AppDataStore
    ): FirebaseEmailService =
        FirebaseEmailServiceImpl(auth, appDataStore)

    @Provides
    @Singleton
    fun provideFirebaseStorageReference(): StorageReference =
        FirebaseStorage.getInstance().reference
}