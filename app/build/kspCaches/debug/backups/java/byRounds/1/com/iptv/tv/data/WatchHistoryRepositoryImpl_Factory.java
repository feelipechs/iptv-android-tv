package com.iptv.tv.data;

import com.iptv.tv.data.local.dao.WatchHistoryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class WatchHistoryRepositoryImpl_Factory implements Factory<WatchHistoryRepositoryImpl> {
  private final Provider<WatchHistoryDao> watchHistoryDaoProvider;

  public WatchHistoryRepositoryImpl_Factory(Provider<WatchHistoryDao> watchHistoryDaoProvider) {
    this.watchHistoryDaoProvider = watchHistoryDaoProvider;
  }

  @Override
  public WatchHistoryRepositoryImpl get() {
    return newInstance(watchHistoryDaoProvider.get());
  }

  public static WatchHistoryRepositoryImpl_Factory create(
      Provider<WatchHistoryDao> watchHistoryDaoProvider) {
    return new WatchHistoryRepositoryImpl_Factory(watchHistoryDaoProvider);
  }

  public static WatchHistoryRepositoryImpl newInstance(WatchHistoryDao watchHistoryDao) {
    return new WatchHistoryRepositoryImpl(watchHistoryDao);
  }
}
