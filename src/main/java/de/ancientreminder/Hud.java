package de.ancientreminder;

import de.ancientreminder.gui.HudEditorScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Alle HUD-Anzeigen. Position, Farbe und Sichtbarkeit sind im Menü/HUD-Editor einstellbar. */
public final class Hud {
    public record Line(String text, int rgb) {}

    public static final String[] IDS = {"ancient", "rtp", "height", "rating", "fire", "chunk"};
    public static final String[] TITLES = {"Ancient-Statistik", "RTP-Statistik", "Höhen-Anzeige", "Spot-Bewertung", "Feuerresistenz", "Chunk-Info"};
    private static final int[] DEF_X = {6, 6, 6, 6, 6, 6};
    private static final int[] DEF_Y = {6, 52, 100, 128, 192, 214};

    private Hud() {}

    public static Config.WidgetCfg cfg(int i) {
        return Config.I.widgets.get(IDS[i]);
    }

    public static int x(int i) {
        int v = cfg(i).x;
        return v < 0 ? DEF_X[i] : v;
    }

    public static int y(int i) {
        int v = cfg(i).y;
        return v < 0 ? DEF_Y[i] : v;
    }

    public static void resetPositions() {
        for (int i = 0; i < IDS.length; i++) {
            cfg(i).x = -1;
            cfg(i).y = -1;
        }
        Config.save();
    }

