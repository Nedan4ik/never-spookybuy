package ru.nedan.spookybuy.items.data;

import net.minecraft.entity.effect.StatusEffect;

public record EffectData(StatusEffect effectType, int duration, int amplifier) {
    public static EffectData of(StatusEffect effectType, int seconds, int amplifier) {
        return new EffectData(effectType, seconds * 20, amplifier);
    }

    public static EffectData instant(StatusEffect effectType, int amplifier) {
        return new EffectData(effectType, 1, amplifier);
    }
}
