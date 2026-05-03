package com.iptv.tv.ui.screens.player;

import com.iptv.tv.player.PlayerManager;
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<PlayerManager> playerManagerProvider;

  public PlayerViewModel_Factory(Provider<PlayerManager> playerManagerProvider) {
    this.playerManagerProvider = playerManagerProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(playerManagerProvider.get());
  }

  public static PlayerViewModel_Factory create(Provider<PlayerManager> playerManagerProvider) {
    return new PlayerViewModel_Factory(playerManagerProvider);
  }

  public static PlayerViewModel newInstance(PlayerManager playerManager) {
    return new PlayerViewModel(playerManager);
  }
}
