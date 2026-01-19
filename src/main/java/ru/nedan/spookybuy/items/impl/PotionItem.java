package ru.nedan.spookybuy.items.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.data.EffectData;

import static net.minecraft.enchantment.Enchantments.UNBREAKING;

public class PotionItem extends CollectItem {

    public PotionItem(boolean splash, int potionColor, EffectData... effects) {
        this.setItem(splash ? Items.SPLASH_POTION : Items.POTION);

        ItemStack raw = EnchantedItem.enchant(this.getItem());

        this.setStack(getPotionWithColor(raw, potionColor));
        this.addEffects(effects);
    }

    private static ItemStack getPotionWithColor(ItemStack stack, int value) {
        NbtCompound nbt = new NbtCompound();

        nbt.putInt("CustomPotionColor", value);

        stack.setTag(nbt);
        stack.addEnchantment(UNBREAKING, 1);

        return stack;
    }
}
