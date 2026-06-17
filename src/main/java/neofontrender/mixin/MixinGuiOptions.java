package neofontrender.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import neofontrender.client.gui.NeofontrenderConfigScreen;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Injects a "Neo Font Render" button into the vanilla Options screen ({@link GuiOptions}).
 * <p>
 * The button appears as an extra row at the bottom of the options list and opens
 * {@link NeofontrenderConfigScreen} when clicked.
 */
@SideOnly(Side.CLIENT)
@Mixin(GuiOptions.class)
public abstract class MixinGuiOptions {

    @Shadow
    private GuiSlot list;

    @Shadow
    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

    @Inject(method = "initGui", at = @At("RETURN"))
    private void neofontrender$addFontConfigButton(CallbackInfo ci) {
        try {
            Field listEntriesField = GuiSlot.class.getDeclaredField("listEntries");
            listEntriesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<IGuiListEntry> entries = (List<IGuiListEntry>) listEntriesField.get(this.list);
            entries.add(new NeoFontRenderButtonEntry());
        } catch (Exception e) {
            // Field name might differ across mappings; fail silently
        }
    }

    @SideOnly(Side.CLIENT)
    private static final class NeoFontRenderButtonEntry implements IGuiListEntry {

        private static final int BUTTON_ID = 9200;
        private GuiButton button;

        @Override
        public GuiButton getButton() {
            return this.button;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
                              int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            this.button.x = x;
            this.button.y = y;
            this.button.width = listWidth;
            this.button.height = slotHeight - 2;
            this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);

            Minecraft.getMinecraft().fontRenderer.drawString(
                    I18n.format("neofontrender.options.button"),
                    x + listWidth / 2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(
                            I18n.format("neofontrender.options.button")) / 2,
                    y + (slotHeight - 8) / 2,
                    0xFFFFFF
            );
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY,
                                    int mouseEvent, int relativeX, int relativeY) {
            if (this.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                NeofontrenderConfigScreen.open();
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.button.mouseReleased(x, y);
        }

        @Override
        public void setSelected(int slotIndex, int selectedIndex) {
        }

        NeoFontRenderButtonEntry() {
            this.button = new GuiButton(BUTTON_ID, 0, 0, 0, 0,
                    I18n.format("neofontrender.options.button"));
        }
    }
}
