package ru.kelcu.windows.mixin.components.elemets;

import net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.kelcu.windows.components.Window;
import ru.kelcu.windows.screens.DesktopScreen;

@Mixin(GuiSkinRenderState.class)
public abstract class GuiSkinRenderStateMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/model/Model$Simple;Lnet/minecraft/resources/Identifier;FFFIIIIFLnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At("HEAD"), index = 6, argsOnly = true)
    private static int x0(int value){
        if(DesktopScreen.currentRenderedWindow != null){
            Window window = DesktopScreen.currentRenderedWindow;
            return value + (int) (window.x+3);
        }
        return value;
    }
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/model/Model$Simple;Lnet/minecraft/resources/Identifier;FFFIIIIFLnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At("HEAD"), index = 7, argsOnly = true)
    private static int y0(int value){
        if(DesktopScreen.currentRenderedWindow != null){
            Window window = DesktopScreen.currentRenderedWindow;
            return value + (int) (window.y+19);
        }
        return value;
    }
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/model/Model$Simple;Lnet/minecraft/resources/Identifier;FFFIIIIFLnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At("HEAD"), index = 8, argsOnly = true)
    private static int x1(int value){
        if(DesktopScreen.currentRenderedWindow != null){
            Window window = DesktopScreen.currentRenderedWindow;
            return value + (int) (window.x+3);
        }
        return value;
    }
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/model/Model$Simple;Lnet/minecraft/resources/Identifier;FFFIIIIFLnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V", at = @At("HEAD"), index = 9, argsOnly = true)
    private static int y1(int value){
        if(DesktopScreen.currentRenderedWindow != null){
            Window window = DesktopScreen.currentRenderedWindow;
            return value + (int) (window.y+19);
        }
        return value;
    }
}
