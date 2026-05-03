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
public final class AddToHistoryUseCase_Factory implements Factory<AddToHistoryUseCase> {
  private final Provider<WatchHistoryRepository> repositoryProvider;

  public AddToHistoryUseCase_Factory(Provider<WatchHistoryRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddToHistoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddToHistoryUseCase_Factory create(
      Provider<WatchHistoryRepository> repositoryProvider) {
    return new AddToHistoryUseCase_Factory(repositoryProvider);
  }

  public static AddToHistoryUseCase newInstance(WatchHistoryRepository repository) {
    return new AddToHistoryUseCase(repository);
  }
}
