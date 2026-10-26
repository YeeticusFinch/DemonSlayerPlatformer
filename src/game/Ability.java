package game;

import java.util.ArrayList;

public class Ability {
    public enum Kind {WAVE_PROJ, WHEEL, FLOWING_DANCE, TIDE, BLESSED_RAIN, WHIRL,
        FIRE_DASH, RISING_SUN, BLAZING_SLAM, FLAME_UNDULATION, TIGER_VOLLEY,
        FLAME_SEARING_RINGS, FLAME_EMBER_BARRAGE, FLAME_RETAINED_HEAT, FLAME_PURGATORY,
        WIND_DUST, WIND_CLAWS, WIND_STORM, WIND_MOUNTAIN, WIND_TREE, WIND_MIST, WIND_BLACK, WIND_EIGHT, WIND_NINE, WIND_TEN,
        BLOOD_BOLT, CLAW_WAVES, NOVA, DEVOUR,
        SWAMP_FLURRY, AQUATIC_DASH, SWAMP_HANDS, SWAMP_STEP,
        NEZUKO_NAILS, NEZUKO_EXPLODING_BLOOD, NEZUKO_SCRATCHING, NEZUKO_HEEL_BASH, NEZUKO_SPIN_KICK, NEZUKO_FLYING_KICK, NEZUKO_FRENZIED_KICKS, NEZUKO_DROP_KICK,
        BALL_KICK, SPINNING_THROW, PIERCING_KICK, SIXFOLD_TEMARI, SPIRALING_SHOT,
        BOULDER_TOSS, SMACK_DOWN, CHASER_ARROW, ERUPTION, TORRENTIAL_ARROWS,
        HAND_SPIKES, HAND_GRASP, HAND_SLAM, HAND_AURA,
        SPIDER_GROUND_POUND, SPIDER_FISSURE, SPIDER_STRIKE, SPIDER_POUNCE,
        RUI_BASIC_THREAD, RUI_SILK_STRIKE, RUI_SILK_BARRAGE, RUI_WEB_SNATCH, RUI_DICING_WEB_WALL,
        RUI_CUTTING_THREAD_CAGE, RUI_CONTORTING_THREADS, RUI_MURDEROUS_EYE_BASKET, RUI_CUTTING_THREAD_ROTATION,
        ENMU_WHISPER, ENMU_REFLEX, ENMU_ECHOING_WHISPER, ENMU_FLESH_HANDS, ENMU_NIGHTMARE, ENMU_SLEEP_CONE,
        AKAZA_COMPASS_NEEDLE, AKAZA_AIR_TYPE, AKAZA_DISORDER, AKAZA_ANNIHILATION,
        AKAZA_CROWN_SPLITTER, AKAZA_EXPLOSIVE_FLURRY, AKAZA_FLYING_PLANET,
        AKAZA_EIGHT_LAYERED_DEMON_CORE, AKAZA_TEN_THOUSAND_LEAVES, AKAZA_BLUE_SILVER_AFTERGLOW,
        AKAZA_REGENERATION}
    public final Kind kind;
    public final String form;
    public final String desc;
    public final int cost;
    public final float cooldown;

    public Ability(Kind kind, String form, String desc, int cost, float cooldown) {
        this.kind = kind;
        this.form = form;
        this.desc = desc;
        this.cost = cost;
        this.cooldown = cooldown;
    }

    public static ArrayList<Ability> water() {
        return water(Profile.SlayerRank.MIZUNOTO);
    }

