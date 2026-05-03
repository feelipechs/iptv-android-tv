package com.iptv.tv.ui.screens.content;

import androidx.lifecycle.SavedStateHandle;
import com.iptv.tv.domain.repository.FavoritesRepository;
import com.iptv.tv.domain.repository.WatchHistoryRepository;
import com.iptv.tv.domain.usecase.GetStreamsUseCase;
import com.iptv.tv.domain.usecase.RefreshStreamsUseCase;
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
public final class ContentViewModel_Factory implements Factory<ContentViewModel> {
  private final Provider<GetStreamsUseCase> getStreamsUseCaseProvider;

  private final Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider;

  private final Provider<FavoritesRepository> favoritesRepositoryProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public ContentViewModel_Factory(Provider<GetStreamsUseCase> getStreamsUseCaseProvider,
      Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getStreamsUseCaseProvider = getStreamsUseCaseProvider;
    this.refreshStreamsUseCaseProvider = refreshStreamsUseCaseProvider;
    this.favoritesRepositoryProvider = favoritesRepositoryProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ContentViewModel get() {
    return newInstance(getStreamsUseCaseProvider.get(), refreshStreamsUseCaseProvider.get(), favoritesRepositoryProvider.get(), watchHistoryRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static ContentViewModel_Factory create(
      Provider<GetStreamsUseCase> getStreamsUseCaseProvider,
      Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ContentViewModel_Factory(getStreamsUseCaseProvider, refreshStreamsUseCaseProvider, favoritesRepositoryProvider, watchHistoryRepositoryProvider, savedStateHandleProvider);
  }

  public static ContentViewModel newInstance(GetStreamsUseCase getStreamsUseCase,
      RefreshStreamsUseCase refreshStreamsUseCase, FavoritesRepository favoritesRepository,
      WatchHistoryRepository watchHistoryRepository, SavedStateHandle savedStateHandle) {
    return new ContentViewModel(getStreamsUseCase, refreshStreamsUseCase, favoritesRepository, watchHistoryRepository, savedStateHandle);
  }
}
