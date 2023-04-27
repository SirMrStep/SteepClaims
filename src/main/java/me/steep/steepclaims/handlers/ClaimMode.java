package me.steep.steepclaims.handlers;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.steep.datahandler.DataHandler;
import me.steep.steepclaims.objects.Format;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@SuppressWarnings("all")
public class ClaimMode {

    public static boolean isClaiming(Player player) {
        return DataHandler.hasData(player, "smpt_claiming", DataType.BOOLEAN);
    }

    public static void enableClaiming(Player player) {
        DataHandler.setDataBoolean(player, "smpt_claiming", true);
        player.sendMessage(Format.color("&aYou are now claiming. Please select the 4 corners of your claim"));
        player.sendMessage(Format.color("&aor punch while sneaking to cancel. Remember that the corners"));
        player.sendMessage(Format.color("&amust line up to create a square or rectangle."));
    }

    /*public static void addCorner(Player player) {
        Chunk
    }*/

    public static void disableClaiming(Player player) {
        if(DataHandler.hasData(player, "smpt_claiming_corner1", DataType.LOCATION)) DataHandler.removeData(player, "smpt_claiming_corner1");
    }

    //public static void claim(Player player, Loc)

}
