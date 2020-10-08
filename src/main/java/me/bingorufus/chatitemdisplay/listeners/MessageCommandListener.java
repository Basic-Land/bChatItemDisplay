package me.bingorufus.chatitemdisplay.listeners;

import me.bingorufus.chatitemdisplay.ChatItemDisplay;
import me.bingorufus.chatitemdisplay.DisplayParser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class MessageCommandListener implements Listener {
    final List<String> msgCmds;
    final ChatItemDisplay m = ChatItemDisplay.getInstance();

    public MessageCommandListener() {

        msgCmds = m.getConfig().getStringList("message-command");
        msgCmds.replaceAll(cmd -> { // Makes sure the command ends with a space
            return cmd.trim() + " ";
        });

    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (m.useOldFormat || !e.getMessage().startsWith("/") || msgCmds.stream().noneMatch(e.getMessage()::startsWith))
            return;
        DisplayParser dp = new DisplayParser(e.getMessage(), e.getPlayer(), true);
        String out = dp.parse();
        if (!dp.containsDisplay() || dp.cancelMessage())
            return;
        e.setMessage(out);
    }

}
