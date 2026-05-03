package com.iptv.tv.domain.usecase;

import com.iptv.tv.domain.repository.FavoritesRepository;
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
public final class GetFavoritesUseCase_Factory implements Factory<GetFavoritesUseCase> {
  private final Provider<FavoritesRepository> repositoryProvider;

  public GetFavoritesUseCase_Factory(Provider<FavoritesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetFavoritesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetFavoritesUseCase_Factory create(
      Provider<FavoritesRepository> repositoryProvider) {
    return new GetFavoritesUseCase_Factory(repositoryProvider);
  }

  public static GetFavoritesUseCase newInstance(FavoritesRepository repository) {
    return new GetFavoritesUseCase(repository);
  }
}
