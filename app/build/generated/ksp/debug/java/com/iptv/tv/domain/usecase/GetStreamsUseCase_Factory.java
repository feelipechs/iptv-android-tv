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
public final class GetStreamsUseCase_Factory implements Factory<GetStreamsUseCase> {
  private final Provider<ContentRepository> repositoryProvider;

  public GetStreamsUseCase_Factory(Provider<ContentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetStreamsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetStreamsUseCase_Factory create(Provider<ContentRepository> repositoryProvider) {
    return new GetStreamsUseCase_Factory(repositoryProvider);
  }

  public static GetStreamsUseCase newInstance(ContentRepository repository) {
    return new GetStreamsUseCase(repository);
  }
}
