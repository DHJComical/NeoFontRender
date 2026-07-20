package neofontrender.mixin;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
import neofontrender.client.render.sign.SignBatchRenderer;
import neofontrender.client.render.sign.SignOcclusionCuller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adapts the 1.7.10 tile-entity dispatch boundary to sign batching and occlusion checks. */
@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher {
    @Shadow public static double staticPlayerX;
    @Shadow public static double staticPlayerY;
    @Shadow public static double staticPlayerZ;

    @Inject(method = "renderTileEntity", at = @At("HEAD"), cancellable = true)
    private void nfr$beginAndCullSignBatch(TileEntity tileEntity, float partialTicks, CallbackInfo ci) {
        // 1.7.10 has no preDrawBatch/drawBatch pair. Keep a complete collection window around
        // this dispatcher call; the explicit hooks remain available to render-loop integrations.
        World renderWorld = tileEntity.getWorldObj();
        SignOcclusionCuller.beginFrame(renderWorld);
        SignBatchRenderer.begin();
        if (!(tileEntity instanceof TileEntitySign)) {
            return;
        }
        TileEntitySign sign = (TileEntitySign) tileEntity;
        if (tileEntity instanceof TileEntitySign && SignOcclusionCuller.shouldCull(
                sign, renderWorld, staticPlayerX, staticPlayerY, staticPlayerZ)) {
            SignBatchRenderer.flush(0);
            ci.cancel();
        }
    }

    @Inject(method = "renderTileEntity", at = @At("RETURN"))
    private void nfr$flushSignBatch(TileEntity tileEntity, float partialTicks, CallbackInfo ci) {
        SignBatchRenderer.flush(0);
    }
}
