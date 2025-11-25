package com.zonty.farmingplugin.zontyFamingPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class ZontyFamingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Jeah boi");
        getServer().getPluginManager().registerEvents(new DestructionListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
