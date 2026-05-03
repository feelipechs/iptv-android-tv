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
public final class RefreshContentUseCase_Factory implements Factory<RefreshContentUseCase> {
  private final Provider<ContentRepository> repositoryProvider;

  public RefreshContentUseCase_Factory(Provider<ContentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public RefreshContentUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static RefreshContentUseCase_Factory create(
      Provider<ContentRepository> repositoryProvider) {
    return new RefreshContentUseCase_Factory(repositoryProvider);
  }

  public static RefreshContentUseCase newInstance(ContentRepository repository) {
    return new RefreshContentUseCase(repository);
  }
}
