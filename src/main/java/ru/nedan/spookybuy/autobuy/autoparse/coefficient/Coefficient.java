package ru.nedan.spookybuy.autobuy.autoparse.coefficient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import ru.nedan.spookybuy.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Coefficient {
    @Getter
    private static final List<Coefficient> all = new ArrayList<>();

    private BigDecimal forPrice;
    private Pair<Double, Double> decimalPair;
    private CoefficientType type;

    public static Coefficient findForPrice(BigDecimal rawPrice) {
        return all.stream()
                .filter(coefficient -> coefficient.getType().test(rawPrice, coefficient.forPrice))
                .findFirst()
                .orElse(null);
    }

    /**
     * Редактирует коэффициент
     * @param forPrice - прайс, для которого использовать, null если использовать старый
     * @param type - тип коэффициента, null если использовать старый
     */
    public void edit(@Nullable BigDecimal forPrice, @Nullable Double left, Double right, @Nullable CoefficientType type) {
        if (forPrice != null) {
            this.forPrice = forPrice;
        }

        if (left != null) {
            this.decimalPair.setLeft(left);
        }

        if (right != null) {
            this.decimalPair.setRight(right);
        }

        if (type != null) {
            this.type = type;
        }
    }

    public static Coefficient createDefault() {
        return new Coefficient(
                BigDecimal.ZERO,
                new Pair<>(0.8, 0.9),
                CoefficientType.OVER
        );
    }

    public void serialize(JsonArray coefficients) {
        JsonObject thisObject = new JsonObject();

        thisObject.addProperty("type", type.name());
        thisObject.addProperty("buyCoef", decimalPair.getLeft());
        thisObject.addProperty("sellCoef", decimalPair.getRight());
        thisObject.addProperty("forPrice", forPrice);

        coefficients.add(thisObject);
    }

    public static void deserialize(JsonArray array) {
        all.clear();

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();

            String typeStr = obj.get("type").getAsString();
            double buyCoef = obj.get("buyCoef").getAsDouble();
            double sellCoef = obj.get("sellCoef").getAsDouble();
            BigDecimal forPrice = obj.get("forPrice").getAsBigDecimal();

            CoefficientType type = CoefficientType.valueOf(typeStr);

            all.add(new Coefficient(forPrice, new Pair<>(buyCoef, sellCoef), type));
        }
    }

}
