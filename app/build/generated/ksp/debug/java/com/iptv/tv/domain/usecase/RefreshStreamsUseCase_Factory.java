package com.iptv.tv.domain.usecase;

import com.iptv.tv.domain.repository.ContentRepository;
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
public final class RefreshStreamsUseCase_Factory implements Factory<RefreshStreamsUseCase> {
  private final Provider<ContentRepository> repositoryProvider;

  public RefreshStreamsUseCase_Factory(Provider<ContentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public RefreshStreamsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static RefreshStreamsUseCase_Factory create(
      Provider<ContentRepository> repositoryProvider) {
    return new RefreshStreamsUseCase_Factory(repositoryProvider);
  }

  public static RefreshStreamsUseCase newInstance(ContentRepository repository) {
    return new RefreshStreamsUseCase(repository);
  }
}