    public static boolean visible(int i) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        Config c = Config.I;
        return switch (i) {
            case 1 -> c.netherEnabled;
            case 2 -> NetherSession.phase == NetherSession.Phase.DESCENDING;
            case 3 -> c.netherEnabled && NetherSession.phase != NetherSession.Phase.IDLE;
            case 4 -> Warnings.fireSeconds() >= 0;
            default -> true;
        };
    }

    public static List<Line> lines(int i, boolean preview) {
        Config c = Config.I;
        Minecraft mc = Minecraft.getInstance();
        int col = Ui.color(cfg(i).color);
        List<Line> out = new ArrayList<>();
        switch (i) {
            case 0 -> {
                int iv = c.statsIntervalMin;
                out.add(new Line("Ancient (Session): " + Stats.sessionAncient, col));
                out.add(new Line("Ø pro " + iv + " Min: " + Ui.d1(Stats.avgPerInterval()), col));
                out.add(new Line("Letzte " + iv + " Min: " + Stats.lastInterval(), col));
                out.add(new Line("Gesamt: " + Data.I.totalAncient, col));
                out.add(new Line("Zeit: " + Ui.time(Stats.sessionSeconds()), col));
            }
            case 1 -> {
                out.add(new Line("RTPs: " + Stats.rtpSession + " (" + Ui.d1(Stats.rtpPerHour()) + "/h)", col));
                out.add(new Line("Ø Ancient/Spot: " + Ui.d1(Data.I.avgAncientPerSpot()), col));
                if (NetherSession.phase != NetherSession.Phase.IDLE || preview) {
                    out.add(new Line("Dieser Spot: " + NetherSession.ancientThisSpot() + " Ancient, "
                            + Ui.time(NetherSession.sessionSeconds()), col));
                }
                RatingLevel best = Data.I.bestLevel();
                if (best != null) {
                    Data.LevelStat st = Data.I.levelStats().get(best);
                    out.add(new Line("Bestes Level: " + best.label + " (Ø " + Ui.d1(st.avgAncient) + ")", best.color));
                }
            }
            case 2 -> {
                int y = mc.player != null ? mc.player.getBlockY() : 68;
                int left = Math.max(0, y - c.targetY);
                out.add(new Line("Höhe: Y " + y, col));
                out.add(new Line("Ziel Y " + c.targetY + ": noch " + left + " Blöcke", left <= 10 ? 0x55FF55 : col));
            }
            case 3 -> ratingLines(out, col, preview);
            case 4 -> {
                int sec = preview ? 150 : Warnings.fireSeconds();
                int rgb = sec <= c.fireWarnSeconds ? 0xFF5555 : col;
                out.add(new Line("Feuerresistenz: " + Ui.time(Math.max(0, sec)), rgb));
            }
            case 5 -> {
                BlockPos pos = mc.player != null ? mc.player.blockPosition() : BlockPos.ZERO;
                out.add(new Line("Chunk: " + (pos.getX() >> 4) + " / " + (pos.getZ() >> 4), col));
                out.add(new Line("Im Chunk: " + (pos.getX() & 15) + " " + pos.getY() + " " + (pos.getZ() & 15), col));
            }
            default -> {
            }
        }
        return out;
    }

    private static void ratingLines(List<Line> out, int col, boolean preview) {
        Config c = Config.I;
        NetherSession.Rating r = NetherSession.last;
        if (r == null && preview) {
            out.add(new Line("Spot: " + RatingLevel.VERY_GOOD.label.toUpperCase(), RatingLevel.VERY_GOOD.color));
            out.add(new Line("Abgebaut: 4 %", col));
            out.add(new Line("BLEIBEN – guter Spot", col));
            out.add(new Line("Neu-Scan in 0:42", col));
            return;
        }
        if (r == null) {
            out.add(new Line("Spot: warte auf Scan ...", col));
        } else {
            out.add(new Line("Spot: " + r.level().label.toUpperCase(), r.level().color));
            out.add(new Line("Abgebaut: " + Math.round(r.pct()) + " %", col));
            if (c.recommend) out.add(new Line(NetherSession.recommendation, col));
        }
        if (NetherSession.SCANNER.isRunning()) {
            out.add(new Line("Scanne ... " + Math.round(NetherSession.SCANNER.progress() * 100) + " %", col));
        } else if (r != null) {
            out.add(new Line("Neu-Scan in " + Ui.time(NetherSession.secondsToNextScan()), col));
        }
        if (!Calibration.done()) {
            out.add(new Line("Kalibrierung: " + Calibration.samples() + "/" + Config.CALIB_NEEDED + " (per /rtp sammeln)", 0xFFAA00));
        }
        if (NetherSession.tunnel != null && Minecraft.getInstance().player != null) {
            out.add(new Line("Gang: " + NetherSession.tunnelInfo(Minecraft.getInstance().player.position()), 0x55FFFF));
        }
    }

    // ------------------------------------------------------------------ Zeichnen

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.I.hudEnabled || mc.player == null) return;
        if (mc.screen instanceof HudEditorScreen) return;
        Font font = mc.font;
        for (int i = 0; i < IDS.length; i++) {
            if (!cfg(i).enabled || !visible(i)) continue;
            draw(g, font, x(i), y(i), lines(i, false), 1f);
        }
    }

    public static int[] size(Font font, List<Line> lines) {
        int w = 0;
        for (Line l : lines) w = Math.max(w, font.width(l.text()));
        return new int[]{w + 8, lines.size() * (font.lineHeight + 1) + 5};
    }

    public static void draw(GuiGraphics g, Font font, int x, int y, List<Line> lines, float alpha) {
        if (lines.isEmpty()) return;
        int[] sz = size(font, lines);
        if (Config.I.hudBackground) {
            int a = Math.round(Config.I.hudBgAlpha / 100f * 255f * alpha);
            g.fill(x, y, x + sz[0], y + sz[1], a << 24);
        }
        int ty = y + 3;
        int aText = Math.round(255f * alpha);
        for (Line l : lines) {
            g.drawString(font, l.text(), x + 4, ty, (aText << 24) | l.rgb(), true);
            ty += font.lineHeight + 1;
        }
    }

    /** Hilfsfunktion für Statistik-Seiten. */
    public static Map<RatingLevel, Data.LevelStat> levelStats() {
        return Data.I.levelStats();
    }
}
