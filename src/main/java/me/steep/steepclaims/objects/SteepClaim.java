package me.steep.steepclaims.objects;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.steep.datahandler.DataHandler;
import me.steep.steepclaims.SteepClaims;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("all")
public class SteepClaim {

    private class ClaimSettings {

        private final Map<UUID, Set<ClaimPermission>> playerPermissions = new HashMap<>();
        private boolean hidden = false;

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public boolean getHidden() {
            return this.hidden;
        }

        public boolean hasPermission(@NotNull Player player, @NotNull ClaimPermission permission) {
            if(getPermissions(player) == null) return false;
            return getPermissions(player).contains(permission);
        }

        /**
         *
         * @param player
         * @param permission
         * @return False if the player already had this ClaimPermission.
         */
        public boolean trustPlayer(@NotNull Player player, @NotNull ClaimPermission permission) {

            if(hasPermission(player, permission)) return false;
            
            if(!playerPermissions.containsKey(player.getUniqueId())) {
                playerPermissions.put(player.getUniqueId(), new HashSet<>(Set.of(permission)));
                return true;
            }

            getPermissions(player).add(permission);
            return true;
        }

        /**
         * @param player The player.
         * @param permission The ClaimPermission to remove.
         * @return False if the player wasn't trusted with this ClaimPermission.
         */
        public boolean untrustPlayer(@NotNull Player player, @NotNull ClaimPermission permission) {

            if(!hasPermission(player, permission)) return false;
            
            if(permission == ClaimPermission.ALL) {
                playerPermissions.remove(player.getUniqueId());
                return true;
            }
            // This is a more detailed check (hasPermission() will return false even if the player is not in the playerPermission map).
            if(!getPermissions(player).contains(permission)) return false;

            getPermissions(player).remove(permission);
            return true;

        }

        @Nullable
        private Set<ClaimPermission> getPermissions(Player player) {
            return playerPermissions.getOrDefault(player.getUniqueId(), null);
        }
    }

    private final UUID ownerID;
    private BoundingBox boundingBox;
    private World world;
    private final ClaimSettings settings;

    public SteepClaim(UUID id, Location corner1, Location corner2) {
        this.ownerID = id;
        if(corner1.getY() < corner1.getWorld().getMaxHeight()) corner1.setY(corner1.getWorld().getMaxHeight());
        if(corner2.getY() > corner2.getWorld().getMinHeight()) corner2.setY(corner2.getWorld().getMinHeight());
        this.boundingBox = BoundingBox.of(corner1, corner2);
        this.world = corner1.getWorld();
        this.settings = new ClaimSettings();
    }

    public UUID getOwnerUUID() {
        return this.ownerID;
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public ClaimSettings getSettings() {
        return this.settings;
    }

    public void saveToChunk() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int xfrom = boundingBox.getMin().getBlockX();
                int zfrom = boundingBox.getMin().getBlockZ();
                int xto = boundingBox.getMax().getBlockX();
                int zto = boundingBox.getMax().getBlockZ();
                Set<Chunk> chunks = new HashSet<>();
                for(int x = xfrom; x <= xto; x++) {
                    for(int z  = zfrom; z <= zto; z++) {

                        //TODO CHECK IF CLAIM INTERFERES WITH ANY WORLDGUARD CLAIM THAT IT SHOULDNT (do worldguard regions have ID's?)

                        Chunk chunk = world.getChunkAt(world.getBlockAt(x, 0, z));
                        if(chunks.contains(chunk)) continue;
                        chunks.add(chunk);

                    }
                }

                chunks.forEach(chunk -> {

                    // TODO add all claim data to chunk

                    if(DataHandler.hasData(chunk, "claim_owner_id", DataType.UUID)) { // TODO make this into small methods
                        // check if data is equal to our claim data, if not, set new data.
                    }

                });

            }
        }.runTaskAsynchronously(SteepClaims.getInst());
    }

    private String getPermissionString(Player player) {
        // TODO on claim creation do everything async (check for worldguard regions too)
        StringBuilder builder = new StringBuilder();
        this.settings.getPermissions(player).forEach(permission -> builder.append(builder.isEmpty() ? permission.toString().toUpperCase() + "," : "," + permission
                .toString().toUpperCase()));
        return builder.toString();
    }

}
