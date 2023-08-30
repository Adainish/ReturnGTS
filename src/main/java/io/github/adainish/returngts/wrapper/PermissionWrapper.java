package io.github.adainish.returngts.wrapper;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class PermissionWrapper
{
    public static String userPermission = "returngts.user.base";
    public static String adminPermission = "returngts.admin";
    public void registerPermissions() {
        registerCommandPermission(userPermission, "The base permission players need to use it");
        registerCommandPermission(adminPermission, "The admin permission");
    }
    public static void registerCommandPermission(String s) {
        if (s == null || s.isEmpty()) {
            return;
        }
        PermissionAPI.registerNode(s, DefaultPermissionLevel.NONE, s);
    }

    public static void registerCommandPermission(String s, String description) {
        if (s == null || s.isEmpty()) {
            return;
        }
        PermissionAPI.registerNode(s, DefaultPermissionLevel.NONE, description);
    }
}
