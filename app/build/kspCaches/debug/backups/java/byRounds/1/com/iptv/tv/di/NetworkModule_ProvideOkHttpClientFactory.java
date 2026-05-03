package com.iptv.tv.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<ServerUrlInterceptor> serverUrlInterceptorProvider;

  public NetworkModule_ProvideOkHttpClientFactory(
      Provider<ServerUrlInterceptor> serverUrlInterceptorProvider) {
    this.serverUrlInterceptorProvider = serverUrlInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(serverUrlInterceptorProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      Provider<ServerUrlInterceptor> serverUrlInterceptorProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(serverUrlInterceptorProvider);
  }

  public static OkHttpClient provideOkHttpClient(ServerUrlInterceptor serverUrlInterceptor) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(serverUrlInterceptor));
  }
}
