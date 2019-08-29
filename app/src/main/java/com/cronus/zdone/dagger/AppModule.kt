package com.cronus.zdone.dagger

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.AppExecutorsImpl
import com.cronus.zdone.api.AuthInterceptor
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.TasksRepositoryImpl
import com.cronus.zdone.api.ZdoneService
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

@Module(subcomponents = arrayOf(
        ScreenComponent::class
), includes = arrayOf(AppModule.Bindings::class)
)
class AppModule {

    @Module
    interface Bindings {

        @Binds
        fun tasksRepository(tasksRepositoryImpl: TasksRepositoryImpl): TasksRepository

        @Binds
        fun appExecutors(appExecutorsImpl: AppExecutorsImpl): AppExecutors
    }

    @Provides
    fun zdoneService(retrofit: Retrofit): ZdoneService {
        return retrofit.create(ZdoneService::class.java)
    }

    @Provides
    fun retrofit(loggingInterceptor: HttpLoggingInterceptor, authInterceptor: AuthInterceptor): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://www.zdone.co/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(
                        OkHttpClient.Builder()
                                .addInterceptor(authInterceptor)
                                .addInterceptor(loggingInterceptor)
                                .build()
                )
                .build()
    }

    @Provides
    fun loggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

}