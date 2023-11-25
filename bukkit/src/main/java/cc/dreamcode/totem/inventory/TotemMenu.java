package cc.dreamcode.totem.inventory;

import cc.dreamcode.menu.bukkit.BukkitMenuBuilder;
import cc.dreamcode.menu.bukkit.base.BukkitMenu;
import cc.dreamcode.menu.bukkit.setup.BukkitMenuSetup;
import cc.dreamcode.totem.TotemEffect;
import cc.dreamcode.totem.config.MessageConfig;
import cc.dreamcode.totem.config.PluginConfig;
import cc.dreamcode.totem.user.UserRepository;
import cc.dreamcode.totem.vault.VaultApi;
import cc.dreamcode.utilities.builder.MapBuilder;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.tasker.core.Tasker;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TotemMenu implements BukkitMenuSetup {
    private final UserRepository userRepository;
    private final MessageConfig messageConfig;
    private final PluginConfig pluginConfig;
    private final VaultApi vaultApi;
    private final Tasker tasker;

    @Override
    public BukkitMenu build() {
        final BukkitMenuBuilder bukkitMenuBuilder = this.pluginConfig.totemMenu;
        final BukkitMenu bukkitMenu = bukkitMenuBuilder.buildEmpty();


        if(pluginConfig.fillInventory){
            ItemStack fillItem = setItemColors(pluginConfig.fillMenuItem);
            for(int i = 0; i < bukkitMenuBuilder.getRows()*9; i++){
                bukkitMenu.setItem(i, fillItem);
            }
        }

        bukkitMenuBuilder.getItems().forEach((integer, itemStack) ->
                bukkitMenu.setItem(integer, setItemColors(itemStack), e -> e.setCancelled(true)));

        for(TotemEffect totemEffect : this.pluginConfig.effects.values()){
            bukkitMenu.setItem(totemEffect.getSlot(), totemEffect.getPresenterItem(), event -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();


                //Check required items
                if(!hasItems(player, totemEffect.getCost())){
                    messageConfig.missingRequiredItems.send(player, new MapBuilder<String, Object>()
                            .put("COUNT_ITEMS", getCountItems(player, totemEffect.getCost().stream().findFirst().get().getType()))
                            .put("REQUIRED_ITEMS", totemEffect.getCost().stream().findFirst().get().getAmount())
                            .build());
                    return;
                }

                this.tasker.newSharedChain("setTotemEffect")
                        .async(() -> this.userRepository.findOrCreateByHumanEntity(player))
                        .acceptSync(user -> {
                            removeItems(player, totemEffect.getCost());
                            user.setTotemEffect(totemEffect);
                            user.save();
                        })
                        .acceptAsync(user -> {
                            user.save();
                        })
                        .execute();

                //Check money
                int effectPrice = totemEffect.getPrice();
                if(effectPrice != 0) {
                    if(!vaultApi.isInitialized()){
                        getLogger().warning("Nie odnaleziono VaultAPI!");
                        return;
                    }

                    if (vaultApi.hasMoney(player, effectPrice)) {
                        messageConfig.noEnoughMoney.send(player, new MapBuilder<String, Object>()
                                .put("PRICE", effectPrice)
                                .put("BALANCE", vaultApi.getBalance(player))
                                .build());
                        return;
                    }
                    vaultApi.removeMoney(player, effectPrice);
                }

                messageConfig.purchasedTotemEffect.send(player, new MapBuilder<String, Object>()
                        .put("TOTEM_EFFECT", totemEffect.getDisplayName())
                        .build());
            });
        }

        return bukkitMenu;
    }


    private void removeItems(Player player, List<ItemStack> items){
        for(ItemStack itemStack : items){
            removeItem(player, itemStack.getType(), itemStack.getAmount());
        }
    }

    private void removeItem(Player player, Material material, int amount) {
        //if(getCountItems(player, material) >= amount) return;

        Inventory inventory = player.getInventory();
        int remainingAmount = amount;

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() != material) continue;

            if (itemStack.getAmount() <= remainingAmount) {
                remainingAmount -= itemStack.getAmount();
                inventory.removeItem(itemStack);
            } else {
                itemStack.setAmount(itemStack.getAmount() - remainingAmount);
                break;
            }
        }
    }

    private boolean hasItems(Player player, List<ItemStack> items) {
        for(ItemStack itemStack : items){
            int required = itemStack.getAmount();
            int count = getCountItems(player, itemStack.getType());
            if(required > count) return false;
        }

        return true;
    }

    private int getCountItems(Player player, Material material){
        int x = 0;

        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null) continue;
            if(item.getType() != material) continue;
            x+=item.getAmount();
        }
        return x;
    }

    private ItemStack setItemColors(ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.hasDisplayName()){
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("&", "§"));
        }

        if(itemMeta.hasLore()){
            List<String> lore = itemMeta.getLore();
            lore.forEach(line -> {
                lore.add(line.replace("&", "§"));
            });

            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
