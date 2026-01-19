package ru.nedan.spookybuy.items.data;

import net.minecraft.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public record EnchantmentData(Enchantment enchantment, int level) {
    public static EnchantmentData of(Enchantment enchantment, int level) {
        return new EnchantmentData(enchantment, level);
    }

    @Override
    public @NotNull String toString() {
        return "EnchantmentData{" +
                "enchantment='" + enchantment + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
