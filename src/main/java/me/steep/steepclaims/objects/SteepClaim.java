package me.steep.steepclaims.objects;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.steep.datahandler.DataHandler;
import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("all")
public class SteepClaim implements ConfigurationSerializable {

    // TODO fix this class for only chunks.

    private class ClaimSettings {

        public enum ClaimFlag {
            HIDDEN,
            PVP;
        }

        private Map<UUID, Set<ClaimPermission>> playerPermissions = new HashMap<>();
        private Map<ClaimFlag, Boolean> flags = new HashMap<>(Map.of(
                ClaimFlag.HIDDEN, false,
                ClaimFlag.PVP, false));

        public ClaimSettings(Map<UUID, Set<ClaimPermission>> playerPermissions, Map<ClaimFlag, Boolean> flags) {
            this.playerPermissions = playerPermissions;
            this.flags = flags;
        }

        public ClaimSettings() {
        }

        public boolean hasPermission(@NotNull Player player, @NotNull ClaimPermission permission) {
            Set<ClaimPermission> permissions = getPermissions(player);
            if(permissions == null) return false;
            return permissions.contains(permission);
        }

        /**
         *
         * @param player
         * @param permission
         * @return False if the player already had this ClaimPermission.
         */
        public boolean addPermission(@NotNull Player player, @NotNull ClaimPermission permission) {

            if(hasPermission(player, permission)) return false;

            if(!this.playerPermissions.containsKey(player.getUniqueId())) {
                this.playerPermissions.put(player.getUniqueId(), new HashSet<>(Set.of(permission)));
                return true;
            }

            getPermissions(player).add(permission);
            return true;

        }

        public void removePermission(@NotNull Player player, @NotNull ClaimPermission permission) {
            if(!this.playerPermissions.containsKey(player.getUniqueId()) || !getPermissions(player).contains(permission)) return;
            getPermissions(player).remove(permission);
            if(getPermissions(player).isEmpty()) {
                this.playerPermissions.remove(player.getUniqueId());
            }
        }

        public void setPermissions(Map<UUID, Set<ClaimPermission>> playerPermissions) {
            this.playerPermissions = playerPermissions;
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

        @NotNull
        public Map<UUID, Set<ClaimPermission>> getPermissions() {
            return this.playerPermissions;
        }

        @NotNull
        public Map<ClaimFlag, Boolean> getFlags() {
            return this.flags;
        }
    }

    private final UUID ownerID;
    private String name;
    private Set<Chunk> chunks;
    private final ClaimSettings settings;

    public SteepClaim(@NotNull UUID id, @NotNull String name, @NotNull Set<Chunk> chunks) {
        this.ownerID = id;
        this.name = name;
        this.chunks = chunks;
        this.settings = new ClaimSettings();
    }

    public SteepClaim(@NotNull UUID id, @NotNull String name, @NotNull Set<Chunk> chunks, ClaimSettings settings) {
        this.ownerID = id;
        this.name = name;
        this.chunks = chunks;
        this.settings = settings;
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

    private void saveToChunk() {
        this.chunks.forEach(chunk -> putClaimDataOwnerID(chunk));
    }

    public void save() {
        // save UUID-<name>.yml file
    }

    private void putClaimDataOwnerID(Chunk chunk) {
        if(DataHandler.hasData(chunk, "claim_owner_id", DataType.UUID) &&
                ((UUID)DataHandler.getData(chunk, "claim_owner_id", DataType.UUID)).equals(this.ownerID)) return;
        DataHandler.setData(chunk, "claim_owner_id", DataType.UUID, this.ownerID);
    }

    private Set<String> getSerializedChunks() {
        Set<String> chunksStrings = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        for(Chunk chunk : this.chunks) {
            builder = new StringBuilder();
            chunksStrings.add(builder.append(chunk.getWorld().getUID().toString().toUpperCase())
                    .append("|").append(chunk.getX()).append(",").append(chunk.getZ()).toString());
        }
        return chunksStrings;
    }

    private String getSerializedPermissionString(UUID uuid) {
        StringBuilder builder = new StringBuilder();
        this.settings.getPermissions(uuid).forEach(permission -> builder.append(builder.isEmpty() ? permission.toString().toUpperCase() + "," : "," + permission
                .toString().toUpperCase()));
        return builder.toString();
    }

    private Map<String, String> getSerializedPermissions() {
        Map<String, String> permissions = new HashMap<>();
        for(UUID uuid : this.settings.playerPermissions.keySet()) permissions.put(uuid.toString().toUpperCase(), getSerializedPermissionString(uuid));
        return permissions;
    }

    private Map<String, Boolean> getSerializedFlags() {
        Map<String, Boolean> flags = new HashMap<>();
        for(ClaimSettings.ClaimFlag flag : this.settings.flags.keySet()) flags.put(flag.toString().toUpperCase(), this.settings.getFlags().get(flag));
        return flags;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("owner", this.ownerID);
        result.put("chunks", getSerializedChunks());
        result.put("permissions", getSerializedPermissions());
        result.put("flags", getSerializedFlags());
        return result;
    }

    public SteepClaim deserialize(Map<String, Object> data) {
        //map of claimsettings TODO
        return new SteepClaim((UUID)data.get("owner"), (String)data.get("name"), (Set<Chunk>)data.get("chunks"), new ClaimSettings(
                //claimsettings TODO
        ));
    }


}
