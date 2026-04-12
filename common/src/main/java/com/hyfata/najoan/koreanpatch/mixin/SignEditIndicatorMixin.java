package com.hyfata.najoan.koreanpatch.mixin;

import com.hyfata.najoan.koreanpatch.indicator.AnimationHandler;
import com.hyfata.najoan.koreanpatch.indicator.IndicatorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditIndicatorMixin extends Screen {
    @Shadow
    private int line;

    @Shadow
    @Final
    protected SignBlockEntity sign;

    @Unique
    private final AnimationHandler koreanPatch$animationHandler = new AnimationHandler();

    protected SignEditIndicatorMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractSignText", at = @At("TAIL"))
    private void koreanPatch$renderSignIndicator(GuiGraphicsExtractor graphics, org.joml.Vector2f cursorPos, CallbackInfo ci) {
        float x = -(sign.getMaxTextLineWidth() / 2f) - IndicatorHandler.getIndicatorWidth() / 2f - 5f;
        int textBlockHeight = 4 * sign.getTextLineHeight() / 2;
        float y = line * sign.getTextLineHeight() - textBlockHeight + Minecraft.getInstance().font.lineHeight / 2f;
        koreanPatch$animationHandler.init(0, y - 4f);
        koreanPatch$animationHandler.calculateAnimation(0, y);
        IndicatorHandler.showCenteredIndicator(graphics, x, koreanPatch$animationHandler.getResultY());
    }
}
