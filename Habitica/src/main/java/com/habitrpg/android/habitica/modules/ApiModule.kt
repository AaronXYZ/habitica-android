package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.implementation.ApiClientImpl
import com.habitrpg.android.habitica.data.implementation.ApiClientImpl.Companion.createGsonFactory
import com.habitrpg.android.habitica.helpers.KeyHelper
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import dagger.Module
import dagger.Provides
import java.lang.ref.WeakReference
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
open class ApiModule {
    @Provides
    @Singleton
    fun providesHostConfig(
        sharedPreferences: SharedPreferences,
        keyHelper: KeyHelper?,
        context: Context
    ): HostConfig {
        return HostConfig(sharedPreferences, keyHelper, context)
    }

    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return createGsonFactory()
    }

    @Provides
    @Singleton
    fun providesPopupNotificationsManager(): NotificationsManager {
        return NotificationsManager()
    }

    @Provides
    @Singleton
    fun providesApiHelper(
        gsonConverter: GsonConverterFactory,
        hostConfig: HostConfig,
        analyticsManager: AnalyticsManager,
        notificationsManager: NotificationsManager,
        context: Context
    ): ApiClient {
        val apiClient = ApiClientImpl(
            gsonConverter,
            hostConfig,
            analyticsManager,
            notificationsManager,
            context
        )
        notificationsManager.apiClient = WeakReference(apiClient)
        return apiClient
    }

    @Provides
    fun providesMaintenanceApiService(gsonConverter: GsonConverterFactory): MaintenanceApiService {
        val adapter = Retrofit.Builder()
            .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(gsonConverter)
            .build()
        return adapter.create(MaintenanceApiService::class.java)
    }
}
