package me.ultradev;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static Set<String> PLAYER_LIST = new HashSet<>();
    public static double TOTAL_LEVEL = 0;
    public static int PLAYERS_FETCHED = 0;

    public static int PLAYERS_TO_FETCH = 100;

    public static void main(String[] args) {
        try {
            fetchPlayer("UltraTheDuck");
            System.out.println("Done! " + PLAYERS_FETCHED + " players fetched - Average: " + (TOTAL_LEVEL / PLAYERS_FETCHED));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fetchPlayer(String name) throws InterruptedException {
        HttpResponse stats = HttpRequest.get("https://api.slothpixel.me/api/players/" + name).send();
        JsonObject statsObject = JsonParser.parseString(stats.bodyText()).getAsJsonObject();
        Number level = statsObject.get("level").getAsNumber();
        String username = statsObject.get("username").getAsString();
        PLAYER_LIST.add(statsObject.get("uuid").getAsString());
        TOTAL_LEVEL += level.doubleValue();
        PLAYERS_FETCHED++;
        System.out.println("Fetched \"" + username + "\" - level " + level + " (" + PLAYERS_FETCHED + "/" + PLAYERS_TO_FETCH + ")");

        if (PLAYERS_FETCHED % 5 == 0) {
            System.out.println("Current average: " + (TOTAL_LEVEL / PLAYERS_FETCHED));
        }

        if (PLAYERS_FETCHED < PLAYERS_TO_FETCH) {
            Thread.sleep(1000);
            HttpResponse friends = HttpRequest.get("https://api.slothpixel.me/api/players/" + name + "/friends").send();
            List<JsonElement> friendsArray = JsonParser.parseString(friends.bodyText()).getAsJsonArray().asList();
            Collections.shuffle(friendsArray);
            Thread.sleep(1000);
            for (JsonElement element : friendsArray) {
                JsonObject friendObject = element.getAsJsonObject();
                String uuid = friendObject.get("uuid").getAsString();
                if (!PLAYER_LIST.contains(uuid)) {
                    if (PLAYERS_FETCHED < PLAYERS_TO_FETCH) fetchPlayer(uuid);
                    else break;
                }
            }
            System.out.println("Path ended for \"" + username + "\".");
        }
    }

}