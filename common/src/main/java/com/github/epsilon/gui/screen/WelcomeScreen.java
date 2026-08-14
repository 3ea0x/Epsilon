package com.github.epsilon.gui.screen;

import com.github.epsilon.holders.ConfigHolder;
import com.github.epsilon.modules.impl.ClientSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class WelcomeScreen extends Screen {

    private static final String TITLE_B64 = "5qyi6L+O5L2/55SoIEVwc2lsb24gLyBXZWxjb21lIHRvIEVwc2lsb24=";
    private static final String NOTICE_B64 = "5pys5a6i5oi356uv5bey5LuO5byA5rqQ6L2s5Li65LuY6LS554mI5pys77yM6I635Y+W5YaF6YOo54mI5pys6K+36IGU57O7IFFR77yaMzM3MzUwMjE2M+OAgiAvIFRoaXMgY2xpZW50IGhhcyB0cmFuc2l0aW9uZWQgZnJvbSBvcGVuIHNvdXJjZSB0byBhIHBhaWQgdmVyc2lvbi4gQ29udGFjdCBRUSAzMzczNTAyMTYzIHRvIG9idGFpbiB0aGUgaW50ZXJuYWwgdmVyc2lvbi4=";
    private static final String CONTINUE_B64 = "57un57utIC8gQ29udGludWU=";
    private static final String DONT_SHOW_AGAIN_B64 = "5LiL5qyh5LiN5YaN5pi+56S6ICjkvaDngrnkuobkuZ/msqHnlKgp";

    public static final WelcomeScreen INSTANCE = new WelcomeScreen();

    private static final int CARD_MARGIN = 16;
    private static final int CARD_PADDING = 20;
    private static final int TITLE_TOP = 18;
    private static final int TITLE_TO_BODY_GAP = 26;
    private static final int BODY_LINE_HEIGHT = 12;
    private static final int BODY_BLOCK_GAP = 4;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_BLOCK_HEIGHT = BUTTON_HEIGHT * 2 + BUTTON_GAP;
    private static final int BUTTON_BOTTOM_PADDING = 12;

    private final List<Component> bodyLines = List.of(
            Component.literal(decode(NOTICE_B64))
    );

    private WelcomeScreen() {
        super(Component.literal(decode(TITLE_B64)));
    }

    private long openedAtMs;

    @Override
    protected void init() {
        super.init();
        openedAtMs = Util.getMillis();

        int buttonWidth = Math.min(200, Math.max(160, this.width - 40));
        int buttonX = (this.width - buttonWidth) / 2;
        int cardHeight = getCardHeight();
        int buttonY = getCardY(cardHeight) + cardHeight - BUTTON_BOTTOM_PADDING - BUTTON_BLOCK_HEIGHT;

        this.addRenderableWidget(Button.builder(Component.literal(decode(DONT_SHOW_AGAIN_B64)), button -> confirmDoNotShowAgain())
                .bounds(buttonX, buttonY, buttonWidth, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(decode(CONTINUE_B64)), button -> continueToNextScreen())
                .bounds(buttonX, buttonY + BUTTON_HEIGHT + BUTTON_GAP, buttonWidth, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float fade = Mth.clamp((Util.getMillis() - openedAtMs) / 250.0f, 0.0f, 1.0f);
        int alpha = Math.round(255.0f * fade);

        int cardWidth = Math.min(560, this.width - CARD_MARGIN * 2);
        int bodyWidth = cardWidth - CARD_PADDING * 2;
        int cardHeight = getCardHeight();
        int cardX = (this.width - cardWidth) / 2;
        int cardY = getCardY(cardHeight);

        int cardColor = new Color(20, 20, 24, Math.min(235, alpha)).getRGB();
        int outlineColor = new Color(120, 124, 132, Math.min(120, alpha)).getRGB();
        int titleColor = new Color(245, 245, 245, alpha).getRGB();
        int bodyColor = new Color(220, 223, 230, alpha).getRGB();

        graphics.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, cardColor);
        graphics.outline(cardX, cardY, cardWidth, cardHeight, outlineColor);

        String title = getTitle().getString();
        int titleX = this.width / 2 - font.width(title) / 2;
        int titleY = cardY + TITLE_TOP;
        graphics.text(font, title, titleX, titleY, titleColor, false);

        int textX = cardX + CARD_PADDING;
        int textY = titleY + TITLE_TO_BODY_GAP;
        for (Component line : bodyLines) {
            List<net.minecraft.util.FormattedCharSequence> wrapped = font.split(line, bodyWidth);
            for (net.minecraft.util.FormattedCharSequence wrappedLine : wrapped) {
                graphics.text(font, wrappedLine, textX, textY, bodyColor, false);
                textY += BODY_LINE_HEIGHT;
            }
            textY += BODY_BLOCK_GAP;
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        continueToNextScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void confirmDoNotShowAgain() {
//        ClientSetting.INSTANCE.showWelcomeScreen.setValue(false);
        ConfigHolder.INSTANCE.saveNow();
        continueToNextScreen();
    }

    private void continueToNextScreen() {
        if (ClientSetting.INSTANCE.useMainMenu.getValue()) {
            minecraft.setScreen(MainMenuScreen.INSTANCE);
        } else {
            minecraft.setScreen(new TitleScreen());
        }
    }

    private int getCardHeight() {
        int cardWidth = Math.min(560, this.width - CARD_MARGIN * 2);
        int bodyWidth = cardWidth - CARD_PADDING * 2;
        int bodyHeight = 0;
        for (Component line : bodyLines) {
            bodyHeight += font.split(line, bodyWidth).size() * BODY_LINE_HEIGHT;
            bodyHeight += BODY_BLOCK_GAP;
        }
        if (bodyHeight > 0) {
            bodyHeight -= BODY_BLOCK_GAP;
        }
        return Math.min(this.height - CARD_MARGIN * 2, TITLE_TOP + TITLE_TO_BODY_GAP + bodyHeight + BUTTON_BOTTOM_PADDING + BUTTON_BLOCK_HEIGHT);
    }

    private int getCardY(int cardHeight) {
        return (this.height - cardHeight) / 2;
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

}
