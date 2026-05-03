package com.iptv.tv.di;

import com.iptv.tv.data.local.AppDatabase;
import com.iptv.tv.data.local.dao.StreamDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideStreamDaoFactory implements Factory<StreamDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideStreamDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public StreamDao get() {
    return provideStreamDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideStreamDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideStreamDaoFactory(dbProvider);
  }

  public static StreamDao provideStreamDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStreamDao(db));
  }
}
