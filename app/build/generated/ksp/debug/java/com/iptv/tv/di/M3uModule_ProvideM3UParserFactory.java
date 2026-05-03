package com.iptv.tv.di;

import android.content.Context;
import com.iptv.tv.data.M3UParser;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class M3uModule_ProvideM3UParserFactory implements Factory<M3UParser> {
  private final Provider<Context> contextProvider;

  public M3uModule_ProvideM3UParserFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public M3UParser get() {
    return provideM3UParser(contextProvider.get());
  }

  public static M3uModule_ProvideM3UParserFactory create(Provider<Context> contextProvider) {
    return new M3uModule_ProvideM3UParserFactory(contextProvider);
  }

  public static M3UParser provideM3UParser(Context context) {
    return Preconditions.checkNotNullFromProvides(M3uModule.INSTANCE.provideM3UParser(context));
  }
}
