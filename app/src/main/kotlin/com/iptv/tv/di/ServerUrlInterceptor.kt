package com.iptv.tv.di

import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.CredentialsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerUrlInterceptor @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : Interceptor {

    @Volatile
    private var cachedBaseUrl: String? = null

    init {
        applicationScope.launch {
            credentialsRepository.getCredentials().collect { credentials ->
                cachedBaseUrl = if (credentials != null && credentials.providerType != ProviderType.M3U_LIST) {
                    val server = credentials.server.trim()
                    if (server.isNotEmpty() && (server.startsWith("http://") || server.startsWith("https://"))) {
                        server.trimEnd('/') + "/"
                    } else null
                } else null
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val baseUrl = cachedBaseUrl
        if (baseUrl == null) {
            return chain.proceed(chain.request())
        }

        return try {
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder()
                .scheme(baseUrl.toHttpUrl().scheme)
                .host(baseUrl.toHttpUrl().host)
                .port(baseUrl.toHttpUrl().port)
                .build()

            val newRequest = originalRequest.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        } catch (e: IllegalArgumentException) {
            chain.proceed(chain.request())
        }
    }
}