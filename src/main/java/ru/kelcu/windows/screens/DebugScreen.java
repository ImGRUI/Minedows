package ru.kelcu.windows.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;

public class DebugScreen extends Screen {
    protected DebugScreen() {
        super(Component.empty());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture skyTexture = textureManager.getTexture(AbstractEndPortalRenderer.END_SKY_LOCATION);
        AbstractTexture portalTexture = textureManager.getTexture(AbstractEndPortalRenderer.END_PORTAL_LOCATION);
        TextureSetup textureSetup = TextureSetup.doubleTexture(skyTexture.getTextureView(), skyTexture.getSampler(), portalTexture.getTextureView(), portalTexture.getSampler());
        guiGraphics.fill(RenderPipelines.END_PORTAL, textureSetup, 0, 0, this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        super.extractRenderState(guiGraphics, i, j, f);
        guiGraphics.fill(i-1, j-1, i+1, j+1, 0xFFFF0000);
        int y = 5;
        guiGraphics.text(font, String.format("Мышь X: %s | Мышь Y: %s", i, j), 5, y, 0xFFFFffff, false);
        y+=10;
        guiGraphics.text(font, AlinLib.localization.getParsedText("{minecraft.fps}FPS"), 5, y, 0xFFFFffff, false);
        y+=10;
        assert this.minecraft != null;
        assert this.minecraft.gui.screen() != null;
        guiGraphics.text(font, String.format("Текущий скрин: %s", this.minecraft.gui.screen().getTitle().getString().isBlank() ? this.minecraft.gui.screen().getClass().getCanonicalName() : this.minecraft.gui.screen().getTitle().getString()), 5, y, 0xFFFFffff, false);
    }
}
