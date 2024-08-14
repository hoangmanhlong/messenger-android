package com.android.kotlin.familymessagingapp.di

import android.app.Application
import androidx.room.Room
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.data_store.dataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.local.work.AppWorkManager
import com.android.kotlin.familymessagingapp.data.remote.AppRetrofitClient
import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.BackendApiService
import com.android.kotlin.familymessagingapp.data.remote.socket.SocketClient
import com.android.kotlin.familymessagingapp.repository.BackendServiceRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.services.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.services.firebase_services.facebook.FacebookService
import com.android.kotlin.familymessagingapp.services.firebase_services.fcm.FCMService
import com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.NotificationHelper
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
    fun provideBackendServiceRepository(
        socketClient: SocketClient,
        appService: BackendApiService
    ): BackendServiceRepository =
        BackendServiceRepository(appService, socketClient)

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
    fun provideFirebaseServiceRepository(
        auth: FirebaseAuth,
        firebaseGoogleService: FirebaseGoogleService,
        firebaseEmailService: FirebaseEmailService,
        firebaseStorageService: FirebaseStorageService,
        firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService,
        facebookService: FacebookService,
        backendServiceRepository: BackendServiceRepository
    ): FirebaseServiceRepository =
        FirebaseServiceRepository(
            auth,
            firebaseGoogleService,
            firebaseEmailService,
            firebaseStorageService,
            firebaseRealtimeDatabaseService,
            facebookService,
            backendServiceRepository
        )

    @Provides
    @Singleton
    fun provideFacebookService(auth: FirebaseAuth) = FacebookService(auth)

    @Provides
    @Singleton
    fun provideDataMemoryRepository(
        appDataStore: AppDataStore,
        appDatabase: AppDatabase
    ): LocalDatabaseRepository =
        LocalDatabaseRepository(appDataStore, appDatabase)

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
        signInClient: SignInClient,
        appDataStore: AppDataStore,
        firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
    ): FirebaseGoogleService =
        FirebaseGoogleService(
            auth,
            signInClient,
            appDataStore,
            firebaseRealtimeDatabaseService
        )

    @Singleton
    @Provides
    fun provinceAppRealtimeDatabaseReference(
        auth: FirebaseAuth,
        firebaseStorageService: FirebaseStorageService,
        fcmService: FCMService,
        socketClient: SocketClient,
        localDatabaseRepository: LocalDatabaseRepository
    ): FirebaseRealtimeDatabaseService =
        FirebaseRealtimeDatabaseService(
            auth,
            firebaseStorageService,
            fcmService,
            socketClient,
            localDatabaseRepository
        )

    @Singleton
    @Provides
    fun provinceFirebaseEmailService(
        auth: FirebaseAuth,
        appDataStore: AppDataStore,
        firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
    ): FirebaseEmailService =
        FirebaseEmailService(
            auth,
            appDataStore,
            firebaseRealtimeDatabaseService
        )

    @Provides
    @Singleton
    fun provideFirebaseStorageReference(application: Application): FirebaseStorageService =
        FirebaseStorageService(application)

    @Provides
    @Singleton
    fun provideGeminiModel(application: Application): GeminiModel = GeminiModel(application)

    @Provides
    @Singleton
    fun provideAppWorkManager(application: Application) = AppWorkManager(application)

    @Provides
    @Singleton
    fun provideSocketIO(): SocketClient = SocketClient()

    @Provides
    @Singleton
    fun provideFCMService(): FCMService = FCMService()

    @Provides
    @Singleton
    fun provideNotificationHelper(application: Application): NotificationHelper =
        NotificationHelper(application)

}