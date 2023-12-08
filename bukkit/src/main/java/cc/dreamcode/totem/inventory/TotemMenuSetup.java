package cc.dreamcode.totem.inventory;

import cc.dreamcode.menu.bukkit.BukkitMenuBuilder;
import cc.dreamcode.menu.bukkit.base.BukkitMenu;
import cc.dreamcode.totem.TotemEffect;
import cc.dreamcode.totem.TotemPlugin;
import cc.dreamcode.totem.config.MessageConfig;
import cc.dreamcode.totem.config.PluginConfig;
import cc.dreamcode.totem.user.UserRepository;
import cc.dreamcode.totem.vault.VaultApi;
import cc.dreamcode.utilities.builder.MapBuilder;
import cc.dreamcode.utilities.bukkit.builder.ItemBuilder;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.tasker.core.Tasker;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class TotemMenuSetup {
    @Inject private Tasker tasker;
    @Inject private VaultApi vaultApi;
    @Inject private PluginConfig pluginConfig;
    @Inject private MessageConfig messageConfig;
    @Inject private UserRepository userRepository;
    @Inject private TotemMenuHolder totemMenuHolder;


    @NonNull
    public BukkitMenu setup() {
        final BukkitMenuBuilder bukkitMenuBuilder = this.pluginConfig.totemMenu;
        final BukkitMenu bukkitMenu = bukkitMenuBuilder.buildEmpty();

        //Fill inventory
        if(pluginConfig.fillInventory){
            ItemStack fillItem = new ItemBuilder(pluginConfig.fillMenuItem).fixColors().toItemStack();
            for(int i = 0; i < bukkitMenuBuilder.getRows()*9; i++){
                bukkitMenu.setItem(i, fillItem);
            }
        }


        //Set items from config to inventory
        bukkitMenuBuilder.getItems().forEach((integer, itemStack) ->
                bukkitMenu.setItem(integer, new ItemBuilder(itemStack).fixColors().toItemStack(), e -> e.setCancelled(true)));


        //Set effects to inventory
        for(TotemEffect totemEffect : this.pluginConfig.effects.values()){
            ItemStack totemItem = new ItemBuilder(totemEffect.getMaterial())
                    .setName(totemEffect.getDisplayName())
                    .setLore(totemEffect.getLore())
                    .fixColors(new MapBuilder<String, Object>()
                            .put("PRICE", totemEffect.getPrice())
                            .build())
                    .toItemStack();

            //Set items in inventory
            bukkitMenu.setItem(totemEffect.getSlot(), totemItem, event -> {
                Player player = (Player) event.getWhoClicked();

                //Check required items
                if(!hasItems(player, totemEffect.getCost())){
                    messageConfig.missingRequiredItems.send(player, new MapBuilder<String, Object>()
                            .put("COUNT_ITEMS", getCountItems(player, totemEffect.getCost().stream().findFirst().get().getType()))
                            .put("REQUIRED_ITEMS", totemEffect.getCost().stream().findFirst().get().getAmount())
                            .build());
                    return;
                }

                //Check money
                double balance = vaultApi.getBalance(player);
                if(totemEffect.getPrice() > balance){
                    messageConfig.noEnoughMoney.send(player, new MapBuilder<String, Object>()
                            .put("PRICE", totemEffect.getPrice())
                            .put("BALANCE", (int) vaultApi.getBalance(player))
                            .build());
                    return;
                }

                this.tasker.newSharedChain(player.getUniqueId().toString())
                        .async(() -> this.userRepository.findOrCreateByHumanEntity(player))
                        .acceptSync(user -> {
                            //Check if the player has already purchased effect
                            if(user.getTotemEffect() != null && totemEffect.getSlot() == user.getTotemEffect().getSlot()) {
                                messageConfig.alreadyPurchased.send(player, new MapBuilder<String, Object>()
                                        .put("EFFECT_NAME", totemEffect.getDisplayName())
                                        .build());
                                return;
                            }

                            removeItems(player, totemEffect.getCost());
                            vaultApi.removeMoney(player, totemEffect.getPrice());
                            user.setTotemEffect(totemEffect);
                            messageConfig.purchasedTotemEffect.send(player, new MapBuilder<String, Object>()
                                    .put("TOTEM_EFFECT", totemEffect.getDisplayName())
                                    .build());
                        })
                        .acceptAsync(user -> {
                            user.save();
                        })
                        .execute();
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
}
