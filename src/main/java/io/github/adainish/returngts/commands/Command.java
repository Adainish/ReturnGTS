package io.github.adainish.returngts.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import io.github.adainish.returngts.util.PermissionUtil;
import io.github.adainish.returngts.wrapper.PermissionWrapper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class Command
{
    public static LiteralArgumentBuilder<CommandSource> getCommand()
    {
        return Commands.literal("gts")
                .requires(cs -> PermissionUtil.checkPermAsPlayer(cs, PermissionWrapper.userPermission))
                .executes(cc -> {
                    try {
                        GTSPlayer gtsPlayer = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                        if (gtsPlayer != null) {
                            ReturnGTS.gts.openGTSMenu(gtsPlayer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        cc.getSource().sendFeedback(new StringTextComponent("Something went wrong while executing the command, please contact a member of Staff if this issue persists"), true);
                        return 1;
                    }
                    return 1;
                })
                ;
    }
}
