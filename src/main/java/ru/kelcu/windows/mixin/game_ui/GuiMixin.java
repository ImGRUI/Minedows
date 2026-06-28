package ru.kelcu.windows.mixin.game_ui;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBar;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcu.windows.Windows;
import ru.kelcu.windows.utils.WinColors;
import ru.kelcu.windows.utils.WindowUtils;
import ru.kelcuprum.alinlib.AlinLib;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static ru.kelcuprum.alinlib.gui.Colors.*;
import static ru.kelcuprum.alinlib.gui.Colors.GROUPIE;

@Mixin(Hud.class)
public abstract class GuiMixin {
    @Shadow
    @Nullable
    protected abstract Player getCameraPlayer();

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private SpectatorGui spectatorGui;

    @Shadow
    protected abstract void extractItemHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker);

    @Shadow
    protected abstract void extractPlayerHealth(GuiGraphicsExtractor guiGraphics);

    @Shadow
    protected abstract void extractVehicleHealth(GuiGraphicsExtractor guiGraphics);

    @Shadow
    protected abstract Hud.ContextualInfo nextContextualInfoState();

    @Shadow
    private com.mojang.datafixers.util.Pair<Hud.ContextualInfo, ContextualBar> contextualInfoBar;
    @Shadow
    @Final
    private Map<Hud.ContextualInfo, Supplier<ContextualBar>> contextualInfoBars;

    @Shadow
    protected abstract void extractSelectedItemName(GuiGraphicsExtractor guiGraphics);

    @Shadow
    @Nullable
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    public abstract Font getFont();

    @Unique
    int screenWidth, screenHeight;
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.screenWidth = guiGraphics.guiWidth();
        this.screenHeight = guiGraphics.guiHeight();
    }
    // HOTBAR

    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarAndDecorations(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.extractHotbar(guiGraphics);
        } else {
            this.extractItemHotbar(guiGraphics, deltaTracker);
        }

        if (this.minecraft.gameMode.canHurtPlayer()) {
            this.extractPlayerHealth(guiGraphics);
        }

        this.extractVehicleHealth(guiGraphics);
        Hud.ContextualInfo contextualInfo = this.nextContextualInfoState();
        Hud.ContextualInfo contextualInfo2 = this.nextContextualInfoStateFucked();
        if (contextualInfo != this.contextualInfoBar.getFirst()) {
            this.contextualInfoBar = Pair.of(contextualInfo, (ContextualBar)((Supplier)this.contextualInfoBars.get(contextualInfo)).get());
        }
        if (contextualInfoBar.getFirst() == Hud.ContextualInfo.LOCATOR) {
            Pair<Hud.ContextualInfo, ContextualBar> notContextualInfoBar = Pair.of(contextualInfo, (ContextualBar) ((Supplier) this.fuckLocatorBar.get(contextualInfo2)).get());
            ((ContextualBar)notContextualInfoBar.getSecond()).extractBackground(guiGraphics, deltaTracker);
            if (this.minecraft.gameMode.hasExperience() && this.minecraft.player.experienceLevel > 0) {
                renderExperienceLevel(guiGraphics, this.minecraft.font, this.minecraft.player.experienceLevel);
            }

            notContextualInfoBar.getSecond().extractRenderState(guiGraphics, deltaTracker);
        }
        ((ContextualBar) this.contextualInfoBar.getSecond()).extractBackground(guiGraphics, deltaTracker);
        if (this.minecraft.gameMode.hasExperience() && this.minecraft.player.experienceLevel > 0) {
            renderExperienceLevel(guiGraphics, this.minecraft.font, this.minecraft.player.experienceLevel);
        }

        ((ContextualBar) this.contextualInfoBar.getSecond()).extractBackground(guiGraphics, deltaTracker);
        if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.extractSelectedItemName(guiGraphics);
        } else if (this.minecraft.player.isSpectator()) {
            this.spectatorGui.extractAction(guiGraphics);
        }
        ci.cancel();
    }

    @Inject(method = "extractItemHotbar", at=@At("HEAD"), cancellable = true)
    public void renderHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci){
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        int x1 = screenWidth / 2 - (186 / 2);
        int x2 = screenWidth / 2 + (186 / 2);
        ItemStack off_item = getCameraPlayer().getOffhandItem();
        int typeOffHand = 0;
        if (!off_item.isEmpty()) {
           if(getCameraPlayer().getMainArm().getOpposite() == HumanoidArm.LEFT) {x1-=26; typeOffHand = 1;} else { x2+=26; typeOffHand = 2;}
//            kelUI$renderSlot(guiGraphics, off_pos, getHotBarY(), deltaTracker, getCameraPlayer(), off_item, false, true);
        }
        WindowUtils.renderPanel(guiGraphics, x1, getHotBarY(), x2, screenHeight);
        int itemPos = x1+3+(typeOffHand == 1 ? 26 : 0);
        if(!off_item.isEmpty()) {
            kelUI$renderSlot(guiGraphics, typeOffHand == 1 ? x1+3 : x2-23, getHotBarY()+3, deltaTracker, getCameraPlayer(), off_item, false);
        }
        for (int slot = 0; slot < 9; slot++) {
            boolean selected = getCameraPlayer().getInventory().getSelectedSlot() == slot;
            kelUI$renderSlot(guiGraphics, itemPos + (slot * 20), getHotBarY()+3, deltaTracker, getCameraPlayer(), getCameraPlayer().getInventory().getItem(slot), selected);
        }
        if(typeOffHand != 0){
            int x3 = typeOffHand == 1 ? x1+25 : x2-27;
            int[] hcolors = WinColors.getHorizontalRuleColors();
            guiGraphics.fill(x3, getHotBarY()+5, x3 + 2, screenHeight-5, hcolors[0]);
            guiGraphics.fill(x3, getHotBarY()+5, x3 + 1, screenHeight-5, hcolors[1]);
        }
        ci.cancel();
    }
    @Inject(method = "extractPlayerHealth", at = @At("HEAD"), cancellable = true)
    void renderPlayrerHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        if (this.minecraft.player == null) return;
        double health = this.minecraft.player.getHealth() / this.minecraft.player.getAttributeValue(Attributes.MAX_HEALTH);
        double armor = (double) this.minecraft.player.getArmorValue() / 20;
        double hunger = (double) this.minecraft.player.getFoodData().getFoodLevel() / 20;
        double air = (double) Math.max(0, this.minecraft.player.getAirSupply()) / this.minecraft.player.getMaxAirSupply();
        int healthColor = this.minecraft.player.hasEffect(MobEffects.POISON) ? 0xFFa3b18a :
                this.minecraft.player.hasEffect(MobEffects.WITHER) ? 0xff4a4e69 :
                        this.minecraft.player.isFullyFrozen() ? 0xFF90e0ef :
                                this.minecraft.player.level().getLevelData().isHardcore() ? ALINA : GROUPIE;
        //
        int size = 82;
        int pos = 105;
        //
        renderBar(guiGraphics, getHotBarX(), getHotBarY() - 13, healthColor, size, 4, health);
        if (armor != 0) renderBar(guiGraphics, getHotBarX(), getHotBarY() - 18, 0xff598392, size, 4, armor);
        //
        renderBar(guiGraphics, getHotBarX() + pos, getHotBarY() - 13, 0xFFff9b54, size, 4, hunger);
        if (this.minecraft.player.isUnderWater() || this.minecraft.player.getAirSupply() != this.minecraft.player.getMaxAirSupply())
            renderBar(guiGraphics, getHotBarX() + pos, getHotBarY() - 18, 0xff0000FF, size, 4, air);

        ci.cancel();
    }
    @Inject(method = "extractVehicleHealth", at = @At("HEAD"), cancellable = true)
    void renderVehicleHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        assert this.minecraft.gameMode != null;
        assert this.minecraft.player != null;
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) return;
        int size = 82;
        int pos = 105;

        int y = getHotBarY() - 6;
        assert this.minecraft.gameMode != null;
        if (this.isExperienceBarVisible()) y -= 8;
        if (this.minecraft.gameMode.canHurtPlayer()) {
            if (this.minecraft.player.isUnderWater() || this.minecraft.player.getAirSupply() != this.minecraft.player.getMaxAirSupply()) y -= 5;
            if (this.minecraft.player.getArmorValue() != 0) y -= 5;
        }

        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        if (livingEntity != null) {
            double health = livingEntity.getHealth() / livingEntity.getMaxHealth();
            renderBar(guiGraphics, getHotBarX()+pos, y, CLOWNFISH, size, 4, Math.min(health, 1));
        }
        ci.cancel();
    }
    // Hotbar shit
    @Unique
    private void renderExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level){
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        Component component = Component.translatable("gui.experience.level", level);
        int i = (guiGraphics.guiWidth() - font.width(component)) / 2;
        int var10000 = guiGraphics.guiHeight() - 26;
        Objects.requireNonNull(font);
        int j = var10000 - 9 - font.lineHeight;
        guiGraphics.text(font, component, i + 1, j, -16777216, false);
        guiGraphics.text(font, component, i - 1, j, -16777216, false);
        guiGraphics.text(font, component, i, j + 1, -16777216, false);
        guiGraphics.text(font, component, i, j - 1, -16777216, false);
        guiGraphics.text(font, component, i, j, -8323296, false);
    }
    @Unique
    void renderBar(GuiGraphicsExtractor guiGraphics, int x, int y, int color, int size, int height, double value){
        if(!Windows.config.getBoolean("ENABLE_NEW_UI", false)) return;
        int[] colors = WinColors.getWindowColors();
        guiGraphics.fill(x, y, x+size-1, y+height, colors[3]);
        guiGraphics.fill(x+1, y+1, x+size-1, y+height, colors[1]);
        guiGraphics.fill(x+1, y+1, x+size-2, y+height-1, colors[2]);
        int ih = (size-2) / 20;
        for(int i = 0; i<(int) (20*value); i++){
            guiGraphics.fill(x+1+(i*ih), y+1, x+1+(i*ih)+(ih-1), y-1+height, color);
        }
    }
    @Unique
    private final Map<Hud.ContextualInfo, Supplier<ContextualBar>> fuckLocatorBar = ImmutableMap.of(Hud.ContextualInfo.EMPTY, (Supplier) () -> ContextualBar.EMPTY, Hud.ContextualInfo.EXPERIENCE, (Supplier) () -> new ExperienceBar(minecraft), Hud.ContextualInfo.JUMPABLE_VEHICLE, (Supplier) () -> new JumpableVehicleBar(minecraft));
    @Unique
    public boolean isExperienceBarVisible() {
        return this.nextContextualInfoStateFucked() != Hud.ContextualInfo.EMPTY;
    }
    @Unique
    private Hud.ContextualInfo nextContextualInfoStateFucked() {
        boolean bl2 = this.minecraft.player.jumpableVehicle() != null;
        boolean bl3 = this.minecraft.gameMode.hasExperience();
        if (bl2) {
            return Hud.ContextualInfo.JUMPABLE_VEHICLE;
        } else {
            return bl3 ? Hud.ContextualInfo.EXPERIENCE : Hud.ContextualInfo.EMPTY;
        }
    }

    //
    @Unique int getColor(int original, int value){
        int r = ARGB.red(original);
        int g = ARGB.green(original);
        int b = ARGB.blue(original);
        double w = value / 255.0;
        return ARGB.color(255, (int) (r*w), (int) (g*w), (int) (b*w));
    }
    @Unique
    void kelUI$renderSlot(GuiGraphicsExtractor guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, boolean isSelected) {
        int[] colors = WinColors.getWindowColors();
        int darkness = getColor(colors[2], 210);
        for(int i = 0; i<20; i++){ // X
            for(int j = i % 2; j<20; j+=2){ // Y
                guiGraphics.fill(x+i, y+j, x+i+1, y+j+1, darkness);
            }
        }
        if(isSelected){
            guiGraphics.fill(x, y, x+20, y+20, 0x3e0000F3);

            guiGraphics.
            //#if MC < 12110
            //$$renderOutline
            //#else
            outline
            //#endif
            (x, y, 20, 20, 0x3e0000F3);
        }
        if (!itemStack.isEmpty()) {
            float g = (float) itemStack.getPopTime() - deltaTracker.getGameTimeDeltaTicks();
            if (g > 0.0F) {
                float h = 1.0F + g / 5.0F;
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate((float) (x + 8), (float) (y + 12));
                guiGraphics.pose().scale(1.0F / h, (h + 1.0F) / 2.0F);
                guiGraphics.pose().translate((float) (-(x + 8)), (float) (-(y + 12)));
            }

            guiGraphics.item(player, itemStack, x + 2, y + 2, 1);
            if (g > 0.0F) guiGraphics.pose().popMatrix();
            guiGraphics.itemDecorations(this.minecraft.font, itemStack, x + 2, y + 2);
        }
    }
    @Unique
    public int getHotBarX(){
        return screenWidth / 2 - (186 / 2);
    }
    @Unique
    public int getHotBarY(){
        return screenHeight - 26;
    }
}
