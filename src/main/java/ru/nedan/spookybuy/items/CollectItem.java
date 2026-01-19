package ru.nedan.spookybuy.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ChatUtil;
import ru.nedan.spookybuy.items.data.AttributeData;
import ru.nedan.spookybuy.items.data.EffectData;
import ru.nedan.spookybuy.items.data.EnchantmentData;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Getter
public class CollectItem {
    private String name;
    private BigDecimal price = new BigDecimal(100);
    private Item item;
    private ItemStack stack;

    private final List<AttributeData> attributesData = Lists.newArrayList();
    private final List<EnchantmentData> enchantmentsData = Lists.newArrayList();
    private final List<String> tooltip = new ArrayList<>();
    private final List<EffectData> effects = new ArrayList<>();
    private String tag;
    private int maxSellPrice = 10000000;

    public static BiPredicate<ItemStack, CollectItem> CHECKER = (itemStack, abItem) -> {
        if (itemStack.getItem() != abItem.getItem()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();

        List<String> strings = itemStack.getTooltip(mc.player, TooltipContext.Default.ADVANCED).stream().map(text -> ChatUtil.stripTextFormat(text.getString())).collect(Collectors.toList());

        boolean tooltipMatch = abItem.tooltip.stream().allMatch(abLine ->
                strings.stream().anyMatch(line -> line.toLowerCase().contains(abLine.toLowerCase()))
        );

        boolean tagMatch = abItem.tag == null || abItem.tag.isEmpty() || (itemStack.getTag() != null && itemStack.getTag().toString().toLowerCase().contains(abItem.tag.toLowerCase()));

        Map<Enchantment, Integer> stackEnchants = EnchantmentHelper.get(itemStack);
        boolean enchantmentsMatch = abItem.enchantmentsData.stream()
                .allMatch(entry -> Objects.equals(stackEnchants.getOrDefault(entry.enchantment(), -1), entry.level()));

        boolean effectsMatch = abItem.effects.stream()
                .allMatch(effect -> {
                    if (itemStack.getItem() instanceof PotionItem) {
                        List<StatusEffectInstance> stackEffects = PotionUtil.getPotionEffects(itemStack);

                        return stackEffects.stream().anyMatch(e ->
                                e.getEffectType().equals(effect.effectType()) &&
                                        e.getDuration() >= effect.duration() &&
                                        e.getAmplifier() >= effect.amplifier());
                    }
                    return false;
                });

        return effectsMatch && tooltipMatch && tagMatch && enchantmentsMatch && containsAllAttributes(itemStack, abItem.attributesData);
    };

    private static boolean containsAllAttributes(ItemStack stack, List<AttributeData> attributes) {
        Multimap<EntityAttribute, EntityAttributeModifier> stackAttributes = stack.getAttributeModifiers(EquipmentSlot.OFFHAND);
        return stackAttributes.size() == attributes.size() &&
                stackAttributes.entries().stream()
                        .allMatch(entry -> containsAttribute(entry.getKey(), entry.getValue().getValue(), entry.getValue().getOperation(), attributes));
    }

    private static boolean containsAttribute(EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation, List<AttributeData> attributes) {
        return attributes.stream()
                .anyMatch(attr -> {
                    boolean matchesAttribute = attr.getAttribute() == attribute;
                    boolean matchesValue = attr.getValue() == value;
                    boolean matchesOperation = attr.getType() == operation;

                    return matchesAttribute && matchesValue && matchesOperation;
                });
    }

    public int getMaxSellPrice() {
        if (maxSellPrice == 10000000) {
            if (this.getItem() == Items.TOTEM_OF_UNDYING) return 50000000;
            if (this.getItem() == Items.PLAYER_HEAD) return 70000000;
        }

        return maxSellPrice;
    }

    public CollectItem setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public CollectItem setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemStack getStack() {
        if (stack == null) return this.item.getDefaultStack();

        return stack;
    }

    public CollectItem setMaxSellPrice(int maxSellPrice) {
        this.maxSellPrice = maxSellPrice;
        return this;
    }

    public CollectItem setName(String name) {
        this.name = name;
        return this;
    }

    public CollectItem setItem(Item item) {
        this.item = item;
        return this;
    }

    public CollectItem setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public CollectItem addAttributes(AttributeData... attributeData) {
        this.attributesData.addAll(Arrays.asList(attributeData));
        return this;
    }

    public CollectItem addEffects(EffectData... effectData) {
        this.effects.addAll(Arrays.asList(effectData));
        return this;
    }

    public CollectItem addEnchantments(EnchantmentData... enchantmentsData) {
        this.enchantmentsData.addAll(Arrays.asList(enchantmentsData));
        return this;
    }

    public CollectItem addTooltips(String... tooltips) {
        this.tooltip.addAll(Arrays.asList(tooltips));
        return this;
    }
}
