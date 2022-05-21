package pl.atakowiec.tabeditor;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.ArrayList;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    static WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
    static PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
    static ArrayList<CraftPlayer> tabPlayers = new ArrayList<>();
    static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        for(int i=0; i<80; i++) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "___"+(i+10));
            EntityPlayer player = new EntityPlayer(getMinecraftServer(), getWorldServer(), profile, getInteractManager());
            CraftPlayer temp = new CraftPlayer(getCraftServer(), player);
            temp.setPlayerListName(getTabPlayerName(i));
            tabPlayers.add(temp);
        }
        saveConfig();
        getTab();
    }

    @Override
    public void onDisable() {
        unloadTab();
    }

    public String getTabPlayerName(int id) {
        return getConfig().get("data.tab"+id)==null ? ""+id : getConfig().getString("data.tab"+id);
    }

    public void setTabPlayerName(int id, String name) {
        getConfig().set("data.tab"+id, name);
        saveConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().setPlayerListName("");
        getTab(e.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("resettab")) {
            for(int i=0;i<4;i++) {
                for(int j=0; j<20; j++) {
                    tabPlayers.get((i*20) + j).setPlayerListName("§8"+i+":"+j);
                    setTabPlayerName((i*20) + j, "§8"+i+":"+j);
                }
            }
        }
        if(cmd.getName().equalsIgnoreCase("tab")) {
            // /tab col id string
            if(args.length < 3) {
                sender.sendMessage("§eUzycie: /tab [col 0-3] [id 0-19] [..content]");
            } else {
                int id;
                int col;
                try {
                    id = Integer.parseInt(args[1]);
                    if(id < 0 || id > 19)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§eid musi byc liczba z przedzialu <0, 79>");
                    return true;
                }
                try {
                    col = Integer.parseInt(args[0]);
                    if(col < 0 || col > 3)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§ecol musi byc liczba z przedzialu <0, 3>");
                    return true;
                }
                StringBuilder string = new StringBuilder();
                for(int i=2; i<args.length; i++)
                    string.append(args[i]).append(" ");
                String result = ChatColor.translateAlternateColorCodes('&', string.toString());
                tabPlayers.get((20*col)+id).setPlayerListName(result);
                sender.sendMessage("§6Ustawiono na: "+result);
                setTabPlayerName((20*col)+id, result);
            }
        }
        return true;
    }

    public void getTab(Player p) {
        CraftPlayer player = (CraftPlayer) p;
        int i=0;
        for (CraftPlayer tabPlayer : tabPlayers) {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, tabPlayer.getHandle());
            player.getHandle().playerConnection.sendPacket(packet);
            packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, tabPlayer.getHandle());
            tabPlayer.setPlayerListName(getTabPlayerName(i));
            player.getHandle().playerConnection.sendPacket(packet);
            i++;
        }
    }

    public void unloadTab(Player p) {
        CraftPlayer player = (CraftPlayer) p;
        int i=0;
        for (CraftPlayer tabPlayer : tabPlayers) {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, tabPlayer.getHandle());
            player.getHandle().playerConnection.sendPacket(packet);
            i++;
        }
    }

    public void unloadTab() {
        for(Player p: Bukkit.getOnlinePlayers())
            unloadTab(p);
    }

    public void getTab() {
        for(Player p: Bukkit.getOnlinePlayers())
            getTab(p);
    }

    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    public WorldServer getWorldServer() {
        return worldServer;
    }

    public MinecraftServer getMinecraftServer() {
        return getCraftServer().getServer();
    }

    public PlayerInteractManager getInteractManager() {
        return playerInteractManager;
    }
}
