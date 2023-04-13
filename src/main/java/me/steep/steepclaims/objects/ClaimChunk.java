package me.steep.steepclaims.objects;

import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.Chunk;

public class ClaimChunk {

    public static BoundingBox getBoundingBox(Chunk chunk) {
        return new BoundingBox(chunk.getBlock(0, chunk.getWorld().getMinHeight(), 0).getLocation(),
                chunk.getBlock(15, chunk.getWorld().getMaxHeight() - 1, 15).getLocation());
    }

}
