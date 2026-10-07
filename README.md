# Demon Slayer: Blades of the Night

A fast-paced 2D action platformer written in pure Java (Swing/AWT, no libraries).
Choose to play as a **Demon Slayer** following the story arcs, or as a **Demon**
hunting humans and slayers at night. Everything is drawn with vector graphics,
and all combat effects run through an additive-blur **bloom** pipeline so slashes,
waves, flames and blood arts genuinely glow.

## Run

```bash
./run.bat      # Windows
./run.sh       # Linux/macOS/Git-Bash
```

A portable JDK is already bundled under `tools/` (downloaded during development).
If it is missing, install any JDK 17+ or drop one into `tools/`.
`--selftest` runs the full headless simulation of every level, `game.MechanicsTest`
runs the gameplay-rules test suite.

## Controls

| Key | Action |
|-----|--------|
| A / D | Move |
| W | Jump - press again mid-air for double jump; press while wall-sliding for wall jump |
| S | Fast fall / drop through thin platforms |
| SHIFT | Dash (brief invincibility, once per air time) |
| J | Attack - 3-hit katana/claw combo |
| Q / E or 1-6 | Cycle/select breathing form or Blood Art |
| F | Cast selected form |
| H | Help panel |
| ESC | Pause |
| R | Retry level |

## Demon Slayer path (27 levels)

1. **Mt. Sagiri training** - 10 parkour levels of increasing difficulty under Urokodaki.
2. Clearing the summit triggers the **color change ceremony**: your black blade flickers
   and settles into blue (**Water Breathing**) or red (**Flame Breathing**) at random.
3. **Sabito's Boulder** - defeat the fox-masked ghost, and the boulder splits in two.
4. **Final Selection** - seven night-time levels up Mt. Fujikasane fighting demons;
   the last night is the **Hand Demon** boss.
5. **Kidnapper's Bog** - investigate the swamp, village, and underwater domain
   against temple and swamp demons.
6. **Asakusa** - protect civilians in the city, witness Muzan's escape, meet
   Tamayo and Yushiro, then fight Susumaru and Yahaba.

Water forms: Water Surface Slash, Water Wheel, Flowing Dance, Striking Tide.
Flame starts with Unknowing Fire, Rising Scorching Sun, and Blazing Universe; Mizunoto unlocks Blooming Flame Undulation, and Mizunoe unlocks Flame Tiger.
Mizunoto rank also unlocks advanced forms for Water and Wind.

## Demon path (6 levels)

Nighttime hunts through villages and forests against humans, hunters and demon
slayers, ending in a duel with Kurenai, the Blazing Captain. Level 5 is a
**daylight mansion raid**: if a demon is exposed to open sky it catches fire and
burns to death rapidly - stay under the roofs. Demons also regenerate passively
and can Devour to heal. Demon players can cycle Crimson Hunger, Forest Hand,
Swamp Demon Art, Temari Demon Art, and Arrow Demon Art loadouts.

## Day/night sun rule

Any level flagged daytime burns demons that stand uncovered by geometry -
including you when playing the demon path.

## Tech notes

- Fixed-timestep loop (60 Hz) on a dedicated thread, BufferStrategy rendering,
  internal 1280x720 buffer letterboxed to the window.
- `Glow` implements a custom additive `Composite` plus separable Gaussian
  convolution over a half-res capture buffer: effects are drawn twice (scene +
  glow layer), blurred twice, then composited additively for real bloom.
- Physics: AABB vs rectangle platforms with one-way platforms, coyote time,
  jump buffering, variable jump height, wall slide/jump, air dash.
- Juice: hitstop, screen shake, squash & stretch, dash afterimages, damage
  popups, delayed-damage HP bar, vignettes at low HP/burning.

## Layout

```
src/game/    all game code (~20 classes)
bin/         compiled classes
tools/       portable JDK
run.bat/.sh  build+launch scripts
```

Fan project for personal use; Demon Slayer is the property of Koyoharu Gotouge / Shueisha.
