package com.cronus.zdone.dagger

import com.cronus.zdone.AppDispatchers
import com.cronus.zdone.AppExecutors
import com.cronus.zdone.AppExecutorsImpl
import com.cronus.zdone.RealAppDispatchers
import com.cronus.zdone.api.AuthInterceptor
import com.cronus.zdone.api.RealTasksRepository
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.ZdoneService
import com.cronus.zdone.home.RealUserSelectedTasksRepository
import com.cronus.zdone.home.UserSelectedTasksRepository
import com.cronus.zdone.notification.TaskNotificationManager
import com.cronus.zdone.notification.TaskNotificationShower
import com.cronus.zdone.stats.summary.DailyStatsSummaryProvider
import com.cronus.zdone.stats.summary.RealDailyStatsSummaryProvider
import com.cronus.zdone.timer.RealTaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@Module(subcomponents = arrayOf(
        ScreenComponent::class
), includes = arrayOf(AppModule.Bindings::class)
)
class AppModule {

    @Module
    interface Bindings {

        @Binds
        @Singleton
        fun tasksRepository(realTasksRepository: RealTasksRepository): TasksRepository

        @Binds
        @Singleton
        fun appExecutors(appExecutorsImpl: AppExecutorsImpl): AppExecutors

        @Binds
        @Singleton
        fun taskExecutionManager(realTaskExecutionManager: RealTaskExecutionManager): TaskExecutionManager

        @Binds
        fun appDispatchers(realAppDispatchers: RealAppDispatchers): AppDispatchers

        @Binds
        @Singleton
        fun dailyStatsProvider(realDailyStatsProvider: RealDailyStatsSummaryProvider): DailyStatsSummaryProvider

        @Binds
        @Singleton
        fun userSelectedTasksRepository(realUserSelectedTasksRepository: RealUserSelectedTasksRepository): UserSelectedTasksRepository

    }

    @Provides
    fun zdoneService(retrofit: Retrofit): ZdoneService {
        return retrofit.create(ZdoneService::class.java)
    }

    @Provides
    fun retrofit(loggingInterceptor: HttpLoggingInterceptor, authInterceptor: AuthInterceptor): Retrofit {
        return Retrofit.Builder()
                //use http://10.0.2.2:5000/ for local dev server
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

    @Provides
    fun taskNotificationManager(
        notificationShower: TaskNotificationShower,
        taskExecutionManager: TaskExecutionManager
    ) = TaskNotificationManager.from(notificationShower, taskExecutionManager)

}