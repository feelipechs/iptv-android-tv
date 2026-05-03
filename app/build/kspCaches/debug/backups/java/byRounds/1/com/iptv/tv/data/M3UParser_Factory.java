package com.iptv.tv.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class M3UParser_Factory implements Factory<M3UParser> {
  private final Provider<Context> contextProvider;

  public M3UParser_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public M3UParser get() {
    return newInstance(contextProvider.get());
  }

  public static M3UParser_Factory create(Provider<Context> contextProvider) {
    return new M3UParser_Factory(contextProvider);
  }

  public static M3UParser newInstance(Context context) {
    return new M3UParser(context);
  }
}
