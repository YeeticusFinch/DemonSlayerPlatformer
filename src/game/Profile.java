package game;

public class Profile {
    public enum Path {SLAYER, DEMON}

    public enum Style {NONE, WATER, FLAME, WIND}

    public enum DemonArt {CRIMSON_HUNGER, FOREST_HAND, SWAMP, SUSUMARU, YAHABA, COMBUSTIBLE_BLOOD}

    public enum SlayerRank {NONE, MIZUNOTO, MIZUNOE, KANOTO, KANOE, TSUCHINOTO, TSUCHINOE, HINOTO, HINOE, KINOTO, KINOE}

    public Path path = Path.SLAYER;
    public Style style = Style.NONE;
    public DemonArt demonArt = DemonArt.CRIMSON_HUNGER;
    public SlayerRank slayerRank = SlayerRank.NONE;
    public int unlockedSlayer;
    public int unlockedDemon;
    public int deaths;
    public int totalKills;
    public boolean colorChanged;
    public boolean finishedSlayer, finishedDemon;
}
