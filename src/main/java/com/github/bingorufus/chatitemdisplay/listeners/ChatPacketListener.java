package com.github.bingorufus.chatitemdisplay.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.bingorufus.chatitemdisplay.ChatItemDisplay;
import com.github.bingorufus.chatitemdisplay.api.display.Displayable;
import com.github.bingorufus.chatitemdisplay.util.ChatItemConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPacketListener extends PacketAdapter {

    private final ChatItemDisplay m;

    public ChatPacketListener(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
        m = (ChatItemDisplay) plugin;
    }

    @Override
    public void onPacketReceiving(final PacketEvent e) {
        if (m.getChatItemDisplayInventories().containsKey(e.getPlayer().getOpenInventory().getTopInventory())) {
            e.setCancelled(true);
        }
        //Prevents items from being duplicated from displayed furnaces by shift clicking a recipe in the recipe booked
    }

    @Override
    public void onPacketSending(final PacketEvent e) {
        BaseComponent[] baseComps;
        BaseComponent[] originalComps;
        int field = 0;
        PacketContainer packet = e.getPacket();
        { // Get JSON of message
            WrappedChatComponent chat = packet.getChatComponents().read(0);
            if (chat == null) {
                Object chatPacket = packet.getHandle();
                try {
                    Field f = chatPacket.getClass().getDeclaredField("components");
                    originalComps = (BaseComponent[]) f.get(chatPacket);
                    field = 1;
                } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                    return;
                }
            } else {
                originalComps = ComponentSerializer.parse(chat.getJson());
            }
            if (originalComps == null) return;
            if (originalComps.length != 1) {
                TextComponent comp = new TextComponent(originalComps);
                originalComps = new BaseComponent[]{comp};
            }
        }


        if (!ComponentSerializer.toString(originalComps).contains("\\u0007cid"))
            return;

        if (originalComps[0].getExtra() == null)
            return;

        List<BaseComponent> editedExtra = new ArrayList<>();
        for (int i = 0; i < originalComps[0].getExtra().size(); i++) {
            List<BaseComponent> extra = originalComps[0].getExtra();

            TextComponent bc = (TextComponent) extra.get(i);

            if (!bc.toLegacyText().contains("\u0007cid")) {
                editedExtra.add(bc);
                continue;
            }
            Pattern pattern = Pattern.compile("(\u0007cid\\{(.*?)}\u0007)");
            Matcher matcher = pattern.matcher(bc.toLegacyText());
            if (!matcher.find()) {
                editedExtra.add(bc);
                continue;
            }

            matcher = pattern.matcher(bc.toLegacyText());
            if (matcher.find()) {

                List<String> partsTemp = new ArrayList<>();
                List<String> parts = new ArrayList<>();
                parts.add(bc.toLegacyText());

                for (int matchNumber = 1; matchNumber < matcher.groupCount(); matchNumber++) {
                    String match = matcher.group(matchNumber);
                    for (String part : parts) {
                        Collections.addAll(partsTemp, part
                                .split("((?<=" + Pattern.quote(match) + ")|(?=" + Pattern.quote(match) + "))"));

                    }
                    parts.clear();
                    parts.addAll(partsTemp);
                    partsTemp.clear();

                }
                for (String part : parts) {

                    TextComponent tc = (TextComponent) TextComponent.fromLegacyText(part)[0];
                    tc.copyFormatting(bc, false);
                    editedExtra.add(tc);
                }

            }


        }
        BaseComponent org = originalComps[0];
        org.setExtra(editedExtra);
        originalComps[0] = org;

        baseComps = originalComps;


        try {
            for (int i = 0; i < baseComps[0].getExtra().size(); i++) {
                List<BaseComponent> extra = baseComps[0].getExtra();
                TextComponent bc = (TextComponent) extra.get(i);
                if (!bc.toLegacyText().contains("\u0007cid"))
                    continue;

                String replace;

                String displaying;
                Pattern pattern = Pattern.compile("\u0007cid(.*?)\u0007"); // Searches for a string that starts and ends
                // with the bell character

                Matcher matcher = pattern.matcher(bc.toLegacyText());

                while (matcher.find()) {
                    displaying = matcher.group(1);
                    char bell = '\u0007';
                    replace = bell + "cid" + matcher.group(1) + bell;


                    String legacyText = bc.toLegacyText().replace(replace, replace + getLastColors(bc.toLegacyText().substring(0, bc.toLegacyText().indexOf(replace))));

                    if (ChatItemConfig.DEBUG_MODE) {
                        Bukkit.getLogger().info(displaying + " is being displayed");
                    }

                    JsonObject jo = (JsonObject) new JsonParser().parse(displaying);
                    Displayable display = m.getDisplayedManager().getDisplayed(UUID.fromString(jo.get("id").getAsString()))
                            .getDisplayable();


                    String[] parts = legacyText
                            .split("((?<=" + Pattern.quote(replace) + ")|(?=" + Pattern.quote(replace) + "))");
                    BaseComponent hover = display.getInsertion();
                    TextComponent component = new TextComponent();
                    for (String part : parts) {
                        if (part.equalsIgnoreCase(replace)) {
                            component.addExtra(hover);
                            continue;
                        }
                        TextComponent tc = (TextComponent) TextComponent.fromLegacyText(part)[0];
                        component.addExtra(tc);
                    }
                    extra.set(i, component);
                    baseComps[0].setExtra(extra);
                    extra = baseComps[0].getExtra();
                    bc = (TextComponent) extra.get(i);

                    if (!bc.toLegacyText().contains("\u0007cid"))
                        break;
                    matcher = pattern.matcher(bc.toLegacyText());
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        if (field == 0) {
            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(baseComps)));
            return;
        }
        packet.getModifier().write(1,
                baseComps);

    }

    private String getLastColors(String string) {
        TextComponent tc = (TextComponent) TextComponent.fromLegacyText(string)[0];
        TextComponent colored = new TextComponent();
        colored.copyFormatting(tc, ComponentBuilder.FormatRetention.FORMATTING, true);
        return colored.toLegacyText();
    }

}
