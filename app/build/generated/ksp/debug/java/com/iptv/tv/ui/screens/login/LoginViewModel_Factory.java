package com.iptv.tv.ui.screens.login;

import com.iptv.tv.domain.repository.ContentRepository;
import com.iptv.tv.domain.repository.CredentialsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  public LoginViewModel_Factory(Provider<CredentialsRepository> credentialsRepositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(credentialsRepositoryProvider.get(), contentRepositoryProvider.get());
  }

  public static LoginViewModel_Factory create(
      Provider<CredentialsRepository> credentialsRepositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    return new LoginViewModel_Factory(credentialsRepositoryProvider, contentRepositoryProvider);
  }

  public static LoginViewModel newInstance(CredentialsRepository credentialsRepository,
      ContentRepository contentRepository) {
    return new LoginViewModel(credentialsRepository, contentRepository);
  }
}
