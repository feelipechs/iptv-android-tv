package com.iptv.tv.ui.screens.home;

import com.iptv.tv.domain.repository.CredentialsRepository;
import com.iptv.tv.domain.repository.FavoritesRepository;
import com.iptv.tv.domain.repository.WatchHistoryRepository;
import com.iptv.tv.domain.usecase.GetCategoriesUseCase;
import com.iptv.tv.domain.usecase.GetStreamsUseCase;
import com.iptv.tv.domain.usecase.RefreshContentUseCase;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider;

  private final Provider<GetStreamsUseCase> getStreamsUseCaseProvider;

  private final Provider<RefreshContentUseCase> refreshContentUseCaseProvider;

  private final Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider;

  private final Provider<FavoritesRepository> favoritesRepositoryProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  public HomeViewModel_Factory(Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider,
      Provider<GetStreamsUseCase> getStreamsUseCaseProvider,
      Provider<RefreshContentUseCase> refreshContentUseCaseProvider,
      Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    this.getCategoriesUseCaseProvider = getCategoriesUseCaseProvider;
    this.getStreamsUseCaseProvider = getStreamsUseCaseProvider;
    this.refreshContentUseCaseProvider = refreshContentUseCaseProvider;
    this.refreshStreamsUseCaseProvider = refreshStreamsUseCaseProvider;
    this.favoritesRepositoryProvider = favoritesRepositoryProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(getCategoriesUseCaseProvider.get(), getStreamsUseCaseProvider.get(), refreshContentUseCaseProvider.get(), refreshStreamsUseCaseProvider.get(), favoritesRepositoryProvider.get(), watchHistoryRepositoryProvider.get(), credentialsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider,
      Provider<GetStreamsUseCase> getStreamsUseCaseProvider,
      Provider<RefreshContentUseCase> refreshContentUseCaseProvider,
      Provider<RefreshStreamsUseCase> refreshStreamsUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    return new HomeViewModel_Factory(getCategoriesUseCaseProvider, getStreamsUseCaseProvider, refreshContentUseCaseProvider, refreshStreamsUseCaseProvider, favoritesRepositoryProvider, watchHistoryRepositoryProvider, credentialsRepositoryProvider);
  }

  public static HomeViewModel newInstance(GetCategoriesUseCase getCategoriesUseCase,
      GetStreamsUseCase getStreamsUseCase, RefreshContentUseCase refreshContentUseCase,
      RefreshStreamsUseCase refreshStreamsUseCase, FavoritesRepository favoritesRepository,
      WatchHistoryRepository watchHistoryRepository, CredentialsRepository credentialsRepository) {
    return new HomeViewModel(getCategoriesUseCase, getStreamsUseCase, refreshContentUseCase, refreshStreamsUseCase, favoritesRepository, watchHistoryRepository, credentialsRepository);
  }
}
