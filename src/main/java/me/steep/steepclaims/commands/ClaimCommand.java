package me.steep.steepclaims.commands;

import me.steep.datahandler.DataHandler;
import me.steep.steepclaims.handlers.ClaimMode;
import me.steep.steepclaims.objects.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClaimCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage("no");
            return true;
        }

        ClaimMode.enableClaiming(player);
        player.sendMessage(Format.color("&aYou are now claiming. Please select the 4 corners of your claim"));
        player.sendMessage(Format.color("&aor punch while sneaking to cancel. Remember that the corners"));
        player.sendMessage(Format.color("&amust line up to create a square or rectangle."));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
