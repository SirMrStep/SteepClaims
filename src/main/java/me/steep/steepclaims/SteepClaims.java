package me.steep.steepclaims;

import me.steep.datahandler.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SteepClaims extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    public void initialize() {
        DataHandler.register(this);

        /*new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTaskLater()*/
    }

    private void initializeListeners() {
        PluginManager pm = Bukkit.getPluginManager();
    }

    private void initializeCommands() {

    }

    private static SteepClaims instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    public static SteepClaims getInst() {
        return instance;
    }
}
