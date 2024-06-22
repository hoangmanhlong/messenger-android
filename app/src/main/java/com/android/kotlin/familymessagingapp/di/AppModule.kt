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
import com.android.kotlin.familymessagingapp.firebase_services.facebook.FacebookService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleServiceImpl
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.firebase_services.storage.AppFirebaseStorage
import com.android.kotlin.familymessagingapp.repository.BackendServiceRepository
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
    fun provideBackendServiceRepository(appService: BackendApiService): BackendServiceRepository =
        BackendServiceRepository(appService)

    @Provides
    @Singleton
    fun provideAppApiService(application: Application): BackendApiService =
        AppRetrofitClient(application).instance

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
        auth: FirebaseAuth,
        firebaseGoogleService: FirebaseGoogleService,
        firebaseEmailService: FirebaseEmailService,
        appFirebaseStorage: AppFirebaseStorage,
        appRealtimeDatabaseService: AppRealtimeDatabaseService,
        appDataMemoryRepository: DataMemoryRepository,
        facebookService: FacebookService
    ): FirebaseServiceRepository =
        FirebaseServiceRepository(
            auth,
            firebaseGoogleService,
            firebaseEmailService,
            appFirebaseStorage,
            appRealtimeDatabaseService,
            appDataMemoryRepository,
            facebookService
        )

    @Provides
    @Singleton
    fun provideFacebookService(auth: FirebaseAuth) = FacebookService(auth)

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
        appRealtimeDatabaseService: AppRealtimeDatabaseService,
        appFirebaseStorage: AppFirebaseStorage
    ): FirebaseGoogleService =
        FirebaseGoogleServiceImpl(
            auth,
            application,
            signInClient,
            appDataStore,
            appRealtimeDatabaseService,
            appFirebaseStorage
        )

    @Singleton
    @Provides
    fun provinceAppRealtimeDatabaseReference(
        application: Application,
        auth: FirebaseAuth,
        appFirebaseStorage: AppFirebaseStorage
    ): AppRealtimeDatabaseService =
        AppRealtimeDatabaseService(application, auth, appFirebaseStorage)

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(
        application: Application,
        auth: FirebaseAuth,
        appDataStore: AppDataStore,
        appRealtimeDatabaseService: AppRealtimeDatabaseService,
        appFirebaseStorage: AppFirebaseStorage
    ): FirebaseEmailService =
        FirebaseEmailServiceImpl(
            application,
            auth,
            appDataStore,
            appRealtimeDatabaseService,
            appFirebaseStorage
        )

    @Provides
    @Singleton
    fun provideFirebaseStorageReference(): AppFirebaseStorage = AppFirebaseStorage()
}