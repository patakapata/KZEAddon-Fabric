package com.theboss.kzeaddonfabric.wip;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.WidgetDispatcher;
import com.theboss.kzeaddonfabric.WidgetRegister;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.AbstractWidget;
import net.minecraft.text.Text;

public class DebugWidget implements WidgetRegister {
    @Override
    public void register(WidgetDispatcher dispatcher) {
        dispatcher.register(new TotalKillWidget(0, 0, 1.0F, Anchor.MIDDLE_MIDDLE, Anchor.LEFT_UP));
    }

    public static class TotalKillWidget extends AbstractWidget {
        public TotalKillWidget(float x, float y, float scale, Anchor windowAnchor, Anchor elementAnchor) {
            super(x, y, scale, windowAnchor, elementAnchor);
        }

        @Override
        public int getColor() {
            return 0xFFFFFF;
        }

        @Override
        public short getAlpha() {
            return 0xFF;
        }

        @Override
        public Text getText() {
            return Text.of(KZEAddon.stats.getTotalKillCount() + " kills");
        }
    }
}
