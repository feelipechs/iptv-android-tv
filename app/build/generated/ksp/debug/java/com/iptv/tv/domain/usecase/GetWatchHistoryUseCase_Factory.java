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
public final class GetWatchHistoryUseCase_Factory implements Factory<GetWatchHistoryUseCase> {
  private final Provider<WatchHistoryRepository> repositoryProvider;

  public GetWatchHistoryUseCase_Factory(Provider<WatchHistoryRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetWatchHistoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetWatchHistoryUseCase_Factory create(
      Provider<WatchHistoryRepository> repositoryProvider) {
    return new GetWatchHistoryUseCase_Factory(repositoryProvider);
  }

  public static GetWatchHistoryUseCase newInstance(WatchHistoryRepository repository) {
    return new GetWatchHistoryUseCase(repository);
  }
}
