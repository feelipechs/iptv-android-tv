package com.iptv.tv.data;

import com.iptv.tv.data.local.dao.CategoryDao;
import com.iptv.tv.data.local.dao.StreamDao;
import com.iptv.tv.data.remote.api.XtreamApiService;
import com.iptv.tv.domain.repository.CredentialsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ContentRepositoryImpl_Factory implements Factory<ContentRepositoryImpl> {
  private final Provider<XtreamApiService> apiProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<StreamDao> streamDaoProvider;

  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public ContentRepositoryImpl_Factory(Provider<XtreamApiService> apiProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<StreamDao> streamDaoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.apiProvider = apiProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.streamDaoProvider = streamDaoProvider;
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public ContentRepositoryImpl get() {
    return newInstance(apiProvider.get(), categoryDaoProvider.get(), streamDaoProvider.get(), credentialsRepositoryProvider.get(), okHttpClientProvider.get());
  }

  public static ContentRepositoryImpl_Factory create(Provider<XtreamApiService> apiProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<StreamDao> streamDaoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new ContentRepositoryImpl_Factory(apiProvider, categoryDaoProvider, streamDaoProvider, credentialsRepositoryProvider, okHttpClientProvider);
  }

  public static ContentRepositoryImpl newInstance(XtreamApiService api, CategoryDao categoryDao,
      StreamDao streamDao, CredentialsRepository credentialsRepository, OkHttpClient okHttpClient) {
    return new ContentRepositoryImpl(api, categoryDao, streamDao, credentialsRepository, okHttpClient);
  }
}
