package com.github.bingorufus.chatitemdisplay.util.bungee;


import com.github.bingorufus.chatitemdisplay.ChatItemDisplay;
import com.github.bingorufus.chatitemdisplay.Display;
import com.github.bingorufus.chatitemdisplay.util.ChatItemConfig;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;


public class BungeeCordReceiver implements PluginMessageListener {


    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] bytes) {// Subchannel, Serialized display,
        // Is command
        if (!channel.equalsIgnoreCase("chatitemdisplay:in"))
            return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        String data = in.readUTF();
        if (ChatItemConfig.DEBUG_MODE) {
            Bukkit.getLogger().info("Received info: " + data);
        }
        receiveDisplay(data, in);


    }

    public void receiveDisplay(String data, ByteArrayDataInput in) {
        Display display = Display.deserialize(data);
        ChatItemDisplay.getInstance().getDisplayedManager().addDisplay(display);

        if (in.readBoolean()) {
            display.getDisplayable().broadcastDisplayable();
        }
    }


}
