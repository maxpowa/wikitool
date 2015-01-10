package com.maxpowa.threading;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class RunnableUpdateCheck implements Runnable {

    private final String modsioApi = "http://mods.io/mods/{modname}/latest";
    private int id;
    private String version;

    public RunnableUpdateCheck(int id, String version) {
        this.id = id;
        this.version = version;
    }

    public void run() {
        try {
            JsonParser parser = new JsonParser();
            Reader reader = new InputStreamReader(new URL(modsioApi.replace("{modname}", id + "")).openStream());
            JsonElement json = parser.parse(reader);
            System.out.println(json.toString());
            String latestVersion = json.getAsJsonObject().get("version").getAsString();
            if (checkForUpdate(version, latestVersion)) {
                URL url = new URL(json.getAsJsonObject().get("download").getAsString());
                // do update-y things
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private boolean checkForUpdate(String current, String online) {
        if (current.equals(online)) {
            return false;
        }

        return checkUpdate(getVersionInts(current), getVersionInts(online));
    }

    private boolean checkUpdate(Integer[] current, Integer[] online) {
        if (current.length > online.length) {
            Integer[] newOnline = new Integer[current.length];
            System.arraycopy(online, 0, newOnline, 0, online.length);
            for (int i = online.length; i < newOnline.length; i++) {
                newOnline[i] = 0;
            }
            online = newOnline;
        } else if (online.length > current.length) {
            Integer[] newCurrent = new Integer[online.length];
            System.arraycopy(current, 0, newCurrent, 0, current.length);
            for (int i = current.length; i < newCurrent.length; i++) {
                newCurrent[i] = 0;
            }
            current = newCurrent;
        }
        for (int i = 0; i < current.length; i++) {
            if (online[i] > current[i]) {
                return true;
            }
        }
        return false;
    }

    private Integer[] getVersionInts(String versionString) {
        String bits;
        if (versionString.split("\\-v").length == 2) {
            bits = versionString.split("\\-v")[1];
        } else if (versionString.split(" v").length == 2) {
            bits = versionString.split(" v")[1];
        } else if (versionString.split(" ").length == 2) {
            bits = versionString.split(" ")[1];
        } else {
            bits = versionString;
        }

        Integer[] versionMapping = new Integer[bits.split("\\.").length];
        String[] nums = bits.split("\\.");
        for (int i = 0; i < nums.length; i++) {
            try {
                versionMapping[i] = Integer.parseInt(nums[i]);
            } catch (NumberFormatException e) {
                versionMapping[i] = 0;
            }
        }

        return versionMapping;
    }
}
