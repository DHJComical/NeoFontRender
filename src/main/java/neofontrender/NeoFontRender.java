package neofontrender;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neofontrender.common.CommonProxy;

@Mod(
        modid = NeoFontRender.MOD_ID,
        name = NeoFontRender.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "required-after:modularui2",
        acceptedMinecraftVersions = "[1.7.10]",
        acceptableRemoteVersions = "*"
)
public class NeoFontRender {

    public static final String MOD_ID = "neofontrender";
    public static final String MOD_NAME = "Neo Font Render";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static NeoFontRender instance;

    @SidedProxy(
            clientSide = "neofontrender.client.ClientProxy",
            serverSide = "neofontrender.common.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
