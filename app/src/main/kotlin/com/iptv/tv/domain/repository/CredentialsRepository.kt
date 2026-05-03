package com.iptv.tv.domain.repository

import com.iptv.tv.domain.model.Credentials
import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {
    fun getCredentials(): Flow<Credentials?>
    suspend fun saveCredentials(credentials: Credentials)
    suspend fun clearCredentials()
    fun getTheme(): Flow<String>
    suspend fun saveTheme(theme: String)
}
