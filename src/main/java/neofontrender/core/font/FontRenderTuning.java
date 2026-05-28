package neofontrender.core.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.AbstractTexture;
import neofontrender.core.config.NeofontrenderConfig;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * Runtime choices for font raster scale and texture filtering.
 *
 * <p>The configured oversample is treated as a quality ceiling. In GUI space a
 * glyph that is rasterized much larger than the current physical GUI scale has
 * to be downsampled by OpenGL, which softens text. Keeping the raster scale near
 * the current GUI scale lets baked anti-aliased pixels land close to 1:1 on the
 * framebuffer.</p>
 */
public final class FontRenderTuning {

    private static final float INTEGER_EPSILON = 0.03F;
    private static final float[] MODELVIEW = new float[16];
    private static final float[] PROJECTION = new float[16];
    private static volatile float currentGuiPixelScale = 0.0F;

    private FontRenderTuning() {
    }

    public static void updateFromCurrentGlState() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) {
                return;
            }

            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            buffer.get(MODELVIEW);
            buffer.clear();
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buffer);
            buffer.get(PROJECTION);

            float[] origin = projectToFramebuffer(0.0F, 0.0F, 0.0F, mc.displayWidth, mc.displayHeight);
            float[] xAxis = projectToFramebuffer(1.0F, 0.0F, 0.0F, mc.displayWidth, mc.displayHeight);
            float[] yAxis = projectToFramebuffer(0.0F, 1.0F, 0.0F, mc.displayWidth, mc.displayHeight);
            float scaleX = distance(origin, xAxis);
            float scaleY = distance(origin, yAxis);
            float scale = (scaleX + scaleY) * 0.5F;
            if (Float.isFinite(scale) && scale > 0.0F) {
                currentGuiPixelScale = clamp(scale, 0.25F, 64.0F);
            }
        } catch (RuntimeException | LinkageError ignored) {
            // Keep the last known scale; fallback is handled by currentGuiScale().
        }
    }

    public static float rasterScale(float configuredOversample) {
        float configured = clamp(configuredOversample, 1.0F, 16.0F);
        if (!NeofontrenderConfig.adaptiveRasterScale()) {
            return configured;
        }
        float target = Math.max(4.0F, currentGuiScale() * 2.0F);
        return clamp(Math.min(configured, target), 1.0F, configured);
    }

    public static void applyFontTextureFilter(AbstractTexture texture, float rasterScale) {
        boolean linear = useLinearFiltering(rasterScale);
        texture.setBlurMipmap(linear, linear && NeofontrenderConfig.renderingMipmap());
    }

    public static void applyBoundTextureFilter(float rasterScale) {
        boolean linear = useLinearFiltering(rasterScale);
        int min = linear
                ? (NeofontrenderConfig.renderingMipmap() ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR)
                : GL11.GL_NEAREST;
        int mag = linear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, min);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mag);
    }

    public static boolean useLinearFiltering(float rasterScale) {
        if (!NeofontrenderConfig.renderingInterpolation()) {
            return false;
        }
        if (!NeofontrenderConfig.adaptiveRasterScale()) {
            return true;
        }

        float screenScale = currentGuiScale();
        float ratio = screenScale / Math.max(1.0F, rasterScale);
        if (ratio >= 1.0F && isNearInteger(ratio)) {
            return false;
        }
        if (ratio > 0.0F && ratio < 1.0F && isNearInteger(1.0F / ratio)) {
            return false;
        }
        return true;
    }

    public static float currentGuiScale() {
        float measured = currentGuiPixelScale;
        if (measured > 0.0F) {
            return measured;
        }
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.displayWidth > 0 && mc.displayHeight > 0) {
                return Math.max(1, new ScaledResolution(mc).getScaleFactor());
            }
        } catch (RuntimeException ignored) {
            // Fall through to a conservative scale.
        }
        return 1.0F;
    }

    public static float alignToPixel(float value) {
        float scale = currentGuiScale();
        if (scale <= 0.0F) {
            return value;
        }
        return Math.round(value * scale) / scale;
    }

    private static float[] projectToFramebuffer(float x, float y, float z, int displayWidth, int displayHeight) {
        float[] eye = transform(MODELVIEW, x, y, z, 1.0F);
        float[] clip = transform(PROJECTION, eye[0], eye[1], eye[2], eye[3]);
        if (clip[3] == 0.0F) {
            return new float[] {0.0F, 0.0F};
        }
        float invW = 1.0F / clip[3];
        float ndcX = clip[0] * invW;
        float ndcY = clip[1] * invW;
        return new float[] {
                ndcX * displayWidth * 0.5F,
                ndcY * displayHeight * 0.5F
        };
    }

    private static float[] transform(float[] m, float x, float y, float z, float w) {
        return new float[] {
                m[0] * x + m[4] * y + m[8] * z + m[12] * w,
                m[1] * x + m[5] * y + m[9] * z + m[13] * w,
                m[2] * x + m[6] * y + m[10] * z + m[14] * w,
                m[3] * x + m[7] * y + m[11] * z + m[15] * w
        };
    }

    private static float distance(float[] a, float[] b) {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean isNearInteger(float value) {
        return Math.abs(value - Math.round(value)) <= INTEGER_EPSILON;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
