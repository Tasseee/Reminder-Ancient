package de.ancientreminder.gui;

import de.ancientreminder.Config;
import de.ancientreminder.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Hier verschiebst du die HUD-Anzeigen per Maus (linke Taste halten und ziehen). */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private int drag = -1;
    private int offX;
    private int offY;
    private boolean wasDown;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD-Editor"));
        this.parent = parent;
    }

    private Component label(int i) {
        return Component.literal(Hud.TITLES[i] + ": " + (Hud.cfg(i).enabled ? "§aAN" : "§cAUS"));
    }

    @Override
    protected void init() {
        int n = Hud.IDS.length;
        int w = Math.min(130, (this.width - 16) / 3 - 4);
        for (int i = 0; i < n; i++) {
            final int idx = i;
            int x = 8 + (i % 3) * (w + 4);
            int y = this.height - 76 + (i / 3) * 22;
            this.addRenderableWidget(Button.builder(label(i), b -> {
                Hud.cfg(idx).enabled = !Hud.cfg(idx).enabled;
                b.setMessage(label(idx));
                Config.save();
            }).bounds(x, y, w, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Positionen zurücksetzen"), b -> Hud.resetPositions())
                .bounds(this.width - 160, this.height - 76, 152, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
                .bounds(this.width - 160, this.height - 54, 152, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        Minecraft mc = Minecraft.getInstance();
        boolean down = mc.mouseHandler.isLeftPressed();

        int count = Hud.IDS.length;
        int[][] rects = new int[count][4];
        for (int i = 0; i < count; i++) {
            List<Hud.Line> lines = Hud.lines(i, true);
            int[] sz = Hud.size(this.font, lines);
            rects[i] = new int[]{Hud.x(i), Hud.y(i), sz[0], sz[1]};
        }

        if (down && !wasDown) {
            for (int i = count - 1; i >= 0; i--) {
                int[] r = rects[i];
                if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                    drag = i;
                    offX = mouseX - r[0];
                    offY = mouseY - r[1];
                    break;
                }
            }
        }
        if (!down && drag >= 0) {
            drag = -1;
            Config.save();
        }
        if (drag >= 0) {
            int[] r = rects[drag];
            Hud.cfg(drag).x = Math.max(0, Math.min(this.width - r[2], mouseX - offX));
            Hud.cfg(drag).y = Math.max(0, Math.min(this.height - r[3], mouseY - offY));
            rects[drag][0] = Hud.cfg(drag).x;
            rects[drag][1] = Hud.cfg(drag).y;
        }
        wasDown = down;

        for (int i = 0; i < count; i++) {
            float alpha = Hud.cfg(i).enabled ? 1f : 0.4f;
            Hud.draw(g, this.font, rects[i][0], rects[i][1], Hud.lines(i, true), alpha);
        }
        g.drawCenteredString(this.font, "HUD-Editor: Anzeigen mit gedrückter linker Maustaste verschieben", this.width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Config.save();
        this.minecraft.setScreen(parent);
    }
}
