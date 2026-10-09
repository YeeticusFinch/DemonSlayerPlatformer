package game;

import java.awt.*;

final class AbilityCast {
    static final Color WATER = new Color(70, 150, 255), WATER_HI = new Color(190, 230, 255);
    static final Color FLAME = new Color(255, 110, 30), FLAME_HI = new Color(255, 220, 120);
    static final Color WIND = new Color(70, 205, 110), WIND_HI = new Color(205, 255, 205);
    static final Color BLOOD = new Color(205, 20, 48), BLOOD_HI = new Color(255, 95, 120);
    static final Color NEZUKO = new Color(235, 28, 92), NEZUKO_HI = new Color(255, 86, 190);
    static final Color HAND = new Color(96, 140, 70), HAND_HI = new Color(150, 200, 110);

    static void cast(World w, Fighter p, Ability a) {
        int f = p.facing();
        boolean fr = p.team == 0;
        float px = p.x + f * 24, py = p.y - 8;
        p.noteAction();
        switch (a.kind) {
            case WAVE_PROJ -> {
                p.startAttack(0.3f);
                p.swingSide *= -1;
                w.meleeStrike(p, 110, 36, 260, 120, Math.toRadians(f == 1 ? -14 : 194), Math.toRadians(200), WATER);
                w.parts.burst(Particles.DROP, px, py, 16, 260, 0.4f, 6, WATER_HI, 300, 0.95f);
            }
            case WHEEL -> {
                Effect e = new Effect(Effect.WHEEL, p, fr).followAt(0, 0)
                        .radius(84).life(0.55f).damage(14, 150, 70).tickEvery(0.11f).colors(WATER, WATER_HI);
                e.spin = 9;
                w.effects.add(e);
                w.parts.burst(Particles.DROP, px, py, 12, 210, 0.35f, 5, WATER_HI, 260, 0.95f);
                p.vx = f * 680;
                p.vy = -740;
                startFlip(p);
            }
            case FLOWING_DANCE -> {
                Effect rib = new Effect(Effect.WATER_RIBBON, p, fr).life(1.15f).colors(WATER, WATER_HI);
                rib.x = p.x;
                rib.y = p.y - 6;
                w.effects.add(rib);
                // 13 ticks, phase step pi/2 -> exactly 3 full sine periods
                for (int i = 0; i < 13; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.085f, () -> {
                        if (p.dead) return;
                        p.vx = p.facing() * 960;
                        p.vy = -480 * FMath.sin(idx * (float) (Math.PI / 2));
                        double ang = Math.toRadians(p.facingRight ? -30 : 210) + (idx % 2 == 0 ? -0.5 : 0.5);
                        w.meleeStrike(p, 84 + (idx % 3) * 6, 40, 200, 80, ang, 2.0f, idx % 2 == 0 ? WATER : WATER_HI);
                        w.parts.burst(Particles.DROP, p.x + p.facing() * 40, p.y - 6, 10, 260, 0.3f, 5, WATER_HI, 340, 0.95f);
                    });
                }
            }
            case TIDE -> {
                setCharge(p, 0.75f);
                p.startAttack(0.75f);
                for (int i = 0; i < 15; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.05f, () -> {
                        if (p.dead) return;
                        p.vx = 0;
                        if (p.onGround) p.vy = Math.max(0, p.vy);
                        w.parts.spawn(Particles.DROP, p.x + FMath.rand(-18, 18), p.y - FMath.rand(18, 52),
                                FMath.rand(-35, 35), FMath.rand(-80, 15), 0.34f, 5, WATER_HI, 180, 0.96f);
                    });
                }
                w.schedule(0.75f, () -> strikingTide(w, p));
            }
            case BLESSED_RAIN -> {
                setCharge(p, 0.5f);
                Effect charge = new Effect(Effect.FLUX, p, fr).at(p.x, p.y - 8)
                        .life(0.52f).colors(WATER, WATER_HI);
                w.effects.add(charge);
                w.schedule(0.5f, () -> {
                    if (p.dead) return;
                    setCharge(p, 0);
                    p.startAttack(0.55f);
                    p.swingSide *= -1;
                    p.vx = p.facing() * 780;
                    p.vy = Math.min(p.vy, -120);
                    Effect aura = new Effect(Effect.HEAL_AURA, p, fr).followAt(0, 0)
                            .radius(78).life(0.85f).damage(36, 80, 80).tickEvery(0.22f).demonsOnly().colors(WATER_HI, new Color(245, 255, 255));
                    w.effects.add(aura);
                    for (int i = 0; i < 12; i++) {
                        final int idx = i;
                        w.schedule(idx * 0.055f, () -> {
                            if (p.dead) return;
                            p.vx = p.facing() * 780;
                            p.vy = FMath.approach(p.vy, -80, 120);
                            w.parts.spawn(Particles.DROP, p.x + FMath.rand(-42, 42), p.y - FMath.rand(46, 96),
                                    FMath.rand(-18, 18), FMath.rand(55, 115), 0.65f, 4, WATER_HI, 260, 0.985f);
                        });
                    }
                    if (p.team == 0) p.hp = Math.min(p.maxHp, p.hp + 8);
                    w.parts.burst(Particles.DROP, p.x, p.y - 38, 14, 120, 0.72f, 5, WATER_HI, 300, 0.985f);
                });
            }
            case WHIRL -> {
                setCharge(p, 0.5f);
                Effect aura = new Effect(Effect.FLUX, p, fr).at(p.x, p.y - 8)
                        .life(0.52f).colors(WATER, WATER_HI);
                w.effects.add(aura);
                for (int i = 0; i < 10; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.05f, () -> {
                        if (p.dead) return;
                        p.vx = 0;
                        if (p.onGround) p.vy = 0;
                        w.parts.spawn(Particles.DROP, p.x + FMath.rand(-18, 18), p.y - FMath.rand(24, 72),
                                FMath.rand(-40, 40), FMath.rand(-90, 25), 0.38f, 5, WATER_HI, 260, 0.97f);
                    });
                }
                w.schedule(0.5f, () -> waterWhirlpoolRelease(w, p));
            }
            case FIRE_DASH -> {
                setCharge(p, 0.42f);
                Effect aura = new Effect(Effect.FLUX, p, fr).at(p.x, p.y - 8)
                        .life(0.45f).colors(FLAME, FLAME_HI);
                w.effects.add(aura);
                for (int i = 0; i < 6; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.055f, () -> {
                        if (p.dead) return;
                        w.parts.burst(Particles.FIRE, p.x + FMath.rand(-12, 12), p.y + FMath.rand(-36, 6),
                                3, 130, 0.42f, 8, FLAME_HI, -160, 0.93f);
                    });
                }
                w.schedule(0.42f, () -> {
                    if (p.dead) return;
                    setCharge(p, 0);
                    unknowingFire(w, p);
                });
            }
            case RISING_SUN -> {
                p.vy = -940;
                startFlip(p);
                for (int i = 0; i < 12; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.045f, () -> {
                        if (p.dead) return;
                        p.vy = Math.min(p.vy, -380);
                        p.startAttack(0.13f);
                        p.swingSide = 1;
                        double up = Math.toRadians(-90 + (idx - 5.5f) * 5.5f);
                        w.meleeStrike(p, 132, 9.6f, 130, 720, up, 2.4f, idx % 2 == 0 ? FLAME : FLAME_HI);
                        Effect arc = new Effect(Effect.ARC, p, fr).at(p.x + p.facing() * 14, p.y - 40)
                                .radius(156 + idx * 6).life(0.2f).damage(6, 90, 520).pierce(1).colors(FLAME, FLAME_HI);
                        arc.angle = (float) up;
                        arc.ex1 = (float) Math.toRadians(190);
                        arc.ex2 = 24;
                        w.effects.add(arc);
                        w.parts.burst(Particles.EMBER, p.x + p.facing() * 16, p.y - 42, 7, 240, 0.38f, 8, FLAME_HI, -140, 0.94f);
                    });
                }
                w.schedule(1f, () -> flameAerialBurst(w, p, p.x, p.y - 145, 220, 36, 180, 640));
            }
            case BLAZING_SLAM -> {
                p.vy = -960;
                p.vx = f * 130;
                startFlip(p);
                if (p instanceof Player pl) {
                    pl.slamPending = true;
                    pl.blazingSlamSlash = true;
                    pl.slamAge = 0;
                    for (int i = 0; i <= 10; i++) {
                        w.schedule(0.52f + i * 0.05f, () -> {
                            if (p.dead || p.onGround) return;
                            p.vx = 0;
                            p.vy = 0;
                        });
                    }
                    w.schedule(1.05f, () -> {
                        if (p.dead) return;
                        p.vy = 1900;
                        p.startAttack(0.45f);
                    });
                } else {
                    for (int i = 0; i <= 10; i++) {
                        w.schedule(0.52f + i * 0.05f, () -> {
                            if (p.dead || p.onGround) return;
                            p.vx = 0;
                            p.vy = 0;
                        });
                    }
                    w.schedule(1.05f, () -> { if (!p.dead) p.vy = 1900; });
                    w.schedule(1.28f, () -> flameSlamImpact(w, p));
                }
            }
            case TIGER_VOLLEY -> {
                // 0.5s charge: sword raised overhead, fire aura + flame particles
                setCharge(p, 0.5f);
                Effect aura = new Effect(Effect.FLUX, p, fr).at(p.x, p.y - 8)
                        .life(0.55f).colors(FLAME, FLAME_HI);
                w.effects.add(aura);
                for (int i = 0; i < 8; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.06f, () -> {
                        if (p.dead) return;
                        w.parts.burst(Particles.FIRE, p.x + FMath.rand(-14, 14), p.y + FMath.rand(-34, 16),
                                3, 140, 0.45f, 8, FLAME_HI, -170, 0.93f);
                    });
                }
                w.schedule(0.5f, () -> {
                    if (p.dead) return;
                    setCharge(p, 0);
                    p.startAttack(0.3f);
                    p.swingSide *= -1;
                    launchTiger(w, p);
                });
            }
            case FLAME_UNDULATION -> {
                p.startAttack(0.5f);
                p.comboStep = 1;
                p.vx = 0;
                if (p.onGround) p.vy = 0;
                for (int i = 0; i <= 20; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.05f, () -> {
                        if (p.dead) return;
                        p.vx = 0;
                        if (p.onGround) p.vy = 0;
                        if (idx == 10) {
                            p.startAttack(0.5f);
                            p.comboStep = 0;
                        }
                        p.swingSide = idx < 10 ? -1 : 1;
                        if (idx % 2 == 0) {
                            boolean down = idx >= 10;
                            float q = down ? (idx - 10) / 10f : idx / 10f;
                            double slashAng = p.facingRight ? (down ? Math.toRadians(-118 + q * 178) : Math.toRadians(66 - q * 184))
                                    : (down ? Math.toRadians(298 - q * 178) : Math.toRadians(114 + q * 184));
                            Effect arc = new Effect(Effect.ARC, p, fr).at(p.x + p.facing() * 18, p.y - 18)
                                    .radius(132 + q * 46).life(0.26f).colors(idx % 4 == 0 ? new Color(210, 46, 8) : FLAME, FLAME_HI);
                            arc.angle = (float) slashAng;
                            arc.ex1 = (float) Math.toRadians(126);
                            arc.ex2 = 24;
                            w.effects.add(arc);
                            w.parts.burst(Particles.FIRE, p.x + p.facing() * 34, p.y - 28 - q * 26, 9, 250, 0.4f, 9,
                                    idx % 4 == 0 ? FLAME : FLAME_HI, -170, 0.93f);
                        }
                    });
                }
                w.schedule(1f, () -> flameUndulationRelease(w, p));
            }
            case WIND_DUST -> {
                p.invulnT = Math.max(p.invulnT, 0.36f);
                p.vx = f * 1590;
                p.vy = Math.min(p.vy, -90);
                startFlip(p);
                Effect rib = new Effect(Effect.WIND_RIBBON, p, fr).life(1.125f).colors(WIND, WIND_HI);
                rib.x = px;
                rib.y = py;
                w.effects.add(rib);
                for (int i = 0; i < 12; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.055f, () -> {
                        if (p.dead) return;
                        p.vx = p.facing() * 1590;
                        double ang = Math.toRadians(p.facingRight ? -18 : 198) + (idx % 2 == 0 ? 0.42 : -0.42);
                        w.meleeStrike(p, 88, 32, 230, 120, ang, Math.toRadians(175), WIND);
                        w.parts.burst(Particles.CIRCLE, p.x + p.facing() * 34, p.y - 10, 7, 230, 0.28f, 6, WIND_HI, 110, 0.96f);
                    });
                }
            }
            case WIND_CLAWS -> {
                p.startAttack(0.32f);
                p.swingSide *= -1;
                for (int i = 0; i < 4; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.04f, () -> {
                        if (p.dead) return;
                        float yy = p.y - 42 + idx * 18;
                        Effect c = new Effect(Effect.CLAW_WAVE, p, fr).at(p.x + p.facing() * 34, yy)
                                .vel(p.facing() * 560, -36 + idx * 24).radius(42).life(1.26f)
                                .damage(30, 230, 120).pierce(1).colors(WIND, WIND_HI);
                        c.stopOnSolid = true;
                        w.effects.add(c);
                        w.meleeStrike(p, 96, 26.25f, 210, 130,
                                Math.toRadians(p.facingRight ? -42 + idx * 24 : 222 - idx * 24),
                                Math.toRadians(90), WIND);
                    });
                }
            }
            case WIND_STORM -> {
                p.startAttack(0.5f);
                p.comboStep = 1;
                p.vx = 0;
                if (p.onGround) p.vy = 0;
                for (int i = 0; i <= 20; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.05f, () -> {
                        if (p.dead) return;
                        p.vx = 0;
                        if (p.onGround) p.vy = 0;
                        if (idx == 10) {
                            p.startAttack(0.5f);
                            p.comboStep = 0;
                        }
                        p.swingSide = idx < 10 ? -1 : 1;
                        if (idx % 2 == 0) {
                            boolean up = idx >= 10;
                            float q = up ? (idx - 10) / 10f : idx / 10f;
                            double base = p.facingRight ? (up ? Math.toRadians(55 - q * 120) : Math.toRadians(-85 + q * 140))
                                    : (up ? Math.toRadians(125 + q * 120) : Math.toRadians(265 - q * 140));
                            Effect arc = new Effect(Effect.ARC, p, fr).at(p.x + p.facing() * 14, p.y - 18)
                                    .radius(128 + q * 34).life(0.24f).colors(idx % 4 == 0 ? new Color(8, 12, 10) : WIND, WIND_HI);
                            arc.angle = (float) base;
                            arc.ex1 = (float) Math.toRadians(118);
                            arc.ex2 = 22;
                            w.effects.add(arc);
                            w.parts.burst(Particles.CIRCLE, p.x + p.facing() * 36, p.y - 26 - q * 28, 5, 180, 0.3f, 7,
                                    idx % 4 == 0 ? new Color(10, 14, 12) : WIND_HI, 70, 0.96f);
                        }
                    });
                }
                w.schedule(1f, () -> {
                    if (p.dead) return;
                    Effect e = new Effect(Effect.WHIRL, p, fr).at(p.x, p.bottom())
                            .radius(281).life(2f).damage(11, 55, 430).tickEvery(0.09f).colors(WIND, new Color(12, 18, 14));
                    e.pull = 1;
                    e.ex1 = 1;
                    e.ex2 = 3;
                    w.effects.add(e);
                    for (int i = 0; i < 30; i++) {
                        final int idx = i;
                        w.schedule(idx * (2f / 30f), () -> {
                            if (p.dead) return;
                            p.vx = 0;
                            if (p.onGround) p.vy = 0;
                            p.startAttack(0.1f);
                            p.swingSide *= -1;
                            if (idx % 5 == 0) startFlip(p);
                            w.meleeStrike(p, 118, 7, 80, 260,
                                    -Math.PI / 2 + idx * 0.52f, Math.toRadians(120), WIND);
                            w.parts.burst(Particles.CIRCLE, p.x + FMath.rand(-54, 54), p.y - FMath.rand(30, 150), 7, 230, 0.35f, 8, WIND_HI, 80, 0.97f);
                        });
                    }
                });
            }
            case WIND_MOUNTAIN -> {
                p.vx = 0;
                p.vy = -1120;
                setCharge(p, 0.72f);
                startFlip(p);
                Effect swirl = new Effect(Effect.WHIRL, p, fr).followAt(0, 0)
                        .radius(94).life(0.72f).colors(WIND, new Color(8, 12, 10));
                swirl.ex2 = 4;
                w.effects.add(swirl);
                for (int i = 0; i < 12; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.052f, () -> {
                        if (p.dead) return;
                        if (idx > 4) p.vy = FMath.approach(p.vy, -35, 220);
                        p.vx = 0;
                        w.parts.burst(Particles.CIRCLE, p.x + FMath.rand(-36, 36), p.y + FMath.rand(-66, 4), 5, 175, 0.34f, 7,
                                idx % 2 == 0 ? WIND_HI : new Color(10, 14, 12), 90, 0.96f);
                    });
                }
                w.schedule(0.72f, () -> coldMountainSlam(w, p));
            }
            case WIND_TREE -> {
                p.vx = f * 720;
                p.vy = -1020;
                startFlip(p);
                for (int i = 0; i < 5; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.1f, () -> {
                        if (p.dead) return;
                        float xx = p.x + p.facing() * (90 + idx * 44);
                        float yy = p.y - 88 - idx * 14;
                        Effect arc = new Effect(Effect.ARC, p, fr).at(xx, yy)
                                .radius(140 + idx * 32).life(0.36f).damage(56, 270, 250)
                                .pierce(1).colors(WIND, WIND_HI);
                        arc.angle = (float) Math.toRadians(p.facingRight ? 4 : 176);
                        arc.ex1 = (float) Math.toRadians(230);
                        arc.ex2 = 28 + idx * 4;
                        w.effects.add(arc);
                        w.parts.burst(Particles.CIRCLE, xx, yy, 8, 240, 0.34f, 7, WIND_HI, -90, 0.96f);
                    });
                }
            }
            case WIND_MIST -> {
                p.vy = -840;
                startFlip(p);
                Effect e = new Effect(Effect.WHIRL, p, fr).at(p.x, p.y - 84)
                        .radius(132).life(1.1f).damage(13, 80, 260).tickEvery(0.1f).colors(WIND, new Color(10, 16, 14));
                e.pull = 1.1f;
                e.ex2 = 3;
                w.effects.add(e);
                for (int i = 0; i < 8; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.08f, () -> {
                        if (p.dead) return;
                        double ang = -Math.PI / 2 + idx * 0.78f;
                        w.meleeStrike(p, 104, 18, 110, 300, ang, Math.toRadians(135), WIND);
                        w.parts.burst(Particles.CIRCLE, p.x + FMath.rand(-38, 38), p.y - FMath.rand(42, 118), 7, 220, 0.34f, 7, WIND_HI, 80, 0.96f);
                    });
                }
            }
            case WIND_BLACK -> {
                p.startAttack(0.72f);
                p.vy = -820;
                p.vx = -f * 160;
                startFlip(p);
                for (int i = 0; i < 7; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.045f, () -> {
                        if (p.dead) return;
                        p.vx = -p.facing() * 130;
                        p.swingSide *= -1;
                        w.parts.burst(Particles.CIRCLE, p.x + FMath.rand(-24, 24), p.y - FMath.rand(28, 72), 4, 160, 0.28f, 6,
                                idx % 2 == 0 ? new Color(8, 10, 10) : new Color(170, 20, 32), 80, 0.95f);
                    });
                }
                w.schedule(0.34f, () -> galeSuddenGusts(w, p));
            }
            case WIND_EIGHT, WIND_NINE, WIND_TEN -> {
                w.popup(p.x, p.y - p.h - 14, "Placeholder form", WIND_HI);
            }
            case BLOOD_BOLT -> {
                Effect b = new Effect(Effect.BLOOD_BOLT, p, fr).at(px, py).vel(f * 790, -18)
                        .radius(22).life(0.9f).damage(30, 250, 120).colors(BLOOD, BLOOD_HI);
                b.stopOnSolid = true;
                w.effects.add(b);
                w.parts.burst(Particles.SPARK, px, py, 8, 180, 0.25f, 6, BLOOD_HI, 0, 0.9f);
            }
            case CLAW_WAVES -> {
                for (int i = 0; i < 3; i++) {
                    final int idx = i;
                    w.schedule(i * 0.07f, () -> {
                        Effect c = new Effect(Effect.CLAW_WAVE, p, fr).at(p.x + p.facing() * 30, p.y - 10)
                                .vel(p.facing() * 460, 0).radius(46).life(0.36f).damage(18, 190, 90).pierce(1)
                                .colors(BLOOD, BLOOD_HI);
                        c.stopOnSolid = true;
                        w.effects.add(c);
                    });
                }
            }
            case NOVA -> {
                Effect n = new Effect(Effect.NOVA, p, fr).at(p.x, p.y - 6)
                        .radius(158).life(0.5f).damage(44, 430, 310).colors(BLOOD, BLOOD_HI);
                w.effects.add(n);
                w.parts.ring(p.x, p.y - 6, BLOOD_HI);
                w.parts.burst(Particles.EMBER, p.x, p.y, 22, 320, 0.6f, 9, BLOOD_HI, 60, 0.93f);
                w.cam.shake(6, 0.25f);
            }
            case DEVOUR -> {
                Effect h = new Effect(Effect.HEAL_AURA, p, false).at(p.x, p.y - 4).radius(46).life(0.9f);
                w.effects.add(h);
                p.hp = Math.min(p.maxHp, p.hp + 30);
                w.popup(p.x, p.y - p.h - 8, "+30", new Color(140, 255, 170));
                w.parts.burst(Particles.CIRCLE, p.x, p.y, 16, 150, 0.6f, 8, new Color(255, 80, 110), -140, 0.95f);
            }
            case NEZUKO_NAILS -> {
                p.startAttack(0.32f);
                p.swingSide = 1;
                p.vy = Math.min(p.vy, -420);
                w.meleeStrike(p, 86, 28, 120, 560, -Math.PI / 2, Math.toRadians(145), NEZUKO_HI);
                Effect arc = new Effect(Effect.ARC, p, fr).at(p.x + f * 16, p.y - 46)
                        .radius(122).life(0.28f).damage(22, 80, 620).pierce(2).colors(NEZUKO, NEZUKO_HI);
                arc.angle = (float) -Math.PI / 2;
                arc.ex1 = (float) Math.toRadians(170);
                arc.ex2 = 24;
                w.effects.add(arc);
                w.parts.burst(Particles.FIRE, p.x + f * 22, p.y - 44, 16, 280, 0.42f, 9, NEZUKO_HI, -170, 0.94f);
            }
            case NEZUKO_EXPLODING_BLOOD -> {
                Effect bolt = new Effect(Effect.BLOOD_BOLT, p, fr).at(px, py).vel(f * 820, -18)
                        .radius(24).life(0.42f).damage(18, 180, 110).pierce(0).demonsOnly().colors(NEZUKO, NEZUKO_HI);
                bolt.stopOnSolid = true;
                bolt.ex2 = 6;
                w.effects.add(bolt);
                final float bx = p.x + f * 360, by = p.y - 52;
                w.schedule(0.34f, () -> { if (!bolt.dead) combustibleVortexGround(w, p, bx, by, 118, 1.0f, true); });
            }
            case NEZUKO_SCRATCHING -> {
                p.startAttack(1.15f);
                for (int i = 0; i < 5; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.2f, () -> {
                        if (p.dead) return;
                        p.swingSide = idx % 2 == 0 ? 1 : -1;
                        p.vx += p.facing() * 70;
                        double scratchAng = Math.toRadians(p.facingRight ? -38 + idx * 18 : 218 - idx * 18);
                        w.meleeStrike(p, 82, 15, 160, 120, scratchAng, Math.toRadians(115), idx % 2 == 0 ? NEZUKO : NEZUKO_HI);
                        Effect c = new Effect(Effect.CLAW_WAVE, p, fr).at(p.x + p.facing() * 34, p.y - 34 + idx * 10)
                                .vel(p.facing() * 460, -30 + idx * 14).radius(46).life(0.36f).damage(12, 170, 115).pierce(1).colors(NEZUKO, NEZUKO_HI);
                        w.effects.add(c);
                    });
                }
                w.schedule(1.0f, () -> {
                    if (p.dead) return;
                    for (int i = 0; i < 2; i++) {
                        int s = i == 0 ? -1 : 1;
                        double slashAngle = Math.toRadians(p.facingRight ? s * 45 : 180 - s * 45);
                        w.meleeStrike(p, 104, 26, 310, 230, slashAngle, Math.toRadians(70), i == 0 ? NEZUKO : NEZUKO_HI);
                        Effect slash = new Effect(Effect.CLAW_WAVE, p, fr).at(p.x + p.facing() * 56, p.y - 30)
                                .vel((float) Math.cos(slashAngle) * 360, (float) Math.sin(slashAngle) * 360)
                                .radius(72).life(0.44f).damage(20, 290, 220).pierce(2)
                                .colors(i == 0 ? NEZUKO : NEZUKO_HI, NEZUKO_HI);
                        w.effects.add(slash);
                    }
                    w.cam.shake(5, 0.18f);
                });
            }
            case NEZUKO_HEEL_BASH -> {
                lockFacing(p, 1.48f);
                setNezukoPose(p, 2, 1.34f);
                p.startAttack(1.42f);
                p.vy = -1310;
                p.vx = -f * 120;
                Effect aura = new Effect(Effect.HEAL_AURA, p, fr).followAt(0, 0).radius(76).life(1.0f).colors(NEZUKO, NEZUKO_HI);
                aura.ex2 = 6;
                w.effects.add(aura);
                for (int i = 0; i <= 28; i++) {
                    w.schedule(0.34f + i * 0.026f, () -> {
                        if (p.dead || p.onGround) return;
                        setNezukoPose(p, 2, 0.08f);
                        p.vx = 0;
                        p.vy = 0;
                    });
                }
                w.schedule(1.08f, () -> { if (!p.dead) { setNezukoPose(p, 3, 0.36f); p.vx = 0; p.vy = 1580; p.startAttack(0.38f); } });
                w.schedule(1.31f, () -> nezukoHeelImpact(w, p, 215, 40));
            }
            case NEZUKO_SPIN_KICK -> {
                lockFacing(p, 0.92f);
                p.invulnT = Math.max(p.invulnT, 0.55f);
                p.vx = f * 1050;
                p.vy = Math.min(p.vy, -160);
                startFlip(p);
                setNezukoPose(p, 4, 0.38f);
                for (int i = 0; i < 8; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.045f, () -> {
                        if (p.dead) return;
                        p.vx = p.facing() * 1050;
                        setNezukoPose(p, 4, 0.08f);
                        w.parts.spawn(Particles.FIRE, p.x - p.facing() * 18, p.y + FMath.rand(-42, -4),
                                -p.facing() * FMath.rand(120, 260), FMath.rand(-60, 60), 0.42f, 9,
                                FMath.chance(0.5f) ? NEZUKO : NEZUKO_HI, -120, 0.94f);
                        w.meleeStrike(p, 80, 13, 210, 110, Math.toRadians(p.facingRight ? -15 : 195), Math.toRadians(100), NEZUKO_HI);
                    });
                }
                w.schedule(0.38f, () -> {
                    if (p.dead) return;
                    if (p instanceof Player pl) pl.flipActive = false;
                    setNezukoPose(p, 2, 0.34f);
                    p.vx = 0;
                    p.vy = 0;
                    p.startAttack(0.36f);
                });
                w.schedule(0.63f, () -> {
                    if (p.dead) return;
                    setNezukoPose(p, 3, 0.28f);
                    p.vy = 460;
                    float tx = p.x + p.facing() * 42;
                    combustibleVortexGround(w, p, tx, p.bottom(), 112, 1.0f, false);
                });
            }
            case NEZUKO_FLYING_KICK -> {
                lockFacing(p, 0.72f);
                p.startAttack(0.45f);
                setNezukoPose(p, 1, 0.48f);
                p.vx = f * 1120;
                p.vy = -220;
                w.schedule(0.22f, () -> {
                    if (p.dead) return;
                    setNezukoPose(p, 1, 0.24f);
                    p.vx = -p.facing() * 520;
                    p.vy = -720;
                    startFlip(p);
                    w.meleeStrike(p, 112, 34, 640, 250, Math.toRadians(p.facingRight ? -10 : 190), Math.toRadians(80), NEZUKO_HI);
                    w.parts.burst(Particles.FIRE, p.x + p.facing() * 42, p.y - 16, 18, 340, 0.48f, 10, NEZUKO_HI, -120, 0.93f);
                });
            }
            case SWAMP_FLURRY -> {
                p.startAttack(0.55f);
                for (int i = 0; i < 8; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.055f, () -> {
                        if (p.dead) return;
                        p.swingSide *= -1;
                        p.vx += p.facing() * 65;
                        double ang = Math.toRadians(p.facingRight ? -45 + idx * 18 : 225 - idx * 18);
                        w.meleeStrike(p, 72, 11, 120, idx % 2 == 0 ? 55 : 140, ang, 1.35f, new Color(34, 128, 118));
                        w.parts.burst(Particles.CIRCLE, p.x + p.facing() * 34, p.y - 12 + (idx % 3) * 9,
                                5, 150, 0.22f, 5, new Color(140, 220, 210), 20, 0.96f);
                    });
                }
            }
            case AQUATIC_DASH -> {
                Effect cloud = new Effect(Effect.SWAMP_CLOUD, p, fr).at(p.x, p.y - 18)
                        .radius(98).life(0.78f).colors(new Color(5, 22, 18), new Color(32, 100, 90));
                w.effects.add(cloud);
                p.invulnT = Math.max(p.invulnT, 0.32f);
                p.vx = f * 980;
                p.vy = Math.min(p.vy, -70);
                for (int i = 0; i < 8; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.04f, () -> {
                        if (p.dead) return;
                        p.vx = p.facing() * 980;
                        p.invulnT = Math.max(p.invulnT, 0.08f);
                        w.parts.spawn(Particles.DROP, p.x - p.facing() * 8, p.y + FMath.rand(-38, 8),
                                -p.facing() * FMath.rand(180, 360), FMath.rand(-80, 80), 0.36f, 7,
                                new Color(25, 118, 116), 120, 0.95f);
                    });
                }
            }
            case SWAMP_HANDS -> {
                for (int i = 0; i < 5; i++) {
                    final float sx = p.x + f * (86 + i * 62) + FMath.rand(-18, 18);
                    w.schedule(i * 0.08f, () -> {
                        float gy = w.groundYUnder(sx, p.y - 40);
                        if (gy > 9000) return;
                        Effect hand = new Effect(Effect.SWAMP_HANDS, p, fr).at(sx, gy)
                                .radius(38).life(0.78f).damage(20.25f, 160, 250)
                                .colors(new Color(235, 238, 232), new Color(24, 84, 76));
                        hand.ex1 = 92;
                        w.effects.add(hand);
                    });
                }
            }
            case SWAMP_STEP -> {
                float startG = w.groundYUnder(p.x, p.y - 60);
                if (startG > 9000) startG = p.bottom();
                w.effects.add(new Effect(Effect.SWAMP_PUDDLE, p, fr).at(p.x, startG).radius(58).life(0.65f));
                final float tx = FMath.clamp(p.x + f * 430, p.w / 2f, w.level.w - p.w / 2f);
                w.schedule(0.16f, () -> {
                    if (p.dead) return;
                    float gy = w.groundYUnder(tx, p.y - 120);
                    if (gy > 9000) gy = p.bottom();
                    p.x = tx;
                    p.y = gy - p.h / 2f;
                    p.vx = p.facing() * 260;
                    p.vy = -120;
                    p.invulnT = Math.max(p.invulnT, 0.28f);
                    w.effects.add(new Effect(Effect.SWAMP_PUDDLE, p, fr).at(p.x, gy).radius(66).life(0.7f));
                    w.parts.burst(Particles.DROP, p.x, gy - 10, 18, 250, 0.4f, 7, new Color(30, 112, 106), 60, 0.94f);
                });
            }
            case BALL_KICK -> launchTemari(w, p, p.x + f * 24, p.y - 18, f * 720, -170, 1f);
            case SPINNING_THROW -> {
                p.vx = -f * 360;
                p.vy = -520;
                startFlip(p);
                launchTemari(w, p, p.x + f * 18, p.y - 28, f * 620, -230, 1f);
            }
            case PIERCING_KICK -> {
                p.invulnT = Math.max(p.invulnT, 0.24f);
                p.vx = f * 1180;
                p.vy = Math.min(p.vy, -40);
                for (int i = 0; i < 7; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.04f, () -> {
                        if (p.dead) return;
                        p.vx = p.facing() * 1180;
                        w.meleeStrike(p, 76, 19, 260, 125, Math.toRadians(p.facingRight ? -8 : 188), 1.35f, new Color(235, 190, 74));
                    });
                }
            }
            case SIXFOLD_TEMARI -> {
                for (int i = 0; i < 6; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.06f, () -> launchTemari(w, p, p.x + p.facing() * 24, p.y - 34 + idx * 8,
                            p.facing() * (560 + idx * 28), -260 + idx * 72, 0.85f));
                }
            }
            case SPIRALING_SHOT -> {
                for (int i = 0; i < 9; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.07f, () -> launchTemari(w, p, p.x + p.facing() * 24, p.y - 28,
                            p.facing() * 610, FMath.sin(idx * 1.45f) * 360, 0.75f));
                }
            }
            case BOULDER_TOSS -> {
                Effect b = new Effect(Effect.BOULDER, p, fr).at(p.x + f * 48, p.y - 38).vel(f * 560, -110)
                        .radius(34).life(1.45f).damage(42, 390, 250).pierce(2).colors(new Color(120, 110, 96), new Color(220, 70, 80));
                b.grav = 580;
                w.effects.add(b);
                arrowFlash(w, p.x + f * 28, p.y - 38, f * 300, -30, fr, 0.45f);
            }
            case SMACK_DOWN -> {
                float ax = p.x + f * 120, ay = p.y - 250;
                Effect ar = new Effect(Effect.ARROW, p, fr).at(ax, ay).vel(0, 780).radius(42).life(0.65f)
                        .damage(36, 80, 620).colors(new Color(190, 24, 38), new Color(255, 120, 130));
                ar.angle = (float) Math.PI / 2;
                w.effects.add(ar);
            }
            case CHASER_ARROW -> {
                Effect ar = new Effect(Effect.ARROW, p, fr).at(p.x + f * 35, p.y - 24).vel(f * 380, -60).radius(30).life(1.8f)
                        .damage(22, 240, 130).colors(new Color(190, 24, 38), new Color(255, 120, 130));
                ar.pull = 1;
                ar.ex1 = 620;
                w.effects.add(ar);
            }
            case ERUPTION -> {
                for (int i = -1; i <= 1; i++) {
                    final int idx = i;
                    w.schedule((idx + 1) * 0.08f, () -> {
                        float sx = p.x + p.facing() * (95 + (idx + 1) * 70);
                        float gy = w.groundYUnder(sx, p.y - 40);
                        if (gy > 9000) return;
                        Effect ar = new Effect(Effect.ARROW, p, fr).at(sx, gy - 8).vel(idx * 170, -760).radius(38).life(0.8f)
                                .damage(24, 130, 520).colors(new Color(190, 24, 38), new Color(255, 120, 130));
                        ar.angle = (float) Math.atan2(ar.vy, ar.vx == 0 ? 1 : ar.vx);
                        w.effects.add(ar);
                    });
                }
            }
            case TORRENTIAL_ARROWS -> {
                for (int i = 0; i < 12; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.055f, () -> {
                        float sx = p.x + p.facing() * (80 + idx * 42) + FMath.rand(-30, 30);
                        Effect ar = new Effect(Effect.ARROW, p, fr).at(sx, p.y - 420 - FMath.rand(0, 80)).vel(FMath.rand(-60, 60), 760).radius(30).life(0.85f)
                                .damage(18, 80, 360).colors(new Color(190, 24, 38), new Color(255, 120, 130));
                        ar.angle = (float) Math.PI / 2;
                        w.effects.add(ar);
                    });
                }
            }
            case HAND_SPIKES -> {
                for (int i = 0; i < 5; i++) {
                    final float sx = p.x + f * (110 + i * 95) + FMath.rand(-26, 26);
                    w.schedule(i * 0.11f, () -> {
                        float gy = w.groundYUnder(sx, p.y - 40);
                        if (gy > 9000) return;
                        Effect e = new Effect(Effect.HAND_SPIKE, p, fr).at(sx, gy)
                                .radius(24).life(0.9f).damage(19.5f, 260, 380)
                                .colors(HAND, HAND_HI);
                        e.ex1 = FMath.rand(150, 210);
                        w.effects.add(e);
                        w.cam.shake(2.5f, 0.12f);
                    });
                }
            }
            case HAND_GRASP -> {
                p.startAttack(0.42f);
                p.swingSide *= -1;
                for (int i = 0; i < 3; i++) {
                    final int idx = i;
                    w.schedule(idx * 0.07f, () -> {
                        Effect h = new Effect(Effect.HAND_SWING, p, fr).at(p.x + p.facing() * 34, p.y - 36)
                                .vel(p.facing(), 0).radius(46).life(0.46f).damage(39.6f, 660, 430).colors(HAND, HAND_HI);
                        h.ex1 = 128 + idx * 18;
                        w.effects.add(h);
                    });
                }
                w.parts.burst(Particles.CIRCLE, p.x + f * 70, p.y - 44, 14, 220, 0.38f, 8, HAND_HI, 80, 0.93f);
                w.cam.shake(5, 0.22f);
            }
            case HAND_SLAM -> {
                float tx = p.x + f * 190;
                float gy = w.groundYUnder(tx, p.y);
                if (gy > 9000) gy = p.bottom();
                final float impactX = tx, impactY = gy;
                Effect charge = new Effect(Effect.HAND_CHARGE, p, fr).at(p.x, p.y - p.h * 0.35f).radius(74).life(0.62f).colors(HAND, HAND_HI);
                charge.vx = f;
                charge.ex1 = impactX;
                charge.ex2 = impactY;
                w.effects.add(charge);
                w.schedule(0.62f, () -> earthClutchImpact(w, p, impactX, impactY));
            }
            case HAND_AURA -> {
                Effect aura = new Effect(Effect.HAND_AURA, p, fr).at(p.x, p.y - p.h * 0.22f)
                        .radius(176).life(0.72f).damage(44.2f, 460, 260).colors(HAND, HAND_HI);
                w.effects.add(aura);
                w.parts.ring(p.x, p.y - p.h * 0.2f, HAND_HI);
                w.cam.shake(5, 0.24f);
            }
            case SPIDER_GROUND_POUND -> spiderGroundPound(w, p);
            case SPIDER_FISSURE -> spiderFissure(w, p);
            case SPIDER_STRIKE -> spiderConcussiveStrike(w, p);
            case SPIDER_POUNCE -> spiderPounce(w, p);
        }
    }

    private static void spiderGroundPound(World w, Fighter p) {
        int f = p.facing();
        boolean fr = p.team == 0;
        p.startAttack(0.9f);
        p.vx = f * 120;
        p.vy = -980;
        w.schedule(0.38f, () -> {
            if (p.dead) return;
            p.vx = 0;
            p.vy = 0;
        });
        w.schedule(0.63f, () -> {
            if (p.dead) return;
            float gy = w.groundYUnder(p.x, p.y);
            if (gy > 9000) gy = p.bottom() + 260;
            p.y = gy - p.h / 2f;
            p.vy = 0;
            spiderImpact(w, p, p.x, gy, 180, 48, fr);
        });
    }

    private static void spiderFissure(World w, Fighter p) {
        p.startAttack(0.44f);
        int f = p.facing();
        boolean fr = p.team == 0;
        float dmgScale = spiderFatherDamageScale(p);
        w.schedule(0.3f, () -> {
            if (p.dead) return;
            w.cam.shake(7, 0.24f);
            for (int i = 0; i < 6; i++) {
                final int idx = i;
                w.schedule(idx * 0.055f, () -> {
                    float sx = p.x + f * (85 + idx * 78);
                    float gy = w.groundYUnder(sx, p.y - 50);
                    if (gy > 9000) return;
                    Effect b = new Effect(Effect.HAND_SPIKE, p, fr).at(sx, gy).radius(30).life(0.82f)
                            .damage(24 * dmgScale, 260, 420).colors(new Color(90, 84, 92), new Color(205, 200, 210));
                    b.ex1 = 145 + idx * 10;
                    w.effects.add(b);
                });
            }
        });
    }

    private static void spiderConcussiveStrike(World w, Fighter p) {
        boolean fr = p.team == 0;
        int f = p.facing();
        float dmgScale = spiderFatherDamageScale(p);
        p.startAttack(0.34f);
        w.meleeStrike(p, 118, 34 * dmgScale, 900, 220, Math.toRadians(p.facingRight ? -6 : 186), Math.toRadians(115), new Color(122, 81, 140));
        Effect ring = new Effect(Effect.SLAM_RING, p, fr).at(p.x + f * 100, p.y - 8).radius(92).life(0.34f)
                .damage(32 * dmgScale, 760, 180).colors(new Color(122, 81, 140), new Color(210, 205, 220));
        w.effects.add(ring);
        w.cam.shake(6, 0.2f);
    }

    private static void spiderPounce(World w, Fighter p) {
        int f = p.facing();
        boolean fr = p.team == 0;
        p.startAttack(0.74f);
        p.vx = f * 1100;
        p.vy = -820;
        w.schedule(0.58f, () -> {
            if (p.dead) return;
            float gy = w.groundYUnder(p.x, p.y - 80);
            if (gy > 9000) gy = p.bottom() + 160;
            p.y = gy - p.h / 2f;
            p.vx *= 0.25f;
            p.vy = 0;
            spiderImpact(w, p, p.x, gy, 138, 38, fr);
        });
    }

    private static void spiderImpact(World w, Fighter p, float x, float gy, float r, float dmg, boolean fr) {
        dmg *= spiderFatherDamageScale(p);
        Effect ring = new Effect(Effect.SLAM_RING, p, fr).at(x, gy).radius(r).life(0.48f)
                .damage(dmg, 520, 420).colors(new Color(122, 81, 140), new Color(230, 224, 235));
        w.effects.add(ring);
        for (int dir = -1; dir <= 1; dir += 2) {
            Effect s = new Effect(Effect.SHOCK_GROUND, p, fr).at(x + dir * 28, gy - 10).vel(dir * 680, 0)
                    .radius(42).life(0.76f).damage(dmg * 0.55f, 360, 260).colors(new Color(76, 62, 76), new Color(210, 205, 220));
            w.effects.add(s);
        }
        w.parts.burst(Particles.CIRCLE, x, gy, 32, 430, 0.65f, 12, new Color(170, 160, 178), 260, 0.92f);
        w.cam.shake(13, 0.38f);
    }

    private static float spiderFatherDamageScale(Fighter p) {
        return p instanceof SpiderFather ? 0.9f : 1f;
    }

    private static void earthClutchImpact(World w, Fighter p, float tx, float gy) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        Effect ring = new Effect(Effect.SLAM_RING, p, fr).at(tx, gy).radius(180).life(0.5f)
                .colors(new Color(120, 95, 70), HAND_HI);
        w.effects.add(ring);
        for (int d = -1; d <= 1; d += 2) {
            Effect s = new Effect(Effect.SHOCK_GROUND, p, fr).at(tx + d * 34, gy - 8)
                    .vel(d * 580, 0).radius(38).life(0.85f).damage(35.2f, 320, 260)
                    .colors(new Color(120, 95, 70), HAND_HI);
            w.effects.add(s);
        }
        w.parts.burst(Particles.CIRCLE, tx, gy, 22, 300, 0.7f, 11, new Color(140, 115, 90), 420, 0.93f);
        w.cam.shake(9, 0.35f);
    }

    private static void launchTemari(World w, Fighter p, float x, float y, float vx, float vy, float scale) {
        boolean fr = p.team == 0;
        Effect ball = new Effect(Effect.TEMARI, p, fr).at(x, y).vel(vx, vy).radius(24 * scale).life(2.25f)
                .damage(26.4f * scale, 250, 180).pierce(3).colors(new Color(52, 108, 198), new Color(255, 230, 120));
        ball.grav = 760;
        w.effects.add(ball);
        w.parts.burst(Particles.SPARK, x, y, 7, 150, 0.25f, 5, new Color(255, 220, 120), 20, 0.92f);
    }

    static void combustibleVortexGround(World w, Fighter p, float x, float refY, float r, float life, boolean demonsOnly) {
        float gy = w.groundYUnder(x, refY);
        if (gy > 9000) gy = refY;
        combustibleVortex(w, p, x, gy - r * 0.55f, r, life, demonsOnly);
    }

    private static void combustibleVortex(World w, Fighter p, float x, float y, float r, float life, boolean demonsOnly) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        Effect v = new Effect(Effect.WHIRL, p, fr).at(x, y).radius(r).life(life)
                .damage(22, 120, 420).tickEvery(0.14f).colors(NEZUKO, NEZUKO_HI);
        v.ex2 = 6;
        v.pull = 0.45f;
        if (demonsOnly) v.demonsOnly();
        w.effects.add(v);
        Effect shock = new Effect(Effect.SLAM_RING, p, fr).at(x, y + r * 0.55f).radius(r * 1.22f).life(0.46f)
                .colors(NEZUKO, NEZUKO_HI);
        w.effects.add(shock);
        w.parts.ring(x, y + r * 0.55f, NEZUKO_HI);
        w.parts.burst(Particles.FIRE, x, y, 28, 360, 0.65f, 12, NEZUKO_HI, -180, 0.93f);
        w.cam.shake(11, 0.34f);
    }

    private static void setNezukoPose(Fighter p, int pose, float time) {
        if (p instanceof Player pl) pl.nezukoLegPose(pose, time);
    }

    private static void lockFacing(Fighter p, float time) {
        if (p instanceof Player pl) pl.lockFacing(time);
    }

    private static void nezukoHeelImpact(World w, Fighter p, float r, float dmg) {
        if (p.dead) return;
        float gy = w.groundYUnder(p.x, p.bottom());
        if (gy <= 9000) {
            p.y = gy - p.h / 2f;
            p.vy = 0;
            p.onGround = true;
        }
        setNezukoPose(p, 3, 0.24f);
        nezukoGroundShock(w, p, r, dmg);
        w.cam.shake(14, 0.42f);
    }

    private static void nezukoGroundShock(World w, Fighter p, float r, float dmg) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        float gy = p.bottom();
        Effect ring = new Effect(Effect.SLAM_RING, p, fr).at(p.x, gy).radius(r).life(0.48f)
                .damage(dmg, 360, 320).colors(NEZUKO, NEZUKO_HI);
        w.effects.add(ring);
        for (int dir = -1; dir <= 1; dir += 2) {
            Effect s = new Effect(Effect.SHOCK_GROUND, p, fr).at(p.x + dir * 28, gy - 10)
                    .vel(dir * 620, 0).radius(46).life(0.75f).damage(dmg * 0.55f, 280, 210).colors(NEZUKO, NEZUKO_HI);
            w.effects.add(s);
        }
        w.parts.burst(Particles.FIRE, p.x, gy - 8, 34, 420, 0.62f, 12, NEZUKO_HI, 120, 0.92f);
        w.cam.shake(8, 0.28f);
    }

    private static void arrowFlash(World w, float x, float y, float vx, float vy, boolean friendly, float life) {
        Effect ar = new Effect(Effect.ARROW, null, friendly).at(x, y).vel(vx, vy).radius(1).life(life)
                .colors(new Color(190, 24, 38), new Color(255, 120, 130));
        ar.angle = (float) Math.atan2(vy, vx);
        w.effects.add(ar);
    }

    private static void startFlip(Fighter p) {
        if (p instanceof Player pl) pl.startFlip();
    }

    private static void coldMountainSlam(World w, Fighter p) {
        if (p.dead) return;
        setCharge(p, 0);
        boolean fr = p.team == 0;
        int f = p.facing();
        p.startAttack(0.42f);
        p.swingSide *= -1;
        p.vx = f * 1260;
        p.vy = 880;
        w.cam.shake(5, 0.18f);
        for (int i = 0; i < 11; i++) {
            final int idx = i;
            w.schedule(idx * 0.045f, () -> {
                if (p.dead) return;
                p.vx = p.facing() * 1260;
                p.vy = 880;
                float xx = p.x + p.facing() * (22 + idx * 20);
                float yy = p.y - 18 + idx * 15;
                Effect arc = new Effect(Effect.ARC, p, fr).at(xx, yy)
                        .radius(108 + idx * 13).life(0.34f).damage(32.2f, 330, 320)
                        .pierce(1).colors(idx % 2 == 0 ? WIND : new Color(8, 12, 10), WIND_HI);
                arc.angle = (float) Math.toRadians(p.facingRight ? 44 : 136);
                arc.ex1 = (float) Math.toRadians(210);
                arc.ex2 = 20 + idx * 2.6f;
                w.effects.add(arc);
                w.meleeStrike(p, 106, 16.1f, 260, 300, Math.toRadians(p.facingRight ? 42 : 138), Math.toRadians(130), idx % 2 == 0 ? WIND : WIND_HI);
                w.parts.burst(Particles.CIRCLE, xx, yy, 7, 260, 0.34f, 7, idx % 2 == 0 ? WIND_HI : new Color(12, 18, 14), 90, 0.96f);
            });
        }
        w.schedule(0.5f, () -> {
            if (!p.dead) {
                p.vx *= 0.35f;
                w.cam.shake(8, 0.24f);
                w.parts.burst(Particles.CIRCLE, p.x, p.bottom(), 24, 380, 0.55f, 9, WIND_HI, 140, 0.93f);
            }
        });
    }

    private static void galeSuddenGusts(World w, Fighter p) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        int f = p.facing();
        p.startAttack(0.38f);
        p.swingSide *= -1;
        p.vx = f * 260;
        p.vy = Math.min(p.vy, -120);
        Effect torrent = new Effect(Effect.WHIRL, p, fr).at(p.x + f * 72, p.y - 46)
                .vel(f * 720, -20).radius(122).life(0.95f).damage(24, 360, 230).tickEvery(0.065f)
                .colors(new Color(6, 8, 8), new Color(218, 24, 38));
        torrent.ex1 = f;
        torrent.ex2 = 5;
        torrent.stopOnSolid = true;
        w.effects.add(torrent);
        for (int i = 0; i < 13; i++) {
            final int idx = i;
            w.schedule(idx * 0.045f, () -> {
                if (p.dead) return;
                float yy = p.y - 62 + FMath.sin(idx * 1.4f) * 42;
                Effect c = new Effect(Effect.CLAW_WAVE, p, fr).at(p.x + p.facing() * (48 + idx * 20), yy)
                        .vel(p.facing() * (760 + idx * 26), -120 + idx * 18).radius(62).life(0.55f)
                        .damage(32, 330, 190).pierce(1).colors(idx % 2 == 0 ? new Color(8, 10, 10) : new Color(174, 20, 34), WIND_HI);
                c.stopOnSolid = true;
                w.effects.add(c);
                w.meleeStrike(p, 114, 20, 270, 170,
                        Math.toRadians(p.facingRight ? -62 + idx * 12 : 242 - idx * 12),
                        Math.toRadians(120), idx % 2 == 0 ? new Color(12, 16, 14) : new Color(210, 26, 42));
                w.parts.burst(Particles.CIRCLE, p.x + p.facing() * 46, p.y - 32, 6, 260, 0.32f, 7,
                        idx % 2 == 0 ? new Color(8, 10, 10) : new Color(220, 34, 48), 100, 0.94f);
            });
        }
        w.cam.shake(7, 0.26f);
    }

    private static void flameUndulationRelease(World w, Fighter p) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        setCharge(p, 0);
        Effect spiral = new Effect(Effect.WHIRL, p, fr).followAt(0, 0)
                .radius(264).life(2f).damage(15, 110, 135).tickEvery(0.12f).colors(FLAME, FLAME_HI);
        spiral.pull = 0.25f;
        spiral.ex2 = 2;
        w.effects.add(spiral);
        p.invulnT = Math.max(p.invulnT, 2f);
        for (int i = 0; i < 40; i++) {
            final int idx = i;
            w.schedule(idx * 0.05f, () -> {
                if (p.dead) return;
                p.startAttack(0.14f);
                p.swingSide = idx % 2 == 0 ? -1 : 1;
                float arm = (idx % 2) * (float) Math.PI;
                float a = (2f - idx * 0.05f) * 10f + arm + idx * 0.18f;
                float rr = 50 + (idx % 12) * 18f;
                float sx = p.x + FMath.cos(a) * rr;
                float sy = p.y - 8 + FMath.sin(a) * rr * 0.68f;
                Color slash = idx % 3 == 0 ? FLAME_HI : idx % 3 == 1 ? FLAME : new Color(210, 42, 8);
                Effect arc = new Effect(Effect.ARC, p, fr).at(sx, sy).radius(50 + (idx % 4) * 11).life(0.22f)
                        .damage(12, 90, 120).pierce(1).colors(slash, FLAME_HI);
                arc.angle = a + (p.facingRight ? 0 : (float) Math.PI);
                arc.ex1 = (float) Math.toRadians(110);
                arc.ex2 = 18;
                w.effects.add(arc);
                w.parts.spawn(Particles.FIRE, sx, sy, FMath.cos(a) * 210, FMath.sin(a) * 120 - 90,
                        0.62f, 12, slash, -150, 0.93f);
                if (idx % 2 == 0)
                    w.parts.burst(Particles.EMBER, sx, sy, 7, 260, 0.36f, 8, FLAME_HI, -110, 0.94f);
            });
        }
        w.cam.shake(7, 0.34f);
    }

    private static void flameAerialBurst(World w, Fighter p, float x, float y, float r, float dmg, float kb, float up) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        Effect burst = new Effect(Effect.SLAM_RING, p, fr).at(x, y).radius(r).life(0.48f)
                .damage(dmg, kb, up).colors(FLAME, FLAME_HI);
        w.effects.add(burst);
        Effect shock = new Effect(Effect.NOVA, p, fr).at(x, y).radius(r * 1.35f).life(0.62f)
                .damage(dmg * 0.65f, kb * 1.25f, up * 0.72f).colors(FLAME, FLAME_HI);
        shock.hitOnce = false;
        shock.tickEvery(0.18f);
        w.effects.add(shock);
        w.parts.burst(Particles.FIRE, x, y, 34, 420, 0.7f, 13, FLAME_HI, -180, 0.92f);
        w.parts.burst(Particles.EMBER, x, y, 24, 360, 0.65f, 9, FLAME, -120, 0.93f);
        w.cam.shake(7, 0.25f);
    }

    private static void strikingTide(World w, Fighter p) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        Effect rib = new Effect(Effect.WATER_RIBBON, p, fr).life(1.35f).colors(WATER, WATER_HI);
        rib.x = p.x;
        rib.y = p.y - 6;
        w.effects.add(rib);
        final float cx = p.x;
        final float cy = p.y - 16;
        final int dir = p.facing();
        p.invulnT = Math.max(p.invulnT, 0.22f);
        for (int i = 0; i < 20; i++) {
            final int idx = i;
            w.schedule(idx * 0.055f, () -> {
                if (p.dead) return;
                float th = idx * 0.44f;
                float r = 3f * FMath.sin(2f * th);
                float tx = cx + dir * (72 + r * FMath.cos(th) * 102f + idx * 7f);
                float ty = cy + r * FMath.sin(th) * 90f;
                p.vx = FMath.clamp((tx - p.x) * 14f, -900, 900);
                p.vy = FMath.clamp((ty - p.y) * 14f, -640, 640);
                p.invulnT = Math.max(p.invulnT, 0.06f);
                double ang = Math.atan2(ty - p.y, tx - p.x);
                w.meleeStrike(p, 82 + (idx % 4) * 8, 28, 190, 105, ang, 2.15f, idx % 2 == 0 ? WATER : WATER_HI);
                if (idx % 2 == 0) startFlip(p);
                w.parts.burst(Particles.DROP, p.x + p.facing() * 34, p.y - 8, 8, 240, 0.28f, 5, WATER_HI, 320, 0.96f);
            });
        }
        w.schedule(1.16f, () -> {
            if (!p.dead) {
                p.vx = p.facing() * 520;
                p.vy = Math.min(p.vy, -120);
            }
        });
    }

    private static void waterWhirlpoolRelease(World w, Fighter p) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        setCharge(p, 0);
        Effect e = new Effect(Effect.WHIRL, p, fr).at(p.x, p.y - 90)
                .radius(205).life(1.225f).damage(13, 40, 72).tickEvery(0.15f).colors(WATER, WATER_HI);
        e.pull = 1.45f;
        e.ex2 = 1;
        w.effects.add(e);
        final float baseX = p.x, baseY = p.y;
        final int dir = p.facing();
        for (int i = 0; i <= 42; i++) {
            final int idx = i;
            w.schedule(i * (1.05f / 42f), () -> {
                if (p.dead) return;
                float q = 10f * (1f - idx / 42f);
                float nx = baseX + dir * q * 12f * FMath.sin(6f * q);
                float ny = baseY - q * 18f;
                p.vx = FMath.clamp((nx - p.x) * 24f, -960, 960);
                p.vy = FMath.clamp((ny - p.y) * 24f, -980, 980);
                p.x = nx;
                p.y = ny;
                p.onGround = false;
            });
        }
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            w.schedule(i * 0.135f, () -> {
                if (p.dead) return;
                startFlip(p);
                double ang = -Math.PI / 2 + idx * 1.05;
                w.meleeStrike(p, 112, 16, 110, 80, ang, 3.1f, WATER);
                w.parts.burst(Particles.DROP, p.x + FMath.rand(-55, 55), p.y - FMath.rand(10, 90), 10, 210, 0.42f, 5, WATER_HI, 260, 0.97f);
            });
        }
    }

    private static void setCharge(Fighter p, float t) {
        if (p instanceof Player pl) pl.chargeT = t;
    }

    private static void flameSlamImpact(World w, Fighter p) {
        if (p.dead) return;
        boolean fr = p.team == 0;
        Effect ring = new Effect(Effect.SLAM_RING, p, fr).at(p.x, p.bottom()).radius(240).life(0.5f).damage(115.2f, 430, 340).colors(FLAME, FLAME_HI);
        w.effects.add(ring);
        for (int dir = -1; dir <= 1; dir += 2) {
            Effect s = new Effect(Effect.SHOCK_GROUND, p, fr).at(p.x + dir * 30, p.bottom() - 10)
                    .vel(dir * 680, 0).radius(54).life(0.9f).damage(26.4f, 330, 230).colors(FLAME, FLAME_HI);
            w.effects.add(s);
        }
        w.parts.burst(Particles.EMBER, p.x, p.bottom(), 42, 460, 0.72f, 11, FLAME_HI, 140, 0.92f);
        w.cam.shake(11, 0.36f);
        p.noteAction();
    }

    static void slam(World w, Player p) {
        Effect ring = new Effect(Effect.SLAM_RING, p, true).at(p.x, p.bottom()).radius(240).life(0.5f).damage(115.2f, 430, 340).colors(FLAME, FLAME_HI);
        w.effects.add(ring);
        for (int dir = -1; dir <= 1; dir += 2) {
            Effect s = new Effect(Effect.SHOCK_GROUND, p, true).at(p.x + dir * 30, p.bottom() - 10)
                    .vel(dir * 680, 0).radius(54).life(0.9f).damage(26.4f, 330, 230).colors(FLAME, FLAME_HI);
            w.effects.add(s);
        }
        w.parts.burst(Particles.EMBER, p.x, p.bottom(), 42, 460, 0.72f, 11, FLAME_HI, 140, 0.92f);
        w.cam.shake(11, 0.36f);
        p.noteAction();
    }

    private static void unknowingFire(World w, Fighter p) {
        float f = p.facing();
        boolean fr = p.team == 0;
        p.noteAction();
        p.invulnT = Math.max(p.invulnT, 0.34f);
        p.vx = f * 1460;
        p.vy = Math.min(p.vy, -50);
        Effect rib = new Effect(Effect.FLAME_RIBBON, p, fr).life(0.9f).colors(FLAME, FLAME_HI);
        rib.x = p.x + f * 24;
        rib.y = p.y - 8;
        w.effects.add(rib);
        // sustained forward push lasting the full ribbon duration
        for (int i = 0; i < 15; i++) {
            w.schedule(i * 0.06f, () -> {
                if (p.dead) return;
                p.vx = p.facing() * 1460;
                p.invulnT = Math.max(p.invulnT, 0.1f);
            });
        }
        ringFire(w, p.x, p.y);
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            w.schedule(0.03f + i * 0.095f, () -> {
                if (p.dead) return;
                w.meleeStrike(p, 92, 38.64f, 300, 140,
                        Math.toRadians(p.facingRight ? -20 + idx * 4 : 200 - idx * 4), 2.2f, FLAME);
                ringFire(w, p.x, p.y);
                w.cam.shake(2, 0.08f);
            });
        }
    }

    private static void ringFire(World w, float x, float y) {
        w.parts.burst(Particles.EMBER, x, y + FMath.rand(-20, 6), 6, 170, 0.4f, 7, AbilityCast.FLAME_HI, -40, 0.94f);
    }

    private static void launchTiger(World w, Fighter p) {
        float f = p.facing();
        boolean fr = p.team == 0;
        p.noteAction();
        Effect th = new Effect(Effect.TIGER_HEAD, p, fr)
                .at(p.x + f * 50, p.y - 12)
                .vel(f * 540, 0)
                .radius(60).life(0.95f).damage(52.8f, 300, 160).pierce(2).colors(FLAME, FLAME_HI);
        th.stopOnSolid = true;
        w.effects.add(th);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            w.schedule(i * 0.095f, () -> {
                Effect t = new Effect(Effect.TIGER, p, fr).at(p.x + p.facing() * 26, p.y - 14 + idx * 8)
                        .vel(p.facing() * (550 + idx * 25), -40 + idx * 55)
                        .radius(32).life(0.85f).damage(42.9f, 250, 130).colors(FLAME, FLAME_HI);
                t.stopOnSolid = true;
                t.grav = -30;
                t.spin = FMath.rand(-3, 3);
                w.effects.add(t);
            });
        }
        w.parts.burst(Particles.EMBER, p.x + f * 30, p.y - 12, 14, 260, 0.5f, 8, FLAME_HI, 60, 0.93f);
        w.cam.shake(4, 0.15f);
    }
}
