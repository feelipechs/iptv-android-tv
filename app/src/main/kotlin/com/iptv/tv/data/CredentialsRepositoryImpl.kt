package com.iptv.tv.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.CredentialsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CredentialsRepository {

    private object Keys {
        val SERVER = stringPreferencesKey("server")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val PROVIDER_TYPE = stringPreferencesKey("provider_type")
        val M3U_SOURCE = stringPreferencesKey("m3u_source")
        val THEME = stringPreferencesKey("theme")
    }

    override fun getCredentials(): Flow<Credentials?> = dataStore.data.map { prefs ->
        val server = prefs[Keys.SERVER] ?: return@map null
        val username = prefs[Keys.USERNAME] ?: return@map null
        val password = prefs[Keys.PASSWORD] ?: return@map null
        val providerType = prefs[Keys.PROVIDER_TYPE]?.let {
            try { ProviderType.valueOf(it) } catch (e: Exception) { ProviderType.XTREAM }
        } ?: ProviderType.XTREAM
        val m3uSource = prefs[Keys.M3U_SOURCE]
        Credentials(server, username, password, providerType, m3uSource)
    }

    override fun getTheme(): Flow<String> = dataStore.data.map { prefs -> prefs[Keys.THEME] ?: "LIGHT" }

    override suspend fun saveTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme
        }
    }

    override suspend fun saveCredentials(credentials: Credentials) {
        dataStore.edit { prefs ->
            prefs[Keys.SERVER] = credentials.server.trimEnd('/')
            prefs[Keys.USERNAME] = credentials.username
            prefs[Keys.PASSWORD] = credentials.password
            prefs[Keys.PROVIDER_TYPE] = credentials.providerType.name
            credentials.m3uSource?.let { prefs[Keys.M3U_SOURCE] = it }
        }
    }

    override suspend fun clearCredentials() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.PASSWORD)
            prefs.remove(Keys.PROVIDER_TYPE)
            prefs.remove(Keys.M3U_SOURCE)
        }
    }
}
