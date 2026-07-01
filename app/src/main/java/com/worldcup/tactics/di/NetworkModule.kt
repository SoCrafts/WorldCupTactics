package com.worldcup.tactics.di

import com.worldcup.tactics.BuildConfig
import com.worldcup.tactics.data.remote.api.FootballDataApi
import com.worldcup.tactics.data.remote.api.SportsDbApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared JSON configuration
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared OkHttpClient (logging only — each Retrofit adds its own auth)
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideBaseOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TheSportsDB
    //  Base URL embeds the API key in the path:
    //    https://www.thesportsdb.com/api/v1/json/{key}/
    //  Free demo key = "3"  |  Set SPORTS_DB_KEY in local.properties for prod
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    @Named("sportsdb")
    fun provideSportsDbRetrofit(
        baseClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val key = BuildConfig.SPORTS_DB_KEY.ifBlank { "3" }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://www.thesportsdb.com/api/v1/json/$key/")
            .client(baseClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideSportsDbApi(@Named("sportsdb") retrofit: Retrofit): SportsDbApi =
        retrofit.create()

    // ─────────────────────────────────────────────────────────────────────────
    //  Football-Data.org v4
    //  Auth via X-Auth-Token header.
    //  Set FOOTBALL_DATA_KEY in local.properties.
    //  Free plan: 10 req/min, access to FIFA World Cup (code=WC).
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    @Named("footballdata")
    fun provideFootballDataOkHttpClient(baseClient: OkHttpClient): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Auth-Token", BuildConfig.FOOTBALL_DATA_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    @Named("footballdata")
    fun provideFootballDataRetrofit(
        @Named("footballdata") client: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.football-data.org/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideFootballDataApi(
        @Named("footballdata") retrofit: Retrofit
    ): FootballDataApi = retrofit.create()
}
