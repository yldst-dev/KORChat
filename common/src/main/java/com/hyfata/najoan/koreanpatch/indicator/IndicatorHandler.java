package com.hyfata.najoan.koreanpatch.indicator;

import com.hyfata.najoan.koreanpatch.config.ColorOpacityConfig;
import com.hyfata.najoan.koreanpatch.config.ConfigManager;
import com.hyfata.najoan.koreanpatch.config.OutlineType;
import com.hyfata.najoan.koreanpatch.config.category.CategoryIndicator;
import com.hyfata.najoan.koreanpatch.config.category.indicator.outline.OutlineConfig;
import com.hyfata.najoan.koreanpatch.driver.InputController;
import com.hyfata.najoan.koreanpatch.driver.InputManager;
import com.hyfata.najoan.koreanpatch.mixin.accessor.MultilineEditBoxAccessor;
import com.hyfata.najoan.koreanpatch.process.LangTypeManager;
import com.hyfata.najoan.koreanpatch.process.LanguageType;
import com.hyfata.najoan.koreanpatch.util.minecraft.EditBoxUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

import java.awt.Color;
import java.util.Map;
import java.util.WeakHashMap;

public class IndicatorHandler {
    private static final Minecraft client = Minecraft.getInstance();
    private static final int frame = 1;
    private static final int horizontalPadding = 0;
    private static final int verticalPadding = 1;
    private static final float textScale = 1.0f;
    private static final Map<Screen, AnimationHandler> animationHandlers = new WeakHashMap<>();

    public static float getIndicatorWidth() {
        float textWidth = (float) LangTypeManager.getInstance().getCurrentTextWidth() * textScale;
        float height = getIndicatorHeight();
        float width = frame * 2f + textWidth + horizontalPadding * 2f;
        if (width <= height + 2f) {
            return height;
        }
        return width;
    }

    public static float getIndicatorHeight() {
        return frame * 2f + (float) client.font.lineHeight * textScale + verticalPadding * 2f;
    }

    public static void renderForScreen(Screen screen, GuiGraphicsExtractor graphics) {
        CategoryIndicator indicator = ConfigManager.getInstance().getConfig().getCategoryIndicator();
        if (!indicator.isShowIndicator()) {
            return;
        }

        GuiEventListener focused = findFocusedInput(screen);
        if (focused instanceof EditBox editBox && editBox.isFocused()) {
            float x = EditBoxUtil.getCursorX(editBox) + 4f;
            float y = EditBoxUtil.calculateIndicatorY(editBox);
            showAnimatedIndicator(screen, graphics, x, y);
            return;
        }

        if (focused instanceof MultiLineEditBox editBox && editBox.isFocused()) {
            renderMultiline(screen, graphics, editBox);
        }
    }

    private static GuiEventListener findFocusedInput(ContainerEventHandler container) {
        GuiEventListener focused = container.getFocused();
        if (focused instanceof EditBox || focused instanceof MultiLineEditBox) {
            return focused;
        }
        if (focused instanceof ContainerEventHandler nested) {
            GuiEventListener nestedFocused = findFocusedInput(nested);
            if (nestedFocused != null) {
                return nestedFocused;
            }
        }
        for (GuiEventListener child : container.children()) {
            if (!child.isFocused()) {
                continue;
            }
            if (child instanceof EditBox || child instanceof MultiLineEditBox) {
                return child;
            }
            if (child instanceof ContainerEventHandler nested) {
                GuiEventListener nestedFocused = findFocusedInput(nested);
                if (nestedFocused != null) {
                    return nestedFocused;
                }
            }
        }
        return null;
    }

    public static void showIndicator(GuiGraphicsExtractor graphics, float x, float y) {
        FormattedCharSequence text = LangTypeManager.getInstance().getCurrentText();
        int width = Math.max(1, Math.round(getIndicatorWidth()));
        int height = Math.max(1, Math.round(getIndicatorHeight()));
        int left = Math.round(x);
        int top = Math.round(y);
        int textColor = getArgb(ConfigManager.getInstance().getConfig().getCategoryIndicator().getTextSettings());
        int backgroundColor = getBackgroundArgb();
        OutlineConfig outline = ConfigManager.getInstance().getConfig().getCategoryIndicator().getOutlineSettings();
        OutlineType outlineType = outline.getOutlineType();

        drawIndicatorShape(graphics, left, top, width, height, backgroundColor, outlineType);
        if (outline.isShowOutline()) {
            drawIndicatorOutline(graphics, left, top, width, height, getOutlineArgb(outline.getColorOpacitySettings()), outlineType);
        }

        float scaledTextWidth = client.font.width(text) * textScale;
        float scaledTextHeight = client.font.lineHeight * textScale;
        int textX = left + Math.max(0, Math.round((width - scaledTextWidth) / 2f));
        int textY = top + Math.max(0, Math.round((height - scaledTextHeight) / 2f));
        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, textY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(client.font, text, 0, 0, textColor, false);
        graphics.pose().popMatrix();
    }

    public static void showCenteredIndicator(GuiGraphicsExtractor graphics, float centerX, float centerY) {
        float x = centerX - getIndicatorWidth() / 2f;
        float y = centerY - getIndicatorHeight() / 2f;
        showIndicator(graphics, x, y);
    }

