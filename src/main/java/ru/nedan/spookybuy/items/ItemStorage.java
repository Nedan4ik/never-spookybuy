package ru.nedan.spookybuy.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.nedan.spookybuy.items.data.AttributeData;
import ru.nedan.spookybuy.items.data.EffectData;
import ru.nedan.spookybuy.items.data.EnchantmentData;
import ru.nedan.spookybuy.items.impl.EnchantedItem;
import ru.nedan.spookybuy.items.impl.PotionItem;
import ru.nedan.spookybuy.items.impl.SphereItem;
import ru.nedan.spookybuy.items.impl.TalismanItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;
import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.*;
import static net.minecraft.entity.attribute.EntityAttributes.*;
import static net.minecraft.entity.effect.StatusEffects.*;
import static net.minecraft.item.Items.*;

public class ItemStorage {
    public static final List<CollectItem> ALL = new ArrayList<>();
    private static final List<EnchantmentData> DEFAULT_FOR_ARMOR;

    static {
        DEFAULT_FOR_ARMOR = Arrays.asList(
                EnchantmentData.of(UNBREAKING, 5),
                EnchantmentData.of(PROTECTION, 5),
                EnchantmentData.of(PROJECTILE_PROTECTION, 5),
                EnchantmentData.of(BLAST_PROTECTION, 5),
                EnchantmentData.of(MENDING, 1),
                EnchantmentData.of(FIRE_PROTECTION, 5)
        );

        reload();
    }

