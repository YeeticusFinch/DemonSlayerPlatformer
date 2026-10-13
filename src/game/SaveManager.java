package game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SaveManager {
    private static final Pattern SAVE_NAME = Pattern.compile("save_(\\d+)\\.json");
    private static final Pattern FIELD = Pattern.compile("\\\"([A-Za-z0-9_]+)\\\"\\s*:\\s*(\\\"(?:\\\\.|[^\\\"])*\\\"|true|false|-?\\d+)");

    private SaveManager() {}

    public static final class SaveInfo {
        public final int slot;
        public final Profile profile;

        private SaveInfo(int slot, Profile profile) {
            this.slot = slot;
            this.profile = profile;
        }

        public String title() {
            String path = profile.path == Profile.Path.SLAYER ? "Slayer" : "Demon";
            int level = profile.path == Profile.Path.SLAYER ? profile.unlockedSlayer : profile.unlockedDemon;
            String loadout = profile.path == Profile.Path.SLAYER ? profile.style.name() : profile.demonArt.name();
            return "Save " + slot + " - " + path + " - Level " + (level + 1) + " - " + loadout;
        }
    }

    public static ArrayList<SaveInfo> saves() {
        ArrayList<SaveInfo> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("."), "save_*.json")) {
            for (Path p : ds) {
                Matcher m = SAVE_NAME.matcher(p.getFileName().toString());
                if (!m.matches()) continue;
                int slot = Integer.parseInt(m.group(1));
                Profile profile = load(slot);
                if (profile != null) out.add(new SaveInfo(slot, profile));
            }
        } catch (IOException ignored) {
        }
        out.sort(Comparator.comparingInt(s -> s.slot));
        return out;
    }

    public static void prepareNewGame() {
        Game.profile = new Profile();
        Game.profile.saveSlot = nextAvailableSlot();
    }

    public static boolean loadIntoGame(int slot) {
        Profile p = load(slot);
        if (p == null) return false;
        p.saveSlot = slot;
        Game.profile = p;
        return true;
    }

    public static void autosave(Profile p) {
        if (p.saveSlot < 0) p.saveSlot = nextAvailableSlot();
        Path target = file(p.saveSlot);
        Path tmp = Paths.get(target.toString() + ".tmp");
        try {
            Files.writeString(tmp, toJson(p), StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Game.log("Autosaved to save_" + p.saveSlot + ".json");
        } catch (IOException atomicFailed) {
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                Game.log("Autosaved to save_" + p.saveSlot + ".json");
            } catch (IOException failed) {
                Game.log("Autosave failed: " + failed.getClass().getSimpleName());
            }
        }
    }

    private static int nextAvailableSlot() {
        boolean[] used = new boolean[1024];
        for (SaveInfo s : saves()) if (s.slot >= 0 && s.slot < used.length) used[s.slot] = true;
        for (int i = 0; i < used.length; i++) if (!used[i]) return i;
        int max = -1;
        for (SaveInfo s : saves()) max = Math.max(max, s.slot);
        return max + 1;
    }

    private static Profile load(int slot) {
        Path p = file(slot);
        if (!Files.isRegularFile(p)) return null;
        try {
            HashMap<String, String> f = parse(Files.readString(p, StandardCharsets.UTF_8));
            Profile pr = new Profile();
            pr.saveSlot = intValue(f, "saveSlot", slot);
            pr.path = enumValue(Profile.Path.class, f.get("path"), Profile.Path.SLAYER);
            pr.style = enumValue(Profile.Style.class, f.get("style"), Profile.Style.NONE);
            pr.demonArt = enumValue(Profile.DemonArt.class, f.get("demonArt"), Profile.DemonArt.CRIMSON_HUNGER);
            pr.slayerRank = enumValue(Profile.SlayerRank.class, f.get("slayerRank"), Profile.SlayerRank.NONE);
            pr.unlockedSlayer = intValue(f, "unlockedSlayer", 0);
            pr.unlockedDemon = intValue(f, "unlockedDemon", 0);
            pr.deaths = intValue(f, "deaths", 0);
            pr.totalKills = intValue(f, "totalKills", 0);
            pr.colorChanged = boolValue(f, "colorChanged", false);
            pr.finishedSlayer = boolValue(f, "finishedSlayer", false);
            pr.finishedDemon = boolValue(f, "finishedDemon", false);
            return pr;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Path file(int slot) {
        return Paths.get("save_" + slot + ".json");
    }

    private static String toJson(Profile p) {
        return "{\n"
                + "  \"version\": 1,\n"
                + "  \"saveSlot\": " + p.saveSlot + ",\n"
                + "  \"path\": \"" + p.path.name() + "\",\n"
                + "  \"unlockedSlayer\": " + p.unlockedSlayer + ",\n"
                + "  \"unlockedDemon\": " + p.unlockedDemon + ",\n"
                + "  \"slayerRank\": \"" + p.slayerRank.name() + "\",\n"
                + "  \"style\": \"" + p.style.name() + "\",\n"
                + "  \"demonArt\": \"" + p.demonArt.name() + "\",\n"
                + "  \"colorChanged\": " + p.colorChanged + ",\n"
                + "  \"finishedSlayer\": " + p.finishedSlayer + ",\n"
                + "  \"finishedDemon\": " + p.finishedDemon + ",\n"
                + "  \"deaths\": " + p.deaths + ",\n"
                + "  \"totalKills\": " + p.totalKills + "\n"
                + "}\n";
    }

    private static HashMap<String, String> parse(String json) {
        HashMap<String, String> f = new HashMap<>();
        Matcher m = FIELD.matcher(json);
        while (m.find()) f.put(m.group(1), unquote(m.group(2)));
        return f;
    }

    private static String unquote(String s) {
        if (s == null || s.length() < 2 || s.charAt(0) != '"') return s;
        return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int intValue(HashMap<String, String> f, String key, int def) {
        try { return Integer.parseInt(f.getOrDefault(key, Integer.toString(def))); }
        catch (NumberFormatException ex) { return def; }
    }

    private static boolean boolValue(HashMap<String, String> f, String key, boolean def) {
        String v = f.get(key);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T def) {
        if (value == null) return def;
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException ex) { return def; }
    }
}
