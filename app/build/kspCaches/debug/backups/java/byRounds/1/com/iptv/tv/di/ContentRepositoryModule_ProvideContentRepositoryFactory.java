package com.iptv.tv.di;

import com.iptv.tv.data.ContentRepositoryImpl;
import com.iptv.tv.data.M3uContentRepository;
import com.iptv.tv.domain.repository.ContentRepository;
import com.iptv.tv.domain.repository.CredentialsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ContentRepositoryModule_ProvideContentRepositoryFactory implements Factory<ContentRepository> {
  private final Provider<ContentRepositoryImpl> xtreamRepoProvider;

  private final Provider<M3uContentRepository> m3uRepoProvider;

  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  public ContentRepositoryModule_ProvideContentRepositoryFactory(
      Provider<ContentRepositoryImpl> xtreamRepoProvider,
      Provider<M3uContentRepository> m3uRepoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    this.xtreamRepoProvider = xtreamRepoProvider;
    this.m3uRepoProvider = m3uRepoProvider;
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
  }

  @Override
  public ContentRepository get() {
    return provideContentRepository(xtreamRepoProvider.get(), m3uRepoProvider.get(), credentialsRepositoryProvider.get());
  }

  public static ContentRepositoryModule_ProvideContentRepositoryFactory create(
      Provider<ContentRepositoryImpl> xtreamRepoProvider,
      Provider<M3uContentRepository> m3uRepoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    return new ContentRepositoryModule_ProvideContentRepositoryFactory(xtreamRepoProvider, m3uRepoProvider, credentialsRepositoryProvider);
  }

  public static ContentRepository provideContentRepository(ContentRepositoryImpl xtreamRepo,
      M3uContentRepository m3uRepo, CredentialsRepository credentialsRepository) {
    return Preconditions.checkNotNullFromProvides(ContentRepositoryModule.INSTANCE.provideContentRepository(xtreamRepo, m3uRepo, credentialsRepository));
  }
}
