package de.ancientreminder.gui;

import de.ancientreminder.Calibration;
import de.ancientreminder.Config;
import de.ancientreminder.Hud;
import de.ancientreminder.NetherSession;
import de.ancientreminder.Target;
import de.ancientreminder.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

import static de.ancientreminder.gui.OptionScreen.action;
import static de.ancientreminder.gui.OptionScreen.cycle;
import static de.ancientreminder.gui.OptionScreen.names;
import static de.ancientreminder.gui.OptionScreen.toggle;

/** Alle Menü-Seiten. */
public final class Menus {
    private Menus() {}

    private static void open(Screen s) {
        Minecraft.getInstance().setScreen(s);
    }

    public static Screen main(Screen parent) {
        return new OptionScreen(parent, "Ancient Reminder – Menü", self -> List.of(
                action("Erz-Finder & Tracer", () -> open(ore(self))),
                action("Erz-Ziele", () -> open(targets(self))),
                action("HUD-Anzeigen", () -> open(huds(self))),
                action("HUD-Editor (verschieben)", () -> open(new HudEditorScreen(self))),
                action("Nether-Scanner", () -> open(nether(self))),
                action("Gang-Markierung", () -> open(tunnel(self))),
                action("Warnungen (Lava/Feuer)", () -> open(warnings(self))),
                action("Chunkgrenzen", () -> open(chunks(self))),
                action("Statistik & Spot-Log", () -> open(new StatsScreen(self)))
        ));
    }

