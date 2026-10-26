package de.ancientreminder.gui;

import de.ancientreminder.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Allgemeiner Einstellungs-Bildschirm mit zwei Spalten und Seiten. */
public class OptionScreen extends Screen {
    public interface Opt {
        Component label();

        void click();
    }

    private static final int PER_PAGE = 12;

    private final Screen parent;
    private final Function<OptionScreen, List<Opt>> builder;
    private final String heading;
    private int page;

    public OptionScreen(Screen parent, String heading, Function<OptionScreen, List<Opt>> builder) {
        super(Component.literal(heading));
        this.parent = parent;
        this.heading = heading;
        this.builder = builder;
    }

    @Override
    protected void init() {
        List<Opt> opts = builder.apply(this);
        int pages = Math.max(1, (opts.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));
        int cw = 190;
        int gap = 6;
        int left = this.width / 2 - cw - gap / 2;
        int right = this.width / 2 + gap / 2;
        int start = page * PER_PAGE;
        int end = Math.min(opts.size(), start + PER_PAGE);
        for (int i = start; i < end; i++) {
            int k = i - start;
            int x = (k % 2 == 0) ? left : right;
            int y = 36 + (k / 2) * 24;
            Opt o = opts.get(i);
            this.addRenderableWidget(Button.builder(o.label(), btn -> {
                o.click();
                btn.setMessage(o.label());
            }).bounds(x, y, cw, 20).build());
        }
        int by = this.height - 28;
        if (pages > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("< Seite"), b -> {
                page = (page + pages - 1) % pages;
                this.rebuildWidgets();
            }).bounds(left, by, 90, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Seite >"), b -> {
                page = (page + 1) % pages;
                this.rebuildWidgets();
            }).bounds(right + cw - 90, by, 90, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> this.onClose())
                .bounds(this.width / 2 - 50, by, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, this.heading, this.width / 2, 14, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Config.save();
        this.minecraft.setScreen(parent);
    }

    // ------------------------------------------------------------------ Bausteine

    public static Opt toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        return new Opt() {
            @Override
            public Component label() {
                return Component.literal(name + ": " + (get.getAsBoolean() ? "§aAN" : "§cAUS"));
            }

            @Override
            public void click() {
                set.accept(!get.getAsBoolean());
                Config.save();
            }
        };
    }

    public static Opt cycle(String name, IntSupplier get, IntConsumer set, int[] values, String suffix) {
        return new Opt() {
            @Override
            public Component label() {
                return Component.literal(name + ": §e" + get.getAsInt() + suffix);
            }

            @Override
            public void click() {
                int cur = get.getAsInt();
                int idx = 0;
                int bestDiff = Integer.MAX_VALUE;
                for (int i = 0; i < values.length; i++) {
                    int d = Math.abs(values[i] - cur);
                    if (d < bestDiff) {
                        bestDiff = d;
                        idx = i;
                    }
                }
                set.accept(values[(idx + 1) % values.length]);
                Config.save();
            }
        };
    }

    public static Opt names(String name, IntSupplier get, IntConsumer set, String[] names) {
        return new Opt() {
            @Override
            public Component label() {
                return Component.literal(name + ": §e" + names[Math.floorMod(get.getAsInt(), names.length)]);
            }

            @Override
            public void click() {
                set.accept((get.getAsInt() + 1) % names.length);
                Config.save();
            }
        };
    }

    public static Opt action(String label, Runnable run) {
        return new Opt() {
            @Override
            public Component label() {
                return Component.literal(label);
            }

            @Override
            public void click() {
                run.run();
            }
        };
    }
}
