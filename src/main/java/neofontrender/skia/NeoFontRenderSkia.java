package neofontrender.skia;

import net.minecraftforge.fml.common.Mod;
import neofontrender.Tags;

/**
 * Empty Forge container for the optional Skija runtime JAR.
 *
 * <p>Forge 1.12 does not reliably retain arbitrary library JARs from the mods directory on every
 * launcher. Declaring the runtime as a client-only mod container makes its Skija classes and native
 * resources available before Neo Font Render selects the optional Skia backend.</p>
 */
@Mod(
        modid = NeoFontRenderSkia.MOD_ID,
        name = "Neo Font Render Skia Runtime",
        version = Tags.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.12,1.13)"
)
public final class NeoFontRenderSkia {
    public static final String MOD_ID = "neofontrender_skia";

    public NeoFontRenderSkia() {
    }
}