    private static Screen ore(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "Erz-Finder & Tracer", self -> List.of(
                toggle("Erz-Finder", () -> c.oreEnabled, v -> c.oreEnabled = v),
                cycle("Radius", () -> c.oreRadius, v -> c.oreRadius = v, new int[]{16, 24, 32, 48, 64, 96}, " Blöcke"),
                cycle("Scan-Intervall", () -> c.oreScanTicks, v -> c.oreScanTicks = v, new int[]{10, 20, 40, 100}, " Ticks"),
                toggle("Chat-Nachricht", () -> c.oreMessage, v -> c.oreMessage = v),
                toggle("Sound", () -> c.oreSound, v -> c.oreSound = v),
                toggle("Drops am Boden", () -> c.oreItems, v -> c.oreItems = v),
                toggle("Tracer", () -> c.tracers, v -> c.tracers = v),
                cycle("Max. Tracer", () -> c.maxTracers, v -> c.maxTracers = v, new int[]{3, 5, 10, 20, 32, 64}, ""),
                cycle("Tracer-Dicke", () -> c.tracerWidth, v -> c.tracerWidth = v, new int[]{1, 2, 3, 4}, ""),
                names("Tracer-Start", () -> c.tracerStart, v -> c.tracerStart = v, new String[]{"Fadenkreuz", "Füße"}),
                cycle("Tracer-Deckkraft", () -> c.tracerAlpha, v -> c.tracerAlpha = v, new int[]{30, 50, 70, 100}, " %"),
                toggle("Block-Umrisse", () -> c.oreBoxes, v -> c.oreBoxes = v)
        ));
    }

    private static Screen targets(Screen parent) {
        return new OptionScreen(parent, "Erz-Ziele", self -> {
            List<OptionScreen.Opt> list = new ArrayList<>();
            for (Target t : Target.values()) {
                list.add(toggle(t.label, () -> t.cfg().enabled, v -> t.cfg().enabled = v));
                list.add(toggle("  Tracer", () -> t.cfg().tracer, v -> t.cfg().tracer = v));
            }
            return list;
        });
    }

    private static Screen huds(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "HUD-Anzeigen", self -> {
            List<OptionScreen.Opt> list = new ArrayList<>();
            list.add(toggle("Alle HUDs", () -> c.hudEnabled, v -> c.hudEnabled = v));
            list.add(toggle("Hintergrund", () -> c.hudBackground, v -> c.hudBackground = v));
            list.add(cycle("Hintergrund-Deckkraft", () -> c.hudBgAlpha, v -> c.hudBgAlpha = v, new int[]{0, 20, 40, 60}, " %"));
            list.add(cycle("Ø-Intervall", () -> c.statsIntervalMin, v -> c.statsIntervalMin = v, new int[]{5, 10, 15, 30, 60}, " Min"));
            for (int i = 0; i < Hud.IDS.length; i++) {
                final int idx = i;
                list.add(toggle(Hud.TITLES[i], () -> Hud.cfg(idx).enabled, v -> Hud.cfg(idx).enabled = v));
            }
            for (int i = 0; i < Hud.IDS.length; i++) {
                final int idx = i;
                list.add(names("Farbe " + Hud.TITLES[i], () -> Hud.cfg(idx).color, v -> Hud.cfg(idx).color = v, Ui.COLOR_NAMES));
            }
            return list;
        });
    }

    private static Screen nether(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "Nether-Scanner", self -> List.of(
                toggle("Nether-Scanner", () -> c.netherEnabled, v -> c.netherEnabled = v),
                cycle("Ziel-Höhe Y", () -> c.targetY, v -> c.targetY = v, new int[]{13, 14, 15, 16, 17}, ""),
                cycle("Scan-Radius", () -> c.scanChunks, v -> c.scanChunks = v, new int[]{2, 3, 4, 5, 6, 8}, " Chunks"),
                cycle("Neu-Scan alle", () -> c.rescanSeconds, v -> c.rescanSeconds = v, new int[]{30, 60, 90, 120}, " s"),
                toggle("Empfehlung anzeigen", () -> c.recommend, v -> c.recommend = v),
                toggle("Chat-Meldung", () -> c.spotMessage, v -> c.spotMessage = v),
                toggle("Sound", () -> c.spotSound, v -> c.spotSound = v),
                toggle("RTP-Sprung erkennen", () -> c.autoDetectJump, v -> c.autoDetectJump = v),
                cycle("'Sehr gut' bis", () -> c.lvlVeryGood, v -> c.lvlVeryGood = v, new int[]{5, 10, 15}, " %"),
                cycle("'Gut' bis", () -> c.lvlGood, v -> c.lvlGood = v, new int[]{15, 25, 35}, " %"),
                cycle("'Ok' bis", () -> c.lvlOk, v -> c.lvlOk = v, new int[]{35, 50, 60}, " %"),
                cycle("'Schlecht' bis", () -> c.lvlBad, v -> c.lvlBad = v, new int[]{60, 75, 85}, " %"),
                action("Kalibrierung zurücksetzen (" + Calibration.samples() + "/" + Config.CALIB_NEEDED + ")", () -> {
                    Calibration.reset();
                    Ui.chat("Kalibrierung zurückgesetzt – 10x /rtp und auf Y" + c.targetY + " gehen.");
                })
        ));
    }

    private static Screen tunnel(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "Gang-Markierung", self -> List.of(
                toggle("Gang-Markierung", () -> c.tunnelEnabled, v -> c.tunnelEnabled = v),
                cycle("Chance bei 'Sehr gut'", () -> c.tunnelChance, v -> c.tunnelChance = v, new int[]{10, 25, 50, 75, 100}, " %"),
                cycle("Cooldown", () -> c.tunnelCooldownMin, v -> c.tunnelCooldownMin = v, new int[]{1, 5, 10, 20, 30}, " Min"),
                cycle("Gang-Länge", () -> c.tunnelLength, v -> c.tunnelLength = v, new int[]{6, 8, 10, 12}, " Blöcke"),
                cycle("Lava-Sicherheitsabstand", () -> c.tunnelLavaSafety, v -> c.tunnelLavaSafety = v, new int[]{3, 4, 5, 6}, " Blöcke"),
                cycle("Max. Entfernung", () -> c.tunnelMaxDist, v -> c.tunnelMaxDist = v, new int[]{16, 24, 32}, " Blöcke"),
                action("Aktuelle Markierung löschen", NetherSession::clearTunnel)
        ));
    }

    private static Screen warnings(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "Warnungen", self -> List.of(
                toggle("Lava-Warnung", () -> c.lavaWarn, v -> c.lavaWarn = v),
                cycle("Lava voraus ab", () -> c.lavaAhead, v -> c.lavaAhead = v, new int[]{2, 3, 4, 5, 6}, " Blöcken"),
                toggle("Lava direkt daneben", () -> c.lavaNearby, v -> c.lavaNearby = v),
                toggle("Warn-Sound", () -> c.lavaSound, v -> c.lavaSound = v),
                toggle("Feuerresistenz-Warnung", () -> c.fireWarn, v -> c.fireWarn = v),
                cycle("Warnen bei", () -> c.fireWarnSeconds, v -> c.fireWarnSeconds = v, new int[]{15, 30, 60}, " s")
        ));
    }

    private static Screen chunks(Screen parent) {
        Config c = Config.I;
        return new OptionScreen(parent, "Chunkgrenzen", self -> List.of(
                toggle("Chunkgrenzen", () -> c.chunkEnabled, v -> c.chunkEnabled = v),
                cycle("Reichweite", () -> c.chunkRange, v -> c.chunkRange = v, new int[]{1, 2, 3, 4, 5}, " Chunks"),
                names("Farbe", () -> c.chunkColor, v -> c.chunkColor = v, Ui.COLOR_NAMES),
                cycle("Deckkraft", () -> c.chunkAlpha, v -> c.chunkAlpha = v, new int[]{10, 15, 25, 35, 50}, " %"),
                cycle("Dicke", () -> c.chunkWidth, v -> c.chunkWidth = v, new int[]{1, 2, 3}, ""),
                cycle("Höhe (+/-)", () -> c.chunkVertical, v -> c.chunkVertical = v, new int[]{16, 32, 64, 128}, " Blöcke"),
                toggle("Sektionslinien (alle 16 Y)", () -> c.chunkSections, v -> c.chunkSections = v),
                toggle("Mit Entfernung ausblenden", () -> c.chunkFade, v -> c.chunkFade = v),
                toggle("Durch Blöcke sichtbar", () -> c.chunkThroughWalls, v -> c.chunkThroughWalls = v),
                toggle("Aktuellen Chunk hervorheben", () -> c.chunkHighlight, v -> c.chunkHighlight = v),
                toggle("Chunk-Info HUD", () -> Hud.cfg(5).enabled, v -> Hud.cfg(5).enabled = v)
        ));
    }
}
