package me.steep.steepclaims.objects;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.steep.datahandler.DataHandler;
import me.steep.steepclaims.SteepClaims;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("all")
public class SteepClaim { // TODO fix this class for only chunks.

    private class ClaimSettings {

        private final Map<UUID, Set<ClaimPermission>> playerPermissions = new HashMap<>();
        private boolean hidden = false;
        private boolean pvp = false;

        /**
         * Attempting to enable pvp in a claim that has hidden set to true will fail and return false.
         *
         * @param pvp
         * @return Whether the action succeeded.
         */
        public boolean setPvPEnabled(boolean pvp) {
            if(this.hidden && pvp) return false;
            this.pvp = pvp;
            return true;
        }

        public boolean getPvPEnabled() {
            return this.pvp;
        }

        /**
         * Attempting to enable hidden in a claim that has pvp set to true will fail and return false.
         *
         * @param hidden
         * @return Whether the action succeeded.
         */
        public boolean setHidden(boolean hidden) {
            if(this.pvp && hidden) return false;
            this.hidden = hidden;
            return true;
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

        @Nullable
        private Set<ClaimPermission> getPermissions(UUID id) {
            return playerPermissions.getOrDefault(id, null);
        }
    }

    private final UUID ownerID;
    private Set<Chunk> chunks;
    private World world;
    private final ClaimSettings settings;

    public SteepClaim(UUID id, Set<Chunk> chunks) {
        this.ownerID = id;
        this.chunks = chunks;
        this.world = chunks[0].getWorld(); // TODO fix this
        this.settings = new ClaimSettings();
    }

    public UUID getOwnerUUID() {
        return this.ownerID;
    }

    public Set<Chunk> getClaimedChunks() {
        return this.chunks;
    }

    public ClaimSettings getSettings() {
        return this.settings;
    }

    public void saveToChunk() {
        putClaimData();
    }

    private void putClaimData() {
        new BukkitRunnable() {
            @Override
            public void run() { // TODO fix this too
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

                    putClaimDataOwnerID(chunk);
                    putClaimDataBoundingBox(chunk);
                    putClaimDataWorld(chunk);
                    putClaimDataPermissions(chunk);

                });

            }
        }.runTaskAsynchronously(SteepClaims.getInst());
    }

    private void putClaimDataOwnerID(Chunk chunk) {
        if(DataHandler.hasData(chunk, "claim_owner_id", DataType.UUID) &&
                ((UUID)DataHandler.getData(chunk, "claim_owner_id", DataType.UUID)).equals(this.ownerID)) return;
        DataHandler.setData(chunk, "claim_owner_id", DataType.UUID, this.ownerID);
    }

    private void putClaimDataWorld(Chunk chunk) {
        if(DataHandler.hasData(chunk, "claim_world", DataType.UUID) &&
                ((UUID)DataHandler.getData(chunk, "claim_world", DataType.UUID)).equals(this.world.getUID())) return;
        DataHandler.setData(chunk, "claim_world", DataType.UUID, this.world.getUID());
    }

    private void putClaimDataBoundingBox(Chunk chunk) {
        if(DataHandler.hasData(chunk, "claim_boundingbox", DataType.BOUNDING_BOX) &&
                ((UUID)DataHandler.getData(chunk, "claim_boundingbox", DataType.BOUNDING_BOX)).equals(this.boundingBox)) return;
        DataHandler.setData(chunk, "claim_boundingbox", DataType.BOUNDING_BOX, this.boundingBox);
    }

    private void putClaimDataPermissions(Chunk chunk) {
        String permissions = getPermissionString(this.ownerID);
        StringBuilder permissionsKey = new StringBuilder("claim_permissions_");
        permissionsKey.append(this.ownerID);
        if(DataHandler.hasData(chunk, permissionsKey.toString(), DataType.STRING) &&
                ((UUID)DataHandler.getData(chunk, permissionsKey.toString(), DataType.STRING)).equals(permissions)) return;
        DataHandler.setData(chunk, permissionsKey.toString(), DataType.STRING, permissions);
    }

    private String getPermissionString(UUID id) {
        // TODO on claim creation do everything async (check for worldguard regions too)
        StringBuilder builder = new StringBuilder();
        this.settings.getPermissions(id).forEach(permission -> builder.append(builder.isEmpty() ? permission.toString().toUpperCase() + "," : "," + permission
                .toString().toUpperCase()));
        return builder.toString();
    }


}
