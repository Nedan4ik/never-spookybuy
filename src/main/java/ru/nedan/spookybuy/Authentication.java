package ru.nedan.spookybuy;

import lombok.Getter;
import lombok.Setter;
import ru.nedan.spookybuy.util.ws.Client;

import java.security.MessageDigest;

public class Authentication {
    @Getter
    @Setter
    private static String username;

    public static void auth() {
        if (username != null) return;

        String hardwareId = getHWID();
        Client.getInstance().sendAuthResponse(hardwareId);
    }

    public static String getHWID() {
        try{
            String toEncrypt =  System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuilder hexString = new StringBuilder();

            byte[] byteData = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
