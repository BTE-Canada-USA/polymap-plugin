package dev.nachwahl.btemap.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

public class Config {
    private JavaPlugin plugin;

    private YamlDocument connections;
    private YamlDocument settings;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.connections = createConfig("connections.yml", false);
    }

    @Nullable
    public YamlDocument getConnections() {
        return this.connections;
    }

    @Nullable
    public YamlDocument getSettings() {
        return this.settings;
    }

    @Nullable
    private YamlDocument createConfig(String name, boolean autoUpdate) {
        try {
            return YamlDocument.create(
                new File(plugin.getDataFolder(), name),
                plugin.getResource(name),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(autoUpdate).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build()
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
