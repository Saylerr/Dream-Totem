package cc.dreamcode.totem.config;

import cc.dreamcode.notice.minecraft.MinecraftNoticeType;
import cc.dreamcode.notice.minecraft.bukkit.BukkitNotice;
import cc.dreamcode.platform.bukkit.component.configuration.Configuration;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.Headers;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Configuration(
        child = "message.yml"
)
@Headers({
        @Header("## Dream-Totem (Message-Config) ##"),
        @Header("Dostepne type: (DO_NOT_SEND, CHAT, ACTION_BAR, SUBTITLE, TITLE, TITLE_SUBTITLE)")
})
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MessageConfig extends OkaeriConfig {
    public BukkitNotice missingRequiredItems = new BukkitNotice(MinecraftNoticeType.CHAT, "&c» &7Brak wymaganych przedmiotów! &8(&f{COUNT_ITEMS}&8/&f{REQUIRED_ITEMS}&8)");
    public BukkitNotice noEnoughMoney = new BukkitNotice(MinecraftNoticeType.CHAT, "&c» &7Nie posiadasz wystarczająco pieniędzy! &8(&f{BALANCE}&8/&f{PRICE}&8)");
    public BukkitNotice purchasedTotemEffect = new BukkitNotice(MinecraftNoticeType.CHAT, "&a» &7Pomyślnie zakupiono efekt: &f{TOTEM_EFFECT}");
    public BukkitNotice noPermission = new BukkitNotice(MinecraftNoticeType.CHAT, "&4Nie posiadasz uprawnien.");
    public BukkitNotice notPlayer = new BukkitNotice(MinecraftNoticeType.CHAT, "&4Nie jestes graczem.");
}