    public static void reload() {
        ALL.clear();

        ALL.addAll(Arrays.asList(
                new CollectItem()
                        .setName("Зачарованное золотое яблоко")
                        .setItem(ENCHANTED_GOLDEN_APPLE),
                new CollectItem()
                        .setName("Золотое яблоко")
                        .setItem(GOLDEN_APPLE),
                new EnchantedItem(contact(EnchantmentData.of(AQUA_AFFINITY, 1),
                        EnchantmentData.of(RESPIRATION, 3)))
                        .setItem(NETHERITE_HELMET)
                        .setName("Шлем крушителя")
                        .setMaxSellPrice(15000000),
                new EnchantedItem(contact())
                        .setMaxSellPrice(15000000)
                        .setName("Нагрудник крушителя")
                        .setItem(NETHERITE_CHESTPLATE),
                new EnchantedItem(contact())
                        .setMaxSellPrice(15000000)
                        .setName("Поножи крушителя")
                        .setItem(NETHERITE_LEGGINGS),
                new EnchantedItem(contact(EnchantmentData.of(DEPTH_STRIDER, 3),
                        EnchantmentData.of(FEATHER_FALLING, 4),
                        EnchantmentData.of(SOUL_SPEED, 3)))
                        .setMaxSellPrice(15000000)
                        .setName("Ботинки крушителя")
                        .setItem(NETHERITE_BOOTS),
                new EnchantedItem(create(EnchantmentData.of(CHANNELING, 1),
                        EnchantmentData.of(FIRE_ASPECT, 2),
                        EnchantmentData.of(IMPALING, 5),
                        EnchantmentData.of(LOYALTY, 3),
                        EnchantmentData.of(MENDING, 1),
                        EnchantmentData.of(SHARPNESS, 7),
                        EnchantmentData.of(UNBREAKING, 5)),
                        "Детекция III",
                        "Яд III",
                        "Возвращение",
                        "Вампиризм II",
                        "Опытный III",
                        "Окисление II",
                        "Ступор III",
                        "Притяжение II",
                        "Подрывник",
                        "Скаут III")
                        .setName("Трезубец крушителя")
                        .setItem(TRIDENT),
                new EnchantedItem(create(EnchantmentData.of(BANE_OF_ARTHROPODS, 7),
                        EnchantmentData.of(SHARPNESS, 7),
                        EnchantmentData.of(SMITE, 7),
                        EnchantmentData.of(FIRE_ASPECT, 2),
                        EnchantmentData.of(LOOTING, 5),
                        EnchantmentData.of(MENDING, 1),
                        EnchantmentData.of(SWEEPING, 3),
                        EnchantmentData.of(UNBREAKING, 5)),
                        "Вампиризм II", "Детекция III", "Опытный III", "Яд III", "Окисление II")
                        .setName("Меч крушителя")
                        .setItem(NETHERITE_SWORD)
                        .setMaxSellPrice(15000000),
                new EnchantedItem(create(EnchantmentData.of(MULTISHOT, 1),
                        EnchantmentData.of(PIERCING, 5),
                        EnchantmentData.of(QUICK_CHARGE, 3),
                        EnchantmentData.of(MENDING, 1),
                        EnchantmentData.of(UNBREAKING, 3)))
                        .setName("Арбалет крушителя")
                        .setItem(CROSSBOW),
                new EnchantedItem(create(EnchantmentData.of(EFFICIENCY, 10),
                        EnchantmentData.of(FORTUNE, 5),
                        EnchantmentData.of(MENDING, 1),
                        EnchantmentData.of(UNBREAKING, 5)),
                        "Магнит", "Пингер", "Паутина", "Бульдозер II", "Авто-Плавка", "Опытный III")
                        .setName("Кирка крушителя")
                        .setMaxSellPrice(25000000)
                        .setItem(NETHERITE_PICKAXE),
                new EnchantedItem(create(EnchantmentData.of(SHARPNESS, 8)))
                        .setName("Заострённый меч")
                        .setItem(NETHERITE_SWORD),
                new CollectItem()
                        .setName("Божья Аура")
                        .setItem(PHANTOM_MEMBRANE)
                        .setTag("effect-item-god"),
                new CollectItem()
                        .setName("Драконий скин")
                        .setItem(PAPER)
                        .setStack(enchant(PAPER))
                        .setTag("trap-skin-item-dragon"),
                new CollectItem()
                        .setName("Неизбежный скин")
                        .setItem(PAPER)
                        .setStack(enchant(PAPER))
                        .setTag("trap-skin-item-inevitable"),
                new CollectItem()
                        .setName("Серебро")
                        .setItem(IRON_NUGGET)
                        .setTag("\"spookystash:currency\":\"silver\""),
                new CollectItem()
                        .setName("Алмаз")
                        .setItem(DIAMOND),
                new CollectItem()
                        .setName("Книга починка")
                        .setItem(ENCHANTED_BOOK)
                        .addEnchantments(EnchantmentData.of(MENDING, 1)),
                new CollectItem()
                        .setName("Элитры")
                        .setItem(ELYTRA)
                        .addEnchantments(
                                EnchantmentData.of(UNBREAKING, 5),
                                EnchantmentData.of(MENDING, 1)
                        ),
                new CollectItem()
                        .setName("Божье касание")
                        .setItem(GOLDEN_PICKAXE)
                        .setStack(enchant(GOLDEN_PICKAXE))
                        .setTag("spawner-item-spawner-break")
                        .setMaxSellPrice(25000000),
                new CollectItem()
                        .setName("Мощный удар")
                        .setItem(GOLDEN_PICKAXE)
                        .setStack(enchant(GOLDEN_PICKAXE))
                        .setTag("bedrock-item-bedrock-break")
                        .setMaxSellPrice(25000000),
                new CollectItem()
                        .setName("Молот Тора")
                        .setItem(NETHERITE_PICKAXE)
                        .setStack(enchant(NETHERITE_PICKAXE))
                        .setTag("radius-item-mega-buldozer")
                        .setMaxSellPrice(25000000),
                new CollectItem()
                        .setName("Трапка")
                        .setItem(NETHERITE_SCRAP)
                        .setTag("schematic-item-trap"),
                new CollectItem()
                        .setName("Отмычка к сферам")
                        .setItem(TRIPWIRE_HOOK)
                        .setStack(enchant(TRIPWIRE_HOOK))
                        .setTag("spheres"),
                new CollectItem()
                        .setName("Спавнер")
                        .setItem(SPAWNER)
                        .setMaxSellPrice(40000000),
                new CollectItem()
                        .setName("Маяк")
                        .setItem(BEACON),
                new CollectItem()
                        .setName("Череп визер-скелета")
                        .setItem(WITHER_SKELETON_SKULL),
                new CollectItem()
                        .setName("Голова дракона")
                        .setItem(DRAGON_HEAD),
                new CollectItem()
                        .setName("Таер блэк")
                        .setItem(TNT)
                        .setTag("tnt-item-black"),
                new CollectItem()
                        .setName("Таер вайт")
                        .setItem(TNT)
                        .setTag("tnt-item-white"),
                new PotionItem(true, 16738740,
                        EffectData.of(SLOWNESS, 10, 8),
                        EffectData.of(SPEED, 20, 4),
                        EffectData.of(BLINDNESS, 5, 8),
                        EffectData.of(GLOWING, 180, 0))
                        .setName("Хлопушка"),
                new PotionItem(true, 16777215,
                        EffectData.of(REGENERATION, 60, 1), EffectData.of(INVISIBILITY, 600, 1), EffectData.instant(INSTANT_HEALTH, 1))
                        .setName("Святая вода"),
                new PotionItem(true, 10040115,
                        EffectData.of(STRENGTH, 30, 4), EffectData.of(SLOWNESS, 30, 3))
                        .setName("Зелье гнева"),
                new PotionItem(true, 65535,
                        EffectData.of(RESISTANCE, 600, 0), EffectData.of(FIRE_RESISTANCE, 600, 0), EffectData.of(HEALTH_BOOST, 60, 2), EffectData.of(INVISIBILITY, 900, 2))
                        .setName("Зелье палладина"),
                new PotionItem(true, 3355443,
                        EffectData.of(STRENGTH, 60, 3), EffectData.of(SPEED, 300, 2), EffectData.of(HASTE, 60, 0), EffectData.instant(INSTANT_DAMAGE, 1))
                        .setName("Зелье ассасина"),
                new PotionItem(true, 3329330,
                        EffectData.of(POISON, 20, 0), EffectData.of(WITHER, 20, 0), EffectData.of(SLOWNESS, 20, 2), EffectData.of(HUNGER, 20, 4), EffectData.of(GLOWING, 20, 0))
                        .setName("Зелье радиации"),
                new PotionItem(true, 4737096,
                        EffectData.of(WEAKNESS, 90, 0), EffectData.of(MINING_FATIGUE, 10, 0), EffectData.of(WITHER, 90, 2), EffectData.of(BLINDNESS, 10, 0))
                        .setName("Снотворное"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODY0MTkwMCwKICAicHJvZmlsZUlkIiA6ICIxNzRjZmRiNGEzY2I0M2I1YmZjZGU0MjRjM2JiMmM2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJhZWwxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lN2E3YWU3Y2RjZjYxNmU4YjdhNDIyMWE2MjFiMjQzNTc1M2M2MGVkNmEyNThlYTA2MGRhZTMwMDJmZmU5ZTI4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", new int[]{-1582985800,1375286202,-1125478243,595287099},
                        AttributeData.of(GENERIC_MOVEMENT_SPEED, 0.07, MULTIPLY_BASE), AttributeData.of(GENERIC_MAX_HEALTH, -4, ADDITION), AttributeData.of(GENERIC_ATTACK_SPEED, 0.13, MULTIPLY_BASE), AttributeData.of(GENERIC_ARMOR, 2, ADDITION), AttributeData.of(GENERIC_ATTACK_DAMAGE, 3, ADDITION))
                        .setName("Сфера Хаоса"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", new int[]{-2101547208,2058105556,-1495491604,1400184240},
                        AttributeData.of(GENERIC_ATTACK_SPEED, 0.15, MULTIPLY_BASE), AttributeData.of(GENERIC_ATTACK_DAMAGE, 2, ADDITION))
                        .setName("Сфера Сатира"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", new int[]{-1896649230,-398640094,-1320063783,945063190},
                        AttributeData.of(GENERIC_MOVEMENT_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_MAX_HEALTH, 4, ADDITION), AttributeData.of(GENERIC_ATTACK_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_ARMOR, 1, ADDITION))
                        .setName("Сфера Бестии"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzc3NDI1NSwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE2YWRjNmJhZmNiNTdmZDcwN2RlZTdkZDZhNzM2ZmUxMjY3MTFkNTNhMWZkNmNlNzg5ZGE0MWIzYmUxM2YyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", new int[]{2124864246,465122563,-1511078600,-555424585},
                        AttributeData.of(GENERIC_MAX_HEALTH, -2, ADDITION), AttributeData.of(GENERIC_ATTACK_DAMAGE, 6, ADDITION), AttributeData.of(GENERIC_ARMOR, -2, ADDITION))
                        .setName("Сфера Ареса"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODUzMjE4MywKICAicHJvZmlsZUlkIiA6ICI1OGZmZWI5NTMxNGQ0ODcwYTQwYjVjYjQyZDRlYTU5OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa2luREJuZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzYzExOGQ2OTZkOTEwZTU0ZGUwMmNhNGQ4MDc1NDNmOWIxOGMwMDhjOTgzOGQyZmY2OTM3NzYyMmZiMWQzMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", new int[]{-1308798306,1128020291,-1307054059,-1317369961},
                        AttributeData.of(GENERIC_MAX_HEALTH, 4, ADDITION), AttributeData.of(GENERIC_ARMOR, 2, ADDITION))
                        .setName("Сфера Гидры"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODU4MjQ5MSwKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M2ODAzZTZkNTY2N2EyZDYxMDYyOGJjM2IzMmY4NjNjZGE0OTVjNDY1NjE2ZGU2NTVjYjMyOTkzM2I2MWFmNzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", new int[]{1858435695,-517000716,-1346102858,1389869685},
                        AttributeData.of(GENERIC_ATTACK_DAMAGE, 2, ADDITION), AttributeData.of(GENERIC_MAX_HEALTH, 2, ADDITION))
                        .setName("Сфера Икара"),
                new SphereItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlOTY5ODQ1OGI3ODQxYzk2YWU0ZjI0ZWM4NGFlMDE3MjQxMDA2NDFjNTY0ZTJhN2IxODVmNDA2ZThlZDIzIn19fQ==", new int[]{654012711,1596536861,-2049048826,-850597571},
                        AttributeData.of(GENERIC_ARMOR_TOUGHNESS, 3, ADDITION), AttributeData.of(GENERIC_MOVEMENT_SPEED, -0.15, MULTIPLY_BASE), AttributeData.of(GENERIC_ARMOR, 3, ADDITION))
                        .setName("Сфера Титана"),
                new SphereItem("ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", new int[]{-122635583,2062758360,-2035264015,874042263},
                        AttributeData.of(GENERIC_LUCK, 1, ADDITION), AttributeData.of(GENERIC_MAX_HEALTH, 2, ADDITION))
                        .setName("Сфера Эрида"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, 4, ADDITION), AttributeData.of(GENERIC_ATTACK_DAMAGE, 3, ADDITION), AttributeData.of(GENERIC_ARMOR, 2, ADDITION), AttributeData.of(GENERIC_ARMOR_TOUGHNESS, 2, ADDITION))
                        .setName("Талисман Крушителя"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, -4, ADDITION), AttributeData.of(GENERIC_MOVEMENT_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_ATTACK_DAMAGE, 7, ADDITION))
                        .setName("Талисман Карателя"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, 2, ADDITION), AttributeData.of(GENERIC_MOVEMENT_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_ATTACK_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_ATTACK_DAMAGE, 4, ADDITION), AttributeData.of(GENERIC_ARMOR, -3, ADDITION))
                        .setName("Талисман Раздора"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, -4, ADDITION), AttributeData.of(GENERIC_ATTACK_DAMAGE, 2, ADDITION), AttributeData.of(GENERIC_ARMOR, 2, ADDITION))
                        .setName("Талисман Тирана"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, -4, ADDITION), AttributeData.of(GENERIC_ATTACK_DAMAGE, 5, ADDITION))
                        .setName("Талисман Ярости"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, 2, ADDITION), AttributeData.of(GENERIC_ATTACK_SPEED, 0.15, MULTIPLY_BASE), AttributeData.of(GENERIC_MOVEMENT_SPEED, 0.15, MULTIPLY_BASE))
                        .setName("Талисман Вихря"),
                new TalismanItem(AttributeData.of(GENERIC_MAX_HEALTH, 1.5, ADDITION), AttributeData.of(GENERIC_ARMOR, 1.5, ADDITION))
                        .setName("Талисман Мрака"),
                new TalismanItem(AttributeData.of(GENERIC_ATTACK_SPEED, 0.1, MULTIPLY_BASE), AttributeData.of(GENERIC_ATTACK_DAMAGE, 2.5, ADDITION))
                        .setName("Талисман Демона")
        ));
    }

    private static ItemStack enchant(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.addEnchantment(UNBREAKING, 1);

        return stack;
    }

    private static List<EnchantmentData> contact(EnchantmentData... enchantmentData) {
        List<EnchantmentData> data = new ArrayList<>(create(enchantmentData));
        data.addAll(DEFAULT_FOR_ARMOR);

        return data;
    }

    private static List<EnchantmentData> create(EnchantmentData... enchantmentData) {
        return Arrays.asList(enchantmentData);
    }
}