    private static void renderMultiline(Screen screen, GuiGraphicsExtractor graphics, MultiLineEditBox editBox) {
        MultilineTextField textField = ((MultilineEditBoxAccessor) editBox).getTextField();
        int lineIndex = Math.max(0, textField.getLineAtCursor());
        Object lineView = textField.getLineView(lineIndex);
        int beginIndex = invokeLineIndex(lineView, "beginIndex");
        int endIndex = invokeLineIndex(lineView, "endIndex");
        String value = textField.value();
        int cursor = Math.max(beginIndex, Math.min(textField.cursor(), endIndex));
        String beforeCursor = value.substring(beginIndex, cursor);
        float maxX = editBox.getX() + editBox.getWidth() - getIndicatorWidth() - 2f;
        float x = editBox.getX() + 4f + client.font.width(beforeCursor) + 4f;
        float y = (float) (editBox.getY() + 4f - editBox.scrollAmount() + lineIndex * 9f - getIndicatorHeight() - 1f);
        showAnimatedIndicator(screen, graphics, Math.min(x, maxX), y);
    }

    private static void showAnimatedIndicator(Screen screen, GuiGraphicsExtractor graphics, float x, float y) {
        AnimationHandler animationHandler = animationHandlers.computeIfAbsent(screen, ignored -> new AnimationHandler());
        animationHandler.init(x, y);
        animationHandler.calculateAnimation(x, y);
        showIndicator(graphics, animationHandler.getResultX(), animationHandler.getResultY());
    }

    private static int invokeLineIndex(Object lineView, String methodName) {
        try {
            return (int) lineView.getClass().getMethod(methodName).invoke(lineView);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    private static void drawIndicatorShape(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int color, OutlineType outlineType) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (outlineType == OutlineType.RECTANGLE) {
            graphics.fill(left, top, left + width, top + height, color);
            return;
        }
        int radius = Math.max(1, Math.min(height / 2, 3));
        double exponent = outlineType == OutlineType.SUPERELLIPSE ? 4.0 : 2.0;
        for (int row = 0; row < height; row++) {
            int inset = computeCornerInset(row, height, radius, exponent);
            graphics.fill(left + inset, top + row, left + width - inset, top + row + 1, color);
        }
    }

    private static void drawIndicatorOutline(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int color, OutlineType outlineType) {
        if (width <= 1 || height <= 1) {
            drawIndicatorShape(graphics, left, top, width, height, color, outlineType);
            return;
        }
        if (outlineType == OutlineType.RECTANGLE) {
            graphics.horizontalLine(left, left + width - 1, top, color);
            graphics.horizontalLine(left, left + width - 1, top + height - 1, color);
            graphics.verticalLine(left, top, top + height - 1, color);
            graphics.verticalLine(left + width - 1, top, top + height - 1, color);
            return;
        }
        int innerWidth = width - frame * 2;
        int innerHeight = height - frame * 2;
        if (innerWidth <= 0 || innerHeight <= 0) {
            drawIndicatorShape(graphics, left, top, width, height, color, outlineType);
            return;
        }
        double exponent = outlineType == OutlineType.SUPERELLIPSE ? 4.0 : 2.0;
        int outerRadius = Math.max(1, Math.min(height / 2, 3));
        int innerRadius = Math.max(1, Math.min(innerHeight / 2, 3));
        for (int row = 0; row < height; row++) {
            int outerInset = computeCornerInset(row, height, outerRadius, exponent);
            int outerLeft = left + outerInset;
            int outerRight = left + width - outerInset;
            int y = top + row;
            int innerRow = row - frame;
            if (innerRow < 0 || innerRow >= innerHeight) {
                graphics.fill(outerLeft, y, outerRight, y + 1, color);
                continue;
            }
            int innerInset = computeCornerInset(innerRow, innerHeight, innerRadius, exponent);
            int innerLeft = left + frame + innerInset;
            int innerRight = left + frame + innerWidth - innerInset;
            if (innerLeft > outerLeft) {
                graphics.fill(outerLeft, y, innerLeft, y + 1, color);
            }
            if (outerRight > innerRight) {
                graphics.fill(innerRight, y, outerRight, y + 1, color);
            }
        }
    }

    private static int computeCornerInset(int row, int height, int radius, double exponent) {
        if (row >= radius && row < height - radius) {
            return 0;
        }
        int offset = row < radius ? radius - 1 - row : row - (height - radius);
        double y = offset + 0.5;
        double normalized = Math.min(1.0, y / radius);
        double projected = Math.pow(1.0 - Math.pow(normalized, exponent), 1.0 / exponent);
        double x = radius * projected;
        return Math.max(0, (int) Math.round(radius - x));
    }

    private static int getArgb(ColorOpacityConfig config) {
        Color color = getCurrentColor(config);
        int alpha = Math.max(0, Math.min(255, Math.round(config.getOpacity() * 255f / 100f)));
        return alpha << 24 | color.getRGB() & 16777215;
    }

    private static int getOutlineArgb(ColorOpacityConfig config) {
        Color color = getCurrentColor(config);
        int alpha = Math.max(0, Math.min(255, Math.round(config.getOpacity() * 208f / 100f)));
        return alpha << 24 | color.getRGB() & 16777215;
    }

    private static int getBackgroundArgb() {
        int opacity = ConfigManager.getInstance().getConfig().getCategoryIndicator().getBackgroundSettings().getOpacity();
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255f / 100f)));
        return alpha << 24;
    }

    private static Color getCurrentColor(ColorOpacityConfig config) {
        InputController controller = InputManager.getController();
        if (controller != null && controller.isFocused()) {
            return config.getImeColor();
        }
        if (LangTypeManager.getInstance().getCurrentType() == LanguageType.KO) {
            return config.getKoreanColor();
        }
        return config.getEnColor();
    }
}
