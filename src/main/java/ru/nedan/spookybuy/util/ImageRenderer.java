package ru.nedan.spookybuy.util;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ImageRenderer {
    private static final Map<String, Identifier> cache = new HashMap<>();

    static {
        // Регистр текстур из ресурсов потому что нет fabric-resource-loader

        loadOrGetTexture(ImageRenderer.class.getResourceAsStream("/assets/spookybuy/icons/left-slider.png"), "left");
        loadOrGetTexture(ImageRenderer.class.getResourceAsStream("/assets/spookybuy/icons/right-slider.png"), "right");
    }

    public static Identifier loadOrGetTexture(InputStream input, String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        try {
            NativeImage image = NativeImage.read(input);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier id = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(name, texture);
            cache.put(name, id);
            return id;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    public static void render(Identifier textureId, float x, float y, float width, float height, int color) {
        if (textureId == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.enableDepthTest();
        mc.getTextureManager().bindTexture(textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_POLYGON, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        bufferBuilder.vertex(x, y + height, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).texture(0, 1 - 0.01f).light(0, 240).next();
        bufferBuilder.vertex(x + width, y + height, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).texture(1, 1 - 0.01f).light(0, 240).next();
        bufferBuilder.vertex(x + width, y, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).texture(1, 0).light(0, 240).next();
        bufferBuilder.vertex(x, y, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, color >>> 24).texture(0, 0).light(0, 240).next();
        tessellator.draw();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}