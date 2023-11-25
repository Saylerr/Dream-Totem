package cc.dreamcode.totem;

import cc.dreamcode.menu.bukkit.base.BukkitMenu;
import cc.dreamcode.totem.config.MessageConfig;
import cc.dreamcode.totem.config.PluginConfig;
import cc.dreamcode.totem.inventory.TotemMenu;
import cc.dreamcode.totem.user.UserRepository;
import cc.dreamcode.totem.vault.VaultApi;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.tasker.core.Tasker;
import lombok.Getter;

public class TotemService {
    @Getter private final BukkitMenu effectsMenu;

    @Inject
    public TotemService(UserRepository userRepository, MessageConfig messageConfig, PluginConfig pluginConfig, VaultApi vaultApi, Tasker tasker) {
        this.effectsMenu = new TotemMenu(userRepository, messageConfig, pluginConfig, vaultApi, tasker).build();
    }
}
