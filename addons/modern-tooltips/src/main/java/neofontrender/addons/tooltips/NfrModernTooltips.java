package neofontrender.addons.tooltips;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neofontrender.api.client.settings.NfrSettingsPageRegistry;

@Mod(
        modid = NfrModernTooltips.MOD_ID,
        name = "NFR Modern Tooltips",
        version = NfrModernTooltips.VERSION,
        dependencies = "required-after:neofontrender@[0.3.4,)",
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.12,1.13)"
)
public final class NfrModernTooltips {
    public static final String MOD_ID = "neofontrender_modern_tooltips";
    public static final String VERSION = "0.1.0";
    public static final Logger LOGGER = LogManager.getLogger("NFR Modern Tooltips");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TooltipConfig.load();
        Arc3DRuntimeSupport.verify();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NfrSettingsPageRegistry.register(new ModernTooltipSettingsPage());
        MinecraftForge.EVENT_BUS.register(new ModernTooltipHandler());
    }
}
