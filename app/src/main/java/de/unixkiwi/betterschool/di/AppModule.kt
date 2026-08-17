package de.unixkiwi.betterschool.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.unixkiwi.betterschool.core.BESTE_SCHULE_BASE_URL
import de.unixkiwi.betterschool.core.local.ApiCacheFileManager
import de.unixkiwi.betterschool.data.auth.AuthRepository
import de.unixkiwi.betterschool.data.auth.CryptoManager
import de.unixkiwi.betterschool.data.auth.LocalTokenSource
import de.unixkiwi.betterschool.data.settings.SettingsRepository
import de.unixkiwi.betterschool.data.timetable.RemoteTimetableSource
import de.unixkiwi.betterschool.data.timetable.TimetableRepository
import de.unixkiwi.betterschool.data.year.RemoteYearSource
import de.unixkiwi.betterschool.data.year.YearsRepository
import net.openid.appauth.AuthorizationService
import net.openid.appauth.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthorizationService(@ApplicationContext context: Context): AuthorizationService {
        return AuthorizationService(context)
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("auth_preferences") }
        )
    }

    @Provides
    @Singleton
    @Named("settings")
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_settings") }
        )
    }

    @Provides
    @Singleton
    fun provideApiCacheFileManager(@ApplicationContext context: Context): ApiCacheFileManager {
        return ApiCacheFileManager(context)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BESTE_SCHULE_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteTimetableSource(retrofit: Retrofit): RemoteTimetableSource {
        return retrofit.create()
    }

    @Provides
    @Singleton
    fun provideRemoteYearSource(retrofit: Retrofit): RemoteYearSource {
        return retrofit.create()
    }

    @Provides
    @Singleton
    fun provideYearsRepository(
        remoteYearSource: RemoteYearSource,
        authRepository: AuthRepository,
        cache: ApiCacheFileManager
    ): YearsRepository {
        return YearsRepository(remoteYearSource, authRepository, cache)
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(
        remoteSource: RemoteTimetableSource,
        yearsRepository: YearsRepository,
        cache: ApiCacheFileManager,
        @ApplicationContext context: Context,
        authRepo: AuthRepository
    ): TimetableRepository {
        return TimetableRepository(remoteSource, yearsRepository, cache, context, authRepo)
    }

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }

    @Provides
    @Singleton
    fun provideLocalTokenSource(
        @Named("auth") dataStore: DataStore<Preferences>,
        cryptoManager: CryptoManager
    ): LocalTokenSource {
        return LocalTokenSource(dataStore, cryptoManager)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @Named("settings") dataStore: DataStore<Preferences>
    ): SettingsRepository {
        return SettingsRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authorizationService: AuthorizationService,
        localTokenSource: LocalTokenSource
    ): AuthRepository {
        return AuthRepository(authorizationService, localTokenSource)
    }
}