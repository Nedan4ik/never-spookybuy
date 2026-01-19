package ru.nedan.spookybuy.items.impl;

import net.minecraft.item.Items;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.data.AttributeData;

public class TalismanItem extends CollectItem {

    public TalismanItem(AttributeData... attributes) {
        this.setItem(Items.TOTEM_OF_UNDYING);
        this.setStack(EnchantedItem.enchant(Items.TOTEM_OF_UNDYING));
        this.addAttributes(attributes);
    }
}
