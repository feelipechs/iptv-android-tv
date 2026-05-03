package com.iptv.tv.di;

import com.iptv.tv.domain.repository.CredentialsRepository;
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
public final class ServerUrlInterceptor_Factory implements Factory<ServerUrlInterceptor> {
  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  public ServerUrlInterceptor_Factory(
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
  }

  @Override
  public ServerUrlInterceptor get() {
    return newInstance(credentialsRepositoryProvider.get());
  }

  public static ServerUrlInterceptor_Factory create(
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    return new ServerUrlInterceptor_Factory(credentialsRepositoryProvider);
  }

  public static ServerUrlInterceptor newInstance(CredentialsRepository credentialsRepository) {
    return new ServerUrlInterceptor(credentialsRepository);
  }
}
