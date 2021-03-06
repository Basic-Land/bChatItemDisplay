package com.github.bingorufus.chatitemdisplay;

import com.github.bingorufus.chatitemdisplay.api.display.DisplayType;
import com.github.bingorufus.chatitemdisplay.api.display.Displayable;
import com.github.bingorufus.chatitemdisplay.displayables.DisplayingPlayer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;

import java.util.UUID;

public class Display {
    private final Displayable dis;
    private final DisplayingPlayer player;
    private final UUID id;

    public Display(Displayable displayable, UUID id) {
        this.dis = displayable;
        this.player = displayable.getDisplayer();
        this.id = id;
    }

    public static Display deserialize(String json) {
        JsonObject jo = (JsonObject) new JsonParser().parse(json);

        UUID id = UUID.fromString(jo.get("id").getAsString());
        JsonObject displayableJSON = jo.getAsJsonObject("displayable");
        Displayable displayable;
        try {
            DisplayType displayType = ChatItemDisplay.getInstance().getDisplayType((Class<? extends DisplayType>) Class.forName(displayableJSON.get("type").getAsString()));
            if (displayType == null) {
                return null;
            }
            displayable = displayType.initDisplayable(displayableJSON);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return new Display(displayable, id);
    }

    public @NonNull Displayable getDisplayable() {
        return dis;
    }

    public DisplayingPlayer getPlayer() {
        return player;
    }

    public UUID getId() {
        return id;
    }


    public String getInsertion() {
        JsonObject jo = new JsonObject();
        jo.addProperty("id", id.toString());
        return '\u0007' + "cid" + jo.toString() + '\u0007';
    }

    public String serialize() {
        JsonObject jo = new JsonObject();
        jo.addProperty("id", id.toString());
        jo.add("displayable", dis.serialize());
        return jo.toString();
    }

    @Override
    public String toString() {
        return serialize();
    }
}
