package ru.nedan.spookybuy.items.impl;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.data.EnchantmentData;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.UNBREAKING;

public class EnchantedItem extends CollectItem {

    public EnchantedItem(List<EnchantmentData> enchantmentData, String... tooltip) {
        this.addEnchantments(enchantmentData.toArray(EnchantmentData[]::new));
        this.addTooltips(tooltip);
    }

    @Override
    public EnchantedItem setItem(Item item) {
        this.setStack(enchant(item));
        super.setItem(item);
        return this;
    }

    public static ItemStack enchant(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.addEnchantment(UNBREAKING, 1);

        return stack;
    }
}