    public static ArrayList<Ability> water(Profile.SlayerRank rank) {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.WAVE_PROJ, "First Form: Water Surface Slash", "Crescent wave projectile", 16, 0.9f));
        l.add(new Ability(Kind.WHEEL, "Second Form: Water Wheel", "Spinning wheel around you", 20, 1.6f));
        l.add(new Ability(Kind.FLOWING_DANCE, "Third Form: Flowing Dance", "Wavy forward dance of slashes", 24, 2.6f));
        l.add(new Ability(Kind.TIDE, "Fourth Form: Striking Tide", "Charge, then release a figure-eight tide of cuts", 34, 3.1f));
        if (atLeast(rank, Profile.SlayerRank.MIZUNOTO)) {
            l.add(new Ability(Kind.BLESSED_RAIN, "Fifth Form: Blessed Rain After the Drought", "Merciful descending water cut", 24, 3.4f));
            l.add(new Ability(Kind.WHIRL, "Sixth Form: Whirlpool", "Vortex that drags foes", 28, 4.5f));
        }
        return l;
    }

    public static ArrayList<Ability> flame() {
        return flame(Profile.SlayerRank.MIZUNOE);
    }

    public static ArrayList<Ability> flame(Profile.SlayerRank rank) {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.FIRE_DASH, "First Form: Unknowing Fire", "Blazing dash through foes", 16, 0.9f));
        l.add(new Ability(Kind.RISING_SUN, "Second Form: Rising Scorching Sun", "Rising uppercut arc", 20, 1.6f));
        l.add(new Ability(Kind.BLAZING_SLAM, "Third Form: Blazing Universe", "Crashing downward flame", 24, 2.6f));
        if (atLeast(rank, Profile.SlayerRank.MIZUNOTO))
            l.add(new Ability(Kind.FLAME_UNDULATION, "Fourth Form: Blooming Flame Undulation", "Twin sword spins release a flame spiral", 30, 3.8f));
        if (atLeast(rank, Profile.SlayerRank.MIZUNOE))
            l.add(new Ability(Kind.TIGER_VOLLEY, "Fifth Form: Flame Tiger", "Volley of tiger flames", 56, 4.5f));
        if (atLeast(rank, Profile.SlayerRank.KINOE)) {
            l.add(new Ability(Kind.FLAME_SEARING_RINGS, "Sixth Form: Searing Rings of Fire", "A spiraling dash leaves rings of flame", 72, 7.5f));
            l.add(new Ability(Kind.FLAME_EMBER_BARRAGE, "Seventh Form: Ember Barrage, Conflagration", "A leaping barrage of intersecting flame slashes", 62, 6.5f));
            l.add(new Ability(Kind.FLAME_RETAINED_HEAT, "Eighth Form: Retained Heat Inferno", "Store blocked damage, then unleash it in a torrent", 76, 10f));
            l.add(new Ability(Kind.FLAME_PURGATORY, "Ninth Form: Purgatory", "A dragon-headed dash ending in a devastating fire tornado", 100, 14f));
        }
        return l;
    }

    public static ArrayList<Ability> wind() {
        return wind(Profile.SlayerRank.NONE);
    }

    public static ArrayList<Ability> wind(Profile.SlayerRank rank) {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.WIND_DUST, "First Form: Dust Whirlwind Cutter", "Cyclone dash of cutting gusts", 16, 0.9f));
        l.add(new Ability(Kind.WIND_CLAWS, "Second Form: Claws-Purifying Wind", "Four crossing claw slashes", 20, 1.6f));
        l.add(new Ability(Kind.WIND_TREE, "Third Form: Clear Storm Wind Tree", "Leaping arched gale slashes", 28, 4.5f));
        if (atLeast(rank, Profile.SlayerRank.MIZUNOTO)) {
            l.add(new Ability(Kind.WIND_STORM, "Fourth Form: Rising Dust Storm", "Stationary jagged tornado", 24, 2.6f));
            l.add(new Ability(Kind.WIND_MOUNTAIN, "Fifth Form: Cold Mountain Wind", "High jump into a violent diagonal gale slam", 32, 4.8f));
        }
        if (atLeast(rank, Profile.SlayerRank.MIZUNOE))
            l.add(new Ability(Kind.WIND_MIST, "Sixth Form: Black Wind Mountain Mist", "Vertical cutting gale", 30, 3.8f));
        if (atLeast(rank, Profile.SlayerRank.KANOE))
            l.add(new Ability(Kind.WIND_BLACK, "Seventh Form: Gale Sudden Gusts", "Backflip into a deadly torrent of black-red slashes", 38, 4.8f));
        if (atLeast(rank, Profile.SlayerRank.TSUCHINOE))
            l.add(new Ability(Kind.WIND_EIGHT, "Eighth Form", "Placeholder", 0, 1f));
        if (atLeast(rank, Profile.SlayerRank.HINOE))
            l.add(new Ability(Kind.WIND_NINE, "Ninth Form", "Placeholder", 0, 1f));
        if (atLeast(rank, Profile.SlayerRank.KINOTO))
            l.add(new Ability(Kind.WIND_TEN, "Tenth Form", "Placeholder", 0, 1f));
        return l;
    }

    private static boolean atLeast(Profile.SlayerRank rank, Profile.SlayerRank min) {
        return rank.ordinal() >= min.ordinal();
    }

    public static ArrayList<Ability> bloodArt() {
        return crimsonHunger();
    }

    public static ArrayList<Ability> crimsonHunger() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.BLOOD_BOLT, "Blood Bolt", "Fires a hardened blood spike", 14, 0.8f));
        l.add(new Ability(Kind.CLAW_WAVES, "Crimson Fangs", "Triple claw wave barrage", 20, 1.8f));
        l.add(new Ability(Kind.NOVA, "Blood Nova", "Explosive burst around you", 26, 3.5f));
        l.add(new Ability(Kind.DEVOUR, "Devour", "Feast to regenerate flesh", 30, 5f));
        return l;
    }

    public static ArrayList<Ability> combustibleBlood(Profile.SlayerRank rank) {
        ArrayList<Ability> l = new ArrayList<>();
        if (atLeast(rank, Profile.SlayerRank.MIZUNOTO)) {
            l.add(new Ability(Kind.NEZUKO_NAILS, "Combustible Blood: Nails of Fury", "Flaming claw uppercut", 16, 1.1f));
            l.add(new Ability(Kind.NEZUKO_EXPLODING_BLOOD, "Combustible Blood: Exploding Blood", "Bolt that erupts into demon-burning flame", 44, 2.4f));
            l.add(new Ability(Kind.NEZUKO_SCRATCHING, "Combustible Blood: Crazy Scratching", "Rapid flaming claw flurry", 24, 3.1f));
            l.add(new Ability(Kind.NEZUKO_HEEL_BASH, "Combustible Blood: Heel Bash", "Aerial heel drop shockwave", 28, 3.6f));
            l.add(new Ability(Kind.NEZUKO_SPIN_KICK, "Combustible Blood: Spin Kick", "Frontflip kick into a flame vortex", 24, 2.8f));
            l.add(new Ability(Kind.NEZUKO_FLYING_KICK, "Combustible Blood: Flying Kick", "Feet-first leap and rebound", 20, 2.2f));
        }
        return l;
    }

    public static ArrayList<Ability> swampDemonArt(boolean full) {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.SWAMP_FLURRY, "Swamp Demon Art: Flurry of Blows", "Rapid punches and kicks", 18, 1.4f));
        l.add(new Ability(Kind.AQUATIC_DASH, "Swamp Demon Art: Aquatic Dash", "Ink cloud and water-line escape dash", 18, 1.8f));
        if (full) {
            l.add(new Ability(Kind.SWAMP_HANDS, "Swamp Demon Art: Swamp Hands", "White hands grasp from black puddles", 24, 3.2f));
            l.add(new Ability(Kind.SWAMP_STEP, "Swamp Demon Art: Swamp Step", "Sink and emerge from a distant puddle", 26, 3.6f));
        }
        return l;
    }

    public static ArrayList<Ability> susumaruArt() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.BALL_KICK, "Temari Demon Art: Ball Kick", "Kick a bouncing temari", 18, 1.1f));
        l.add(new Ability(Kind.SPINNING_THROW, "Temari Demon Art: Spinning Throw", "Backflip and throw a temari", 20, 1.8f));
        l.add(new Ability(Kind.PIERCING_KICK, "Temari Demon Art: Piercing Kick", "Rapid kick dash", 22, 1.8f));
        l.add(new Ability(Kind.SIXFOLD_TEMARI, "Temari Demon Art: Sixfold Temari", "Six-ball barrage", 32, 3.6f));
        l.add(new Ability(Kind.SPIRALING_SHOT, "Temari Demon Art: Spiraling Shot", "Sinusoidal temari barrage", 34, 4.2f));
        return l;
    }

    public static ArrayList<Ability> yahabaArt() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.BOULDER_TOSS, "Arrow Demon Art: Boulder Toss", "Arrow-hurled boulder", 24, 2.6f));
        l.add(new Ability(Kind.SMACK_DOWN, "Arrow Demon Art: Smack Down", "Downward force arrow", 22, 2.2f));
        l.add(new Ability(Kind.CHASER_ARROW, "Arrow Demon Art: Chaser Arrow", "Homing red arrow", 20, 1.9f));
        l.add(new Ability(Kind.ERUPTION, "Arrow Demon Art: Eruption", "Rising ground arrows", 28, 3.2f));
        l.add(new Ability(Kind.TORRENTIAL_ARROWS, "Arrow Demon Art: Torrential Arrows", "Arrow rain", 36, 4.4f));
        return l;
    }

    public static ArrayList<Ability> forestHand() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.HAND_SPIKES, "Blood Demon Art: Hand Forest", "Hands erupt from the ground ahead", 24, 3.2f));
        l.add(new Ability(Kind.HAND_GRASP, "Blood Demon Art: Seizing Hands", "Long arms grab and hurl a foe", 22, 2.8f));
        l.add(new Ability(Kind.HAND_SLAM, "Blood Demon Art: Earth Clutch", "A giant hand crushes the ground after a windup", 26, 3.6f));
        l.add(new Ability(Kind.HAND_AURA, "Blood Demon Art: Hand Aura", "Spiraling hands blast nearby foes", 28, 4.2f));
        return l;
    }

    public static ArrayList<Ability> spiderFatherArt() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.SPIDER_GROUND_POUND, "Spider Demon Art: Ground Pound", "Leap, hover, then slam with a shockwave", 32, 4.4f));
        l.add(new Ability(Kind.SPIDER_FISSURE, "Spider Demon Art: Ground Fissure", "Stomp a line of erupting boulders", 28, 3.6f));
        l.add(new Ability(Kind.SPIDER_STRIKE, "Spider Demon Art: Concussive Strike", "Short-range blast with major knockback", 22, 2.4f));
        l.add(new Ability(Kind.SPIDER_POUNCE, "Spider Demon Art: Pounce", "Explosive forward leap", 30, 3.8f));
        return l;
    }

    public static ArrayList<Ability> ruiArt() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.RUI_BASIC_THREAD, "Filukinesis: Basic Thread", "Outstretched hand fires a single thread", 8, 0.7f));
        l.add(new Ability(Kind.RUI_SILK_STRIKE, "Filukinesis: Silk Strike", "Three parallel threads in a cutting line", 16, 1.4f));
        l.add(new Ability(Kind.RUI_SILK_BARRAGE, "Filukinesis: Silk Barrage", "Two crossing threads slice forward", 20, 1.8f));
        l.add(new Ability(Kind.RUI_WEB_SNATCH, "Filukinesis: Web Snatch", "Latching threads fling a foe skyward", 22, 2.4f));
        l.add(new Ability(Kind.RUI_DICING_WEB_WALL, "Filukinesis: Dicing Web Wall", "A vast grid of threads rolls forward", 34, 3.6f));
        l.add(new Ability(Kind.RUI_CUTTING_THREAD_CAGE, "Filukinesis: Cutting Thread Cage", "A thread cage closes in on a foe", 38, 4.4f));
        l.add(new Ability(Kind.RUI_CONTORTING_THREADS, "Filukinesis: Contorting Threads", "Fanned threads snap into one cutting line", 36, 4.2f));
        l.add(new Ability(Kind.RUI_MURDEROUS_EYE_BASKET, "Filukinesis: Murderous Eye Basket", "A closing ring of slicing silk", 44, 5.4f));
        l.add(new Ability(Kind.RUI_CUTTING_THREAD_ROTATION, "Filukinesis: Cutting Thread Rotation", "A towering thread wall with a gap", 40, 4.8f));
        return l;
    }

    public static ArrayList<Ability> enmuArt() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.ENMU_WHISPER, "Blood Demon Art: Whisper", "Three sleeping whispers, launched in sequence", 18, 1.6f));
        l.add(new Ability(Kind.ENMU_REFLEX, "Blood Demon Art: Reflex", "A one-second looping cardioid of evasive flips and strikes", 38, 6f));
        l.add(new Ability(Kind.ENMU_ECHOING_WHISPER, "Blood Demon Art: Echoing Whisper", "An orb bursts into eight whispers and four brief sleep cones", 42, 6.4f));
        l.add(new Ability(Kind.ENMU_FLESH_HANDS, "Blood Demon Art: Flesh Hands", "Hands erupt from the ground", 30, 4.2f));
        l.add(new Ability(Kind.ENMU_NIGHTMARE, "Blood Demon Art: Nightmare", "A watching eye puts victims to sleep before the flesh crushes them", 78, 12f));
        l.add(new Ability(Kind.ENMU_SLEEP_CONE, "Blood Demon Art: Soporific Gaze", "Project a forward sleep cone for one second", 28, 4.2f));
        return l;
    }

    public static ArrayList<Ability> destructiveDeath() {
        ArrayList<Ability> l = new ArrayList<>();
        l.add(new Ability(Kind.AKAZA_COMPASS_NEEDLE, "Destructive Death: Compass Needle", "Charge for half a second to gain six seconds of automatic defense and attack teleports", 38, 8f));
        l.add(new Ability(Kind.AKAZA_AIR_TYPE, "Destructive Death: Air Type", "Three quick punches launch three blue shockwaves", 20, 1.8f));
        l.add(new Ability(Kind.AKAZA_DISORDER, "Destructive Death: Disorder", "Eight punches launch explosive shockwaves", 52, 5.6f));
        l.add(new Ability(Kind.AKAZA_ANNIHILATION, "Destructive Death: Annihilation Type", "Charge, dash, explode, then repeat the dash", 100, 12f));
        l.add(new Ability(Kind.AKAZA_CROWN_SPLITTER, "Destructive Death: Leg Type — Crown Splitter", "Rise into a devastating explosive heel drop", 48, 4.6f));
        l.add(new Ability(Kind.AKAZA_EXPLOSIVE_FLURRY, "Destructive Death: Leg Type — Explosive Flurry", "Afterimage dash-kick with tremendous knockback", 42, 4.2f));
        l.add(new Ability(Kind.AKAZA_FLYING_PLANET, "Destructive Death: Leg Type — Flying Planet Thousand Wheels", "Rising blue-and-red flying kick for airborne foes", 50, 5.2f));
        l.add(new Ability(Kind.AKAZA_EIGHT_LAYERED_DEMON_CORE, "Destructive Death: Eight-Layered Demon Core", "Hover and barrage the enemy with eight angled shockwaves", 72, 7.2f));
        l.add(new Ability(Kind.AKAZA_TEN_THOUSAND_LEAVES, "Destructive Death: Crushing Type — Ten Thousand Leaves Flashing Willow", "Dash-jump into a crushing ground punch and blue shockwave", 64, 6.4f));
        l.add(new Ability(Kind.AKAZA_BLUE_SILVER_AFTERGLOW, "Destructive Death: Blue Silver Chaotic Afterglow", "Spend all SP to summon eight attacking clones and red-black pillars", 1, 22f));
        l.add(new Ability(Kind.AKAZA_REGENERATION, "Destructive Death: Rapid Regeneration", "Restore 20% HP and regrow all limbs for half your SP", 1, 6f));
        return l;
    }
}
