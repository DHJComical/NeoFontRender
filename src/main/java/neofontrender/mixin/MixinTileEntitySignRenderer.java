package neofontrender.mixin;

import neofontrender.core.config.NeofontrenderConfig;
import neofontrender.core.font.support.FontRenderTuning;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntitySignRenderer.class)
public abstract class MixinTileEntitySignRenderer {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"
            )
    )
    private int sfr$drawVisibleSignText(FontRenderer renderer, String text, int x, int y, int color) {
        if (NeofontrenderConfig.signTextLodCulling()) {
            FontRenderTuning.updateFromCurrentGlState(false);
            int width = renderer.getStringWidth(text);
            // Vanilla culls the tile entity by distance/frustum but still submits every text line.
            // Skip only lines whose projected quad is entirely off-screen or below the configured
            // physical-pixel threshold; the wooden sign model remains on the normal renderer path.
            if (!FontRenderTuning.isCurrentTextQuadVisible(
                    x, y, width, renderer.FONT_HEIGHT, NeofontrenderConfig.signTextMinPixelHeight())) {
                return x + width;
            }
        }
        return renderer.drawString(text, x, y, color);
    }
}
