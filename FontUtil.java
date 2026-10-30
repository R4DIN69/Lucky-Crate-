package com.luckycrates.util;

public final class FontUtil {
    private static final String NORMAL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";

    private FontUtil() {}

    public static String toSmallCaps(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = NORMAL.indexOf(c);
            if (idx != -1) {
                sb.append(SMALL_CAPS.charAt(idx));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
