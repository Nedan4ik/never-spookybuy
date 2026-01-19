package ru.nedan.spookybuy.items.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.data.AttributeData;

public class SphereItem extends CollectItem {

    public SphereItem(String owner, int[] id, AttributeData... attributes) {
        this.setItem(Items.PLAYER_HEAD);
        this.setStack(withSkull(owner, id));

        this.addAttributes(attributes);
    }

    private static ItemStack withSkull(String owner, int[] id) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound main = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        NbtCompound texture = new NbtCompound();

        texture.putString("Value", owner);
        textures.add(texture);

        properties.put("textures", textures);
        skullOwner.put("Properties", properties);

        skullOwner.putIntArray("Id", id);
        main.put("SkullOwner", skullOwner);

        stack.setTag(main);

        return stack;
    }
}
