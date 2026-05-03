package com.iptv.tv.ui.screens.category;

import androidx.lifecycle.SavedStateHandle;
import com.iptv.tv.domain.repository.FavoritesRepository;
import com.iptv.tv.domain.repository.WatchHistoryRepository;
import com.iptv.tv.domain.usecase.GetCategoriesUseCase;
import com.iptv.tv.domain.usecase.RefreshContentUseCase;
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
public final class CategoryViewModel_Factory implements Factory<CategoryViewModel> {
  private final Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider;

  private final Provider<RefreshContentUseCase> refreshContentUseCaseProvider;

  private final Provider<FavoritesRepository> favoritesRepositoryProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public CategoryViewModel_Factory(Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider,
      Provider<RefreshContentUseCase> refreshContentUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getCategoriesUseCaseProvider = getCategoriesUseCaseProvider;
    this.refreshContentUseCaseProvider = refreshContentUseCaseProvider;
    this.favoritesRepositoryProvider = favoritesRepositoryProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public CategoryViewModel get() {
    return newInstance(getCategoriesUseCaseProvider.get(), refreshContentUseCaseProvider.get(), favoritesRepositoryProvider.get(), watchHistoryRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static CategoryViewModel_Factory create(
      Provider<GetCategoriesUseCase> getCategoriesUseCaseProvider,
      Provider<RefreshContentUseCase> refreshContentUseCaseProvider,
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new CategoryViewModel_Factory(getCategoriesUseCaseProvider, refreshContentUseCaseProvider, favoritesRepositoryProvider, watchHistoryRepositoryProvider, savedStateHandleProvider);
  }

  public static CategoryViewModel newInstance(GetCategoriesUseCase getCategoriesUseCase,
      RefreshContentUseCase refreshContentUseCase, FavoritesRepository favoritesRepository,
      WatchHistoryRepository watchHistoryRepository, SavedStateHandle savedStateHandle) {
    return new CategoryViewModel(getCategoriesUseCase, refreshContentUseCase, favoritesRepository, watchHistoryRepository, savedStateHandle);
  }
}
