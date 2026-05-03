package com.iptv.tv.data;

import com.iptv.tv.data.local.dao.CategoryDao;
import com.iptv.tv.data.local.dao.StreamDao;
import com.iptv.tv.domain.repository.CredentialsRepository;
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
public final class M3uContentRepository_Factory implements Factory<M3uContentRepository> {
  private final Provider<M3UParser> m3uParserProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<StreamDao> streamDaoProvider;

  private final Provider<CredentialsRepository> credentialsRepositoryProvider;

  public M3uContentRepository_Factory(Provider<M3UParser> m3uParserProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<StreamDao> streamDaoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    this.m3uParserProvider = m3uParserProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.streamDaoProvider = streamDaoProvider;
    this.credentialsRepositoryProvider = credentialsRepositoryProvider;
  }

  @Override
  public M3uContentRepository get() {
    return newInstance(m3uParserProvider.get(), categoryDaoProvider.get(), streamDaoProvider.get(), credentialsRepositoryProvider.get());
  }

  public static M3uContentRepository_Factory create(Provider<M3UParser> m3uParserProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<StreamDao> streamDaoProvider,
      Provider<CredentialsRepository> credentialsRepositoryProvider) {
    return new M3uContentRepository_Factory(m3uParserProvider, categoryDaoProvider, streamDaoProvider, credentialsRepositoryProvider);
  }

  public static M3uContentRepository newInstance(M3UParser m3uParser, CategoryDao categoryDao,
      StreamDao streamDao, CredentialsRepository credentialsRepository) {
    return new M3uContentRepository(m3uParser, categoryDao, streamDao, credentialsRepository);
  }
}
