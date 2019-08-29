package com.cronus.zdone.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(context: Context) : Interceptor {

    val apiTokenManager = ApiTokenManager(context)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val withApiToken = originalRequest.newBuilder()
                .addHeader("x-api-key", apiTokenManager.getToken())
                .build()

        return chain.proceed(withApiToken)

    }

}
