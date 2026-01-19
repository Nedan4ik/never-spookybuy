package ru.nedan.spookybuy.util.autojoin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.nedan.neverapi.etc.Config;

public class AutoJoinConfiguration {

    public static boolean enabled = false;
    public static String ANARCHY_TO_JOIN = "";
    public static String PASSWORD = "";
    public static long timeFromLogout = 3000;

    public static void serialize() {
        JsonObject thisObject = new JsonObject();

        thisObject.addProperty("enabled", enabled);
        thisObject.addProperty("anarchy", ANARCHY_TO_JOIN);
        thisObject.addProperty("password", PASSWORD);
        thisObject.addProperty("timeFromLogout", timeFromLogout);

        Config.saveData("./config/autojoin-spookybuy.nvr", thisObject);
    }

    public static void deserialize() {
        JsonElement element = Config.readData("./config/autojoin-spookybuy.nvr");

        if (element == null) return;

        JsonObject object = element.getAsJsonObject();

        enabled = object.get("enabled").getAsBoolean();
        ANARCHY_TO_JOIN = object.get("anarchy").getAsString();
        PASSWORD = object.get("password").getAsString();
        timeFromLogout = object.get("timeFromLogout").getAsLong();
    }

    public static int longToTicks() {
        return (int) (timeFromLogout / 50L);
    }
}
