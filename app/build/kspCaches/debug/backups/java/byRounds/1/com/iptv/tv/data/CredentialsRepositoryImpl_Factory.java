package com.iptv.tv.data;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class CredentialsRepositoryImpl_Factory implements Factory<CredentialsRepositoryImpl> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public CredentialsRepositoryImpl_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public CredentialsRepositoryImpl get() {
    return newInstance(dataStoreProvider.get());
  }

  public static CredentialsRepositoryImpl_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new CredentialsRepositoryImpl_Factory(dataStoreProvider);
  }

  public static CredentialsRepositoryImpl newInstance(DataStore<Preferences> dataStore) {
    return new CredentialsRepositoryImpl(dataStore);
  }
}
