package cc.dreamcode.totem;

import cc.dreamcode.utilities.bukkit.builder.ItemBuilder;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TotemEffect implements Serializable {
    private final String potionEffectType;
    private final boolean forOpponent;
    private final int duration;
    private final int amplifier;
    private final int price;
    private final int slot;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final List<ItemStack> cost;

    public TotemEffect(String potionEffectType, boolean forOpponent, int duration, int amplifier, int price, int slot, Material material, String displayName, List<String> lore, List<ItemStack> cost) {
        this.potionEffectType = potionEffectType;
        this.forOpponent = forOpponent;
        this.duration = duration;
        this.amplifier = amplifier;
        this.price = price;
        this.slot = slot;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.cost = cost;
    }

    public ItemStack getPresenterItem(){
        ItemBuilder itemBuilder = new ItemBuilder(material);
        itemBuilder.setName(displayName.replace("&", "§"));

        List<String> lore = new ArrayList<>();
        this.lore.forEach(line -> {
            lore.add(line.replace("&", "§").replace("{PRICE}", price + ""));
        });

        itemBuilder.setLore(lore);

        return itemBuilder.toItemStack();
    }

    public PotionEffect getPotionEffect(){
        PotionEffectType potionEffectType = PotionEffectType.getByName(this.potionEffectType);
        return new PotionEffect(potionEffectType, duration, amplifier);
    }
}
