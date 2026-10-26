package de.ancientreminder.gui;

import de.ancientreminder.Calibration;
import de.ancientreminder.Config;
import de.ancientreminder.Data;
import de.ancientreminder.RatingLevel;
import de.ancientreminder.Stats;
import de.ancientreminder.Ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Statistik: RTPs, Ancient pro Spot-Level und die letzten Spots. */
public class StatsScreen extends Screen {
    private final Screen parent;

    public StatsScreen(Screen parent) {
        super(Component.literal("Statistik & Spot-Log"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int by = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Spot-Log leeren"), b -> {
            Data.I.clearSpots();
            Ui.chat("Spot-Log geleert.");
        }).bounds(this.width / 2 - 160, by, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> this.onClose())
                .bounds(this.width / 2 + 10, by, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        int x = this.width / 2 - 150;
        int y = 14;
        g.drawCenteredString(this.font, "Statistik & Spot-Log", this.width / 2, y, 0xFFFFFFFF);
        y += 20;

        Data d = Data.I;
        y = line(g, x, y, "Ancient gesamt: " + d.totalAncient + "   |   Session: " + Stats.sessionAncient, 0xFFFFFF);
        y = line(g, x, y, "RTPs gesamt: " + d.totalRtp + "   |   Session: " + Stats.rtpSession
                + " (" + Ui.d1(Stats.rtpPerHour()) + "/h)", 0xFFFFFF);
        y = line(g, x, y, "Ø Ancient pro Spot: " + Ui.d1(d.avgAncientPerSpot()) + "   (" + d.spots.size() + " Spots im Log)", 0xFFFFFF);
        y = line(g, x, y, "Kalibrierung: " + Calibration.samples() + "/" + Config.CALIB_NEEDED
                + (Calibration.done() ? " (fertig)" : " (läuft)"), Calibration.done() ? 0x55FF55 : 0xFFAA00);
        y += 6;
        y = line(g, x, y, "Ancient pro Spot nach Bewertung:", 0xFFFF55);
        for (Map.Entry<RatingLevel, Data.LevelStat> e : d.levelStats().entrySet()) {
            Data.LevelStat st = e.getValue();
            String text = e.getKey().label + ": " + (st.n == 0 ? "–"
                    : "Ø " + Ui.d1(st.avgAncient) + " Ancient in Ø " + Ui.d1(st.avgMinutes) + " Min (" + st.n + " Spots)");
            y = line(g, x + 8, y, text, e.getKey().color);
        }
        y += 6;
        y = line(g, x, y, "Letzte Spots:", 0xFFFF55);
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM. HH:mm");
        List<Data.Spot> spots = d.spots;
        int shown = 0;
        for (int i = spots.size() - 1; i >= 0 && shown < 8; i--, shown++) {
            Data.Spot s = spots.get(i);
            RatingLevel lvl = RatingLevel.valueOf(s.level);
            String text = fmt.format(new Date(s.time)) + "  " + lvl.label + " (" + Math.round(s.pct) + " %)  "
                    + s.ancient + " Ancient / " + Ui.time(s.seconds);
            y = line(g, x + 8, y, text, lvl.color);
        }
    }

    private int line(GuiGraphics g, int x, int y, String text, int rgb) {
        g.drawString(this.font, text, x, y, 0xFF000000 | rgb, true);
        return y + 11;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
