package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class CustomCape {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static CustomCape instance;
    private final ArrayList<ResourceLocation> capeLocations;
    private boolean isAnimated;
    private int currentFrame = 0;
    private int frameDelay = 5; // ticks between frames
    private int tickCounter = 0;
    private File capeFile;
    private ResourceLocation originalCapeLocation;

    public CustomCape() {
        instance = this;
        capeLocations = new ArrayList<>();
        // Load initial frame delay from ModConfig
        frameDelay = Math.max(1, ModConfig.customCapeFrameDelay);
        loadCapeFromFile();
    }

    public static void reloadCape() {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            CustomCape inst = (instance != null) ? instance : new CustomCape();
            inst.deleteOldTextures();
            inst.reloadCape(); // reuse the existing instance method body
            inst.printStatus();
        });
    }

    private void deleteOldTextures() {
        try {
            for (ResourceLocation loc : capeLocations) {
                try {
                    mc.getTextureManager().deleteTexture(loc);
                } catch (Throwable ignored) {}
            }
        } finally {
            capeLocations.clear();
            currentFrame = 0;
            tickCounter = 0;
        }
    }

    public static CustomCape getInstance() {
        if (instance == null) {
            instance = new CustomCape();
        }
        return instance;
    }

    private boolean isEnabled() {
        return ModConfig.enableCustomCape;
    }

    /**
     * How the custom cape loads:
     * 1. Check if module is enabled in config
     * 2. Look for 'capes' directory in mod's config folder (config/ratallofyou/capes)
     * 3. Find first PNG or GIF file alphabetically
     * 4. Load the image file and create Minecraft textures
     */
    private void loadCapeFromFile() {
        if (!isEnabled()) {
            return;
        }

        // Step 1: Create mod config directory structure if it doesn't exist
        File modConfigDir = new File("config/ratallofyou");
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }

        // Step 2: Create capes directory within mod config
        File capesDir = new File(modConfigDir, "capes");
        if (!capesDir.exists()) {
            capesDir.mkdirs();
            // Don't return here - continue to check for files even in newly created directory
        }

        // Step 3: Look for cape files (png or gif) - first file alphabetically
        File[] files = capesDir.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif"));

        if (files != null && files.length > 0) {
            // Use the first cape file found (alphabetically)
            java.util.Arrays.sort(files);
            capeFile = files[0];
            loadCape(capeFile);
        }
    }

    /**
     * Loads the cape file and determines if it's animated (GIF) or static (PNG)
     */
    private void loadCape(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            String fileName = file.getName().toLowerCase();

            if (fileName.endsWith(".gif")) {
                loadAnimatedCape(fis);
            } else if (fileName.endsWith(".png")) {
                loadStaticCape(fis);
            }

            fis.close();
        } catch (Exception e) {
            System.err.println("[CustomCape] Failed to load cape: " + e.getMessage());
        }
    }

    /**
     * Loads animated GIF cape by reading each frame and creating separate textures
     * This is how animated capes work - each frame becomes a separate ResourceLocation
     */
    private void loadAnimatedCape(InputStream capeStream) {
        try {
            isAnimated = true;

            // Get GIF reader
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                ImageInputStream iis = ImageIO.createImageInputStream(capeStream);
                reader.setInput(iis);

                int numImages = reader.getNumImages(true);

                // Create a ResourceLocation for each frame
                for (int i = 0; i < numImages; i++) {
                    BufferedImage frame = reader.read(i);
                    // Fix transparent pixels in GIF frame
                    frame = makeOpaque(frame, 0xFFFFFF); // Use white background for transparent pixels
                    DynamicTexture texture = new DynamicTexture(frame);
                    ResourceLocation location = mc.getTextureManager().getDynamicTextureLocation(
                        "ratallofyou_custom_cape_" + i, texture);
                    capeLocations.add(location);
                }

                reader.dispose();
                iis.close();
            }
        } catch (Exception e) {
            System.err.println("[CustomCape] Error loading animated cape: " + e.getMessage());
        }
    }

    /**
     * Loads static PNG cape - creates single texture
     */
    private void loadStaticCape(InputStream capeStream) {
        try {
            isAnimated = false;

            // Get PNG reader
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                ImageInputStream iis = ImageIO.createImageInputStream(capeStream);
                reader.setInput(iis);

                BufferedImage image = reader.read(0);

                // Create single ResourceLocation for static cape
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = mc.getTextureManager().getDynamicTextureLocation(
                    "ratallofyou_custom_cape", texture);
                capeLocations.add(location);

                reader.dispose();
                iis.close();
            }
        } catch (Exception e) {
            System.err.println("[CustomCape] Error loading static cape: " + e.getMessage());
        }
    }

    /**
     * Returns the current cape texture to be rendered
     * For animated capes, cycles through frames
     * For static capes, always returns the same texture
     */
    public ResourceLocation getCurrentCapeLocation() {
        if (!isEnabled() || capeLocations.isEmpty()) {
            return null;
        }

        if (isAnimated && capeLocations.size() > 1) {
            // Return current frame for animated cape
            return capeLocations.get(currentFrame % capeLocations.size());
        } else {
            // Return single texture for static cape
            return capeLocations.get(0);
        }
    }

    /**
     * Handles animation timing for GIF capes
     * Called every client tick to advance animation frames
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Keep frameDelay in sync with config
        int desiredDelay = Math.max(1, ModConfig.customCapeFrameDelay);
        if (desiredDelay != frameDelay) setFrameDelay(desiredDelay);

        if (!isEnabled()) {
            // If disabled, ensure counters reset so when re-enabled animation starts fresh
            tickCounter = 0;
            currentFrame = 0;
            return;
        }

        // If enabled but nothing loaded yet, try to load
        if (capeLocations.isEmpty()) {
            loadCapeFromFile();
        }

        if (!isAnimated || capeLocations.isEmpty()) return;

        tickCounter++;
        if (tickCounter >= frameDelay) {
            currentFrame = (currentFrame + 1) % capeLocations.size();
            tickCounter = 0;
        }
    }

    public boolean hasCape() {
        return isEnabled() && !capeLocations.isEmpty();
    }

    public void setFrameDelay(int delay) {
        this.frameDelay = Math.max(1, delay);
    }

    public int getFrameDelay() {
        return frameDelay;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public int getFrameCount() {
        return capeLocations.size();
    }

    /**
     * Debug method to print current cape loading status
     */
    public void printStatus() {
        System.out.println("=== CustomCape Status ===");
        System.out.println("Enabled: " + isEnabled());
        System.out.println("Has Cape: " + hasCape());
        System.out.println("Is Animated: " + isAnimated());
        System.out.println("Frame Count: " + getFrameCount());
        System.out.println("Frame Delay: " + frameDelay + " ticks");
        if (capeFile != null) {
            System.out.println("Loaded File: " + capeFile.getName());
        }
        System.out.println("========================");
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!isEnabled() || capeLocations.isEmpty()) {
            return;
        }

        // Only apply to the main player (you)
        if (event.entityPlayer == mc.thePlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;

            // Store original cape location if we haven't already
            if (originalCapeLocation == null) {
                originalCapeLocation = player.getLocationCape();
            }

            // Apply our custom cape texture
            ResourceLocation customCape = getCurrentCapeLocation();
            if (customCape != null) {
                try {
                    // Use reflection to set the cape location
                    Field capeLocationField = AbstractClientPlayer.class.getDeclaredField("locationCape");
                    capeLocationField.setAccessible(true);
                    capeLocationField.set(player, customCape);
                } catch (Exception e) {
                    // Try alternative field names if the above doesn't work
                    try {
                        Field[] fields = AbstractClientPlayer.class.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType() == ResourceLocation.class &&
                                field.getName().toLowerCase().contains("cape")) {
                                field.setAccessible(true);
                                field.set(player, customCape);
                                break;
                            }
                        }
                    } catch (Exception e2) {
                        // Silently fail
                    }
                }
            }
        }
    }

    /**
     * Restore original cape when the module is disabled
     */
    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if ((!isEnabled() || capeLocations.isEmpty()) && originalCapeLocation != null) {
            // Restore original cape
            if (event.entityPlayer == mc.thePlayer) {
                AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;
                try {
                    Field capeLocationField = AbstractClientPlayer.class.getDeclaredField("locationCape");
                    capeLocationField.setAccessible(true);
                    capeLocationField.set(player, originalCapeLocation);
                } catch (Exception e) {
                    // Silently fail
                }
            }
        }
    }

    // Add this helper method in CustomCape.java
    private BufferedImage makeOpaque(BufferedImage image, int fillColor) {
        BufferedImage opaque = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha == 0) {
                    opaque.setRGB(x, y, fillColor); // fillColor e.g. 0xFFFFFF for white
                } else {
                    opaque.setRGB(x, y, argb | 0xFF000000); // force opaque
                }
            }
        }
        return opaque;
    }

}
