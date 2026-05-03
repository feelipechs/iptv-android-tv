package com.iptv.tv.di;

import com.iptv.tv.data.remote.api.XtreamApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideXtreamApiFactory implements Factory<XtreamApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideXtreamApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public XtreamApiService get() {
    return provideXtreamApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideXtreamApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideXtreamApiFactory(retrofitProvider);
  }

  public static XtreamApiService provideXtreamApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideXtreamApi(retrofit));
  }
}
