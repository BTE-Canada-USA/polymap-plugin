package dev.nachwahl.btemap.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.function.block.Counter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.nachwahl.btemap.BTEMap;
import dev.nachwahl.btemap.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@CommandAlias("map")
public class MapCommand extends BaseCommand {

    @Dependency
    private BTEMap plugin;

    private YamlDocument settings = plugin.getPluginConfig().getSettings();
    private String prefix = settings.getString("prefix");

    @Subcommand("link")
    @CommandPermission("map.link")
    public void onLink(CommandSender sender) {
        sender.sendMessage(
            new MessageBuilder(settings, "responses.map.link.loading")
                .setPrefix(prefix)
                .build()
        );
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                int code = new Random().nextInt(900000) + 100000;
                try {
                    PreparedStatement checkLinkedPs = plugin.getSqlConnector().getConnection().prepareStatement("SELECT * FROM User WHERE minecraftUUID = ?");
                    checkLinkedPs.setString(1, ((Player) sender).getUniqueId().toString());
                    ResultSet checkLinkedRs = checkLinkedPs.executeQuery();
                    if(checkLinkedRs.next()) {
                        sender.sendMessage(
                            new MessageBuilder(settings, "responses.map.link.already-linked")
                                .setPrefix(prefix)
                                .build()
                        );
                        return;
                    } else {

                        PreparedStatement deleteCodePs = plugin.getSqlConnector().getConnection().prepareStatement("DELETE FROM LinkCodes WHERE playerUUID = ?");
                        deleteCodePs.setString(1, ((Player) sender).getUniqueId().toString());
                        deleteCodePs.execute();

                        PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO `LinkCodes` (`id`, `code`, `playerUUID`, `createdAt`) VALUES (?, ?, ?, ?);");
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, String.valueOf(code));
                        ps.setString(3, ((Player) sender).getUniqueId().toString());
                        ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                        ps.execute();
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    sender.sendMessage(
                        new MessageBuilder(settings, "responses.map.link.error")
                            .setPrefix(prefix)
                            .build()
                    );
                    return;
                }
                sender.sendMessage(
                    new MessageBuilder(settings, "responses.map.link.code")
                        .setPrefix(prefix)
                        .replace(settings, "url", "map-url")
                        .build()
                );

            }
        });
    }

    @Default
    @Subcommand("create")
    @CommandPermission("map.create")
    public void onCreate(CommandSender sender){
        double lat = 0.0;
        double lon = 0.0;
        String city = "n/A";
        Player player = (Player) sender;
        LocalSession sm = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
        if(sm == null) {
            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.please-select")
                    .setPrefix(prefix)
                    .build()
            );
            return;
        }
        Region region = null;
        try {
            region = sm.getSelection(WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelectionWorld());
        } catch (IncompleteRegionException e) {
            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.please-select")
                    .setPrefix(prefix)
                    .build()
            );
            return;
        }
        if(region == null) {
            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.please-select")
                    .setPrefix(prefix)
                    .build()
            );
            return;
        }
        List<BlockVector2D> poly = null;
        try {
            poly = region.polygonize(50);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.too-large")
                    .setPrefix(prefix)
                    .replace("size", "50")
                    .build()
            );
            return;
        }
        sender.sendMessage(
            new MessageBuilder(settings, "responses.map.create.loading")
                .setPrefix(prefix)
                .build()
        );



        String coords = "[";
        for (BlockVector2D vector2D : poly) {
            System.out.println(BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1] + ", " + BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0]);
            System.out.println(Arrays.toString(BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())));
            lat = BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1];
            lon = BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0];
            coords = coords + "[" + BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1] + ", " + BTEMap.toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0] + "],";

        }
        coords = coords.substring(0, coords.length() - 1);
        coords = coords + "]";
        System.out.println(coords);
        URL url = null;


        try {
            url = new URL("https://nominatim.openstreetmap.org/reverse.php?osm_type=N&format=json&zoom=18&lon=" + lon + "&accept-language=de&lat=" + lat);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String response = "";
            for (int i; (i = reader.read()) >= 0;)
                response += (char) i;

            System.out.println(response);
            JsonElement jsonElement = new JsonParser().parse(response);
            if(jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city") != null) {
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village") != null){
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town") != null) {
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town").getAsString();
            } else {
                city = "n/A";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sm.getRegionSelector(WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelectionWorld()).clear();

        try {
            region.contract(new Vector().setY(region.getHeight()-1));
        } catch (RegionOperationException e) {
            e.printStackTrace();
        }

        Region finalRegion = region;
        String finalCoords = coords;
        String finalCity = city;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            UUID uuid = UUID.randomUUID();
            Counter counter = new Counter();
            RegionVisitor visitor = new RegionVisitor(finalRegion, counter);
            Operations.completeBlindly(visitor);
            try {
                PreparedStatement checkUserPs = plugin.getSqlConnector().getConnection().prepareStatement("SELECT * FROM User WHERE minecraftUUID = ?");
                checkUserPs.setString(1, player.getUniqueId().toString());
                ResultSet checkUserRs = checkUserPs.executeQuery();

                if(checkUserRs.next()) {
                    PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO Region (createdAt, id, username, userUUID, data, city, area, description, ownerID) VALUES (?, ?, ?, ?, ?, ?, ?, '', ?)");
                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                    ps.setString(2, uuid.toString());
                    ps.setString(3, player.getName());
                    ps.setString(4, player.getUniqueId().toString());
                    ps.setString(5,finalCoords);
                    ps.setString(6, finalCity);
                    ps.setInt(7, counter.getCount());
                    ps.setString(8, checkUserRs.getString("id"));
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO Region (createdAt, id, username, userUUID, data, city, area, description) VALUES (?, ?, ?, ?, ?, ?, ?, '')");
                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                    ps.setString(2, uuid.toString());
                    ps.setString(3, player.getName());
                    ps.setString(4, player.getUniqueId().toString());
                    ps.setString(5,finalCoords);
                    ps.setString(6, finalCity);
                    ps.setInt(7, counter.getCount());
                    ps.executeUpdate();
                }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.success")
                    .setPrefix(prefix)
                    .build()
            );
            sender.sendMessage(
                new MessageBuilder(settings, "responses.map.create.see-online")
                    .setPrefix(prefix)
                    .replace(settings, "url", "map-url")
                    .build()
            );
        });
    }





}
