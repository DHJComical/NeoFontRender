package neofontrender.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

/**
 * Applies optional sign optimizations only when at least one sign option is enabled at launch.
 * Runtime checks remain in the mixin for config reloads, but this gate preserves the original
 * target bytecode entirely when both options are disabled.
 */
public final class NeoFontRenderMixinPlugin implements IMixinConfigPlugin {
    private boolean signMixinEnabled = true;

    @Override
    public void onLoad(String mixinPackage) {
        signMixinEnabled = readSignOptions();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("MixinTileEntitySignRenderer")) {
            return signMixinEnabled;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean readSignOptions() {
        File config = new File(gameDirectory(), "config/neofontrender.toml");
        if (!config.isFile()) {
            // The generated defaults enable both optimizations.
            return true;
        }
        boolean inPerformance = false;
        boolean lod = true;
        boolean batching = true;
        boolean frustum = true;
        boolean modelLod = true;
        try {
            List<String> lines = Files.readAllLines(config.toPath(), StandardCharsets.UTF_8);
            for (String raw : lines) {
                String line = raw.split("#", 2)[0].trim();
                if (line.startsWith("[")) {
                    inPerformance = "[performance]".equals(line);
                    continue;
                }
                if (!inPerformance) {
                    continue;
                }
                if (line.startsWith("signTextLodCulling")) {
                    lod = parseBoolean(line, lod);
                } else if (line.startsWith("signTextBatching")) {
                    batching = parseBoolean(line, batching);
                } else if (line.startsWith("signTextFrustumCulling")) {
                    frustum = parseBoolean(line, frustum);
                } else if (line.startsWith("signModelLod")) {
                    modelLod = parseBoolean(line, modelLod);
                }
            }
        } catch (Throwable ignored) {
            // Keep the default-on behavior if the file cannot be read during early Mixin loading.
            return true;
        }
        return lod || batching || frustum || modelLod;
    }

    private static boolean parseBoolean(String line, boolean fallback) {
        int equals = line.indexOf('=');
        if (equals < 0) {
            return fallback;
        }
        String value = line.substring(equals + 1).trim();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    private static File gameDirectory() {
        try {
            Class<?> launch = Class.forName("net.minecraft.launchwrapper.Launch");
            Object home = launch.getField("minecraftHome").get(null);
            if (home instanceof File) {
                return (File) home;
            }
        } catch (Throwable ignored) {
        }
        return new File(System.getProperty("user.dir", "."));
    }
}
