package me.steep.steepclaims.listeners;

import me.steep.steepclaims.handlers.ClaimMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimInteractListener implements Listener {

    @EventHandler
    public void onClaimInteract(PlayerInteractEvent e) {

        if((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getPlayer().isSneaking()) {
            ClaimMode.disableClaiming(e.getPlayer());
            return true;
        }

        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;



    }

}
