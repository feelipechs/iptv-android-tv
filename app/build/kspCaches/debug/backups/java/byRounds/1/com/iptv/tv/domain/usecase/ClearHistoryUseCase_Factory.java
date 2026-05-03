package com.iptv.tv.domain.usecase;

import com.iptv.tv.domain.repository.WatchHistoryRepository;
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
public final class ClearHistoryUseCase_Factory implements Factory<ClearHistoryUseCase> {
  private final Provider<WatchHistoryRepository> repositoryProvider;

  public ClearHistoryUseCase_Factory(Provider<WatchHistoryRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ClearHistoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ClearHistoryUseCase_Factory create(
      Provider<WatchHistoryRepository> repositoryProvider) {
    return new ClearHistoryUseCase_Factory(repositoryProvider);
  }

  public static ClearHistoryUseCase newInstance(WatchHistoryRepository repository) {
    return new ClearHistoryUseCase(repository);
  }
}
