package com.iptv.tv.di

import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.CredentialsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor que substitui o baseUrl do Retrofit pela URL real do servidor
 * armazenada nas credenciais. Isso permite que a URL seja configurada pelo
 * usuário sem precisar recriar o client Retrofit.
 */
@Singleton
class ServerUrlInterceptor @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = runBlocking { credentialsRepository.getCredentials().first() }
            ?: return chain.proceed(chain.request())

        if (credentials.providerType == ProviderType.M3U_LIST) {
            return chain.proceed(chain.request())
        }

        val server = credentials.server.trim()
        if (server.isEmpty()) {
            return chain.proceed(chain.request())
        }

        val serverUrl = server.trimEnd('/') + "/"

        // Valida se serverUrl tem scheme http/https antes de tentar modificar
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            return chain.proceed(chain.request())
        }

        return try {
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder()
                .scheme(serverUrl.toHttpUrl().scheme)
                .host(serverUrl.toHttpUrl().host)
                .port(serverUrl.toHttpUrl().port)
                .build()

            val newRequest = originalRequest.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        } catch (e: IllegalArgumentException) {
            chain.proceed(chain.request())
        }
    }
}