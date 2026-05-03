package com.iptv.tv.ui.screens.favorites;

import com.iptv.tv.domain.repository.FavoritesRepository;
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
public final class FavoritesViewModel_Factory implements Factory<FavoritesViewModel> {
  private final Provider<FavoritesRepository> favoritesRepositoryProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  public FavoritesViewModel_Factory(Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider) {
    this.favoritesRepositoryProvider = favoritesRepositoryProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
  }

  @Override
  public FavoritesViewModel get() {
    return newInstance(favoritesRepositoryProvider.get(), watchHistoryRepositoryProvider.get());
  }

  public static FavoritesViewModel_Factory create(
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider) {
    return new FavoritesViewModel_Factory(favoritesRepositoryProvider, watchHistoryRepositoryProvider);
  }

  public static FavoritesViewModel newInstance(FavoritesRepository favoritesRepository,
      WatchHistoryRepository watchHistoryRepository) {
    return new FavoritesViewModel(favoritesRepository, watchHistoryRepository);
  }
}
