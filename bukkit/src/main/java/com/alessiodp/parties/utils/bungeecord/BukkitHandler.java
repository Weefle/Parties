package com.alessiodp.parties.utils.bungeecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.alessiodp.parties.Parties;
import com.alessiodp.parties.PartiesBungee;
import com.alessiodp.parties.configuration.Messages;
import com.alessiodp.parties.handlers.LogHandler;
import com.alessiodp.parties.objects.Party;
import com.alessiodp.parties.objects.ThePlayer;
import com.alessiodp.parties.utils.enums.ConsoleColors;
import com.alessiodp.parties.utils.enums.LogLevel;

public class BukkitHandler implements PluginMessageListener {
	Parties plugin;
	
	public BukkitHandler(Parties instance) {
		plugin = instance;
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, PartiesBungee.channel);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, PartiesBungee.channel, this);
	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals(PartiesBungee.channel)) {
			return;
		}
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		DataInputStream in = new DataInputStream(stream);
		Packet packet;
		ThePlayer tp = plugin.getPlayerHandler().getPlayer(player.getUniqueId());
		Party party = plugin.getPartyHandler().getParty(tp.getPartyName());
		
		try {
			packet = new Packet(in);
			if (packet.getVersion().equals(plugin.getDescription().getVersion())) {
				if (party != null) {
					if (tp.getRank() < packet.getRankNeeded())
						return;
					List<String> list = new ArrayList<String>();
					for (Player pl : party.getOnlinePlayers())
						if ((pl != player) && (plugin.getPlayerHandler().getPlayer(pl.getUniqueId()).getRank() >= packet.getRankMinimum())) {
							list.add(pl.getUniqueId().toString());
						}
					packet.setInfo(list);
					packet.setMessage(ChatColor.translateAlternateColorCodes('&', Messages.follow_following_server.replace("%server%", packet.getServer())));
					/*
					 * Send output
					 */
					ByteArrayOutputStream outstream = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(outstream);
					packet.write(out);
					Player p = Bukkit.getServer().getOnlinePlayers().iterator().next();
					if (p != null) {
						p.sendPluginMessage(plugin, PartiesBungee.channel, outstream.toByteArray());
					}
				}
			} else {
				LogHandler.log(LogLevel.BASIC, "Skipping bungeecord Parties packet. Versions don't match! (" + packet.getVersion() + ")", true, ConsoleColors.RED);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
