package ru.nedan.spookybuy.items.data;

import lombok.Getter;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

@Getter
public class AttributeData {
    private final EntityAttribute attribute;
    private final double value;
    private final EntityAttributeModifier.Operation type;

    public AttributeData(EntityAttribute attribute, double value, EntityAttributeModifier.Operation type) {
        this.attribute = attribute;
        this.value = value;
        this.type = type;
    }

    public static AttributeData of(EntityAttribute attribute, double value, EntityAttributeModifier.Operation type) {
        return new AttributeData(attribute, value, type);
    }
}
