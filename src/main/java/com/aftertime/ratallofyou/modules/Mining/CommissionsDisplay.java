//package com.aftertime.ratallofyou.modules.Mining;
//
//import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
//import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
//import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
//import com.aftertime.ratallofyou.utils.TabUtils;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.network.NetworkPlayerInfo;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.util.ChatComponentText;
//import net.minecraft.util.EnumChatFormatting;
//import net.minecraft.util.IChatComponent;
//import net.minecraftforge.client.event.RenderGameOverlayEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class CommissionsDisplay {
//    private static final Minecraft mc = Minecraft.getMinecraft();
//    private static final String[] COLORS = {
//            "§f", "§1", "§2", "§3", "§4", "§5", "§6", "§7",
//            "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§0"
//    };
//
//    private List<String> renderText = new ArrayList<>();
//    private String powderText = "";
//    private int tickCounter = 0;
//    private boolean inSkyblock = false;
//    private boolean debugPrinted = false;
//    private int debugResetCounter = 0;
//
//    // Patterns for matching commission text in tab list
//    private static final Pattern COMMISSION_PATTERN = Pattern.compile("§r §r§f(.*): §r(§..*%|§aDONE)§r");
//    private static final Pattern MITHRIL_POWDER_PATTERN = Pattern.compile("§r §r§fMithril Powder: §r§2(.*)§r");
//
//    @SubscribeEvent
//    public void onTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || !isModuleEnabled()) return;
//
//        // Process every 20 ticks (about 1 second)
//        if (tickCounter++ % 20 != 0) return;
//
//        // Only check if player is in Skyblock
//        inSkyblock = TabUtils.isInSkyblock();
//
//        if (inSkyblock) {
//            updateCommissions();
//        }
//
//        // Reset debug flag more frequently (every 5 seconds)
//        if (debugResetCounter++ >= 5) {
//            debugPrinted = false;
//            debugResetCounter = 0;
//            logDebug("Debug flag reset - will print tab data on next update");
//        }
//    }
//
//    @SubscribeEvent
//    public void onRender(RenderGameOverlayEvent.Post event) {
//        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !isModuleEnabled() || mc.thePlayer == null) return;
//
//        if (inSkyblock) {
//            renderCommissions();
//        }
//    }
//
//    private void logDebug(String message) {
//        System.out.println("[CommissionsDebug] " + message);
//        if (mc.thePlayer != null) {
//            mc.thePlayer.addChatMessage(new ChatComponentText(
//                    EnumChatFormatting.AQUA + "[CommissionsDebug] " +
//                    EnumChatFormatting.WHITE + message));
//        }
//    }
//
//    private void updateCommissions() {
//        // Get tab list info directly from NetworkPlayerInfo like in the test file
//        if (mc.getNetHandler() == null || mc.getNetHandler().getPlayerInfoMap() == null) {
//            logDebug("NetHandler or PlayerInfoMap is null");
//            return;
//        }
//
//        Collection<NetworkPlayerInfo> playerInfos = mc.getNetHandler().getPlayerInfoMap();
//        List<String> tabLines = new ArrayList<>();
//        List<String> output = new ArrayList<>();
//
//        // Extract tab list entries
//        for (NetworkPlayerInfo playerInfo : playerInfos) {
//            IChatComponent displayName = playerInfo.getDisplayName();
//            if (displayName != null) {
//                String line = displayName.getFormattedText();
//                tabLines.add(line);
//            } else if (playerInfo.getGameProfile() != null) {
//                // Try to get player name as fallback
//                tabLines.add(playerInfo.getGameProfile().getName());
//            }
//        }
//
//        // Print the entire tab list to console and chat for debugging
//        if (!debugPrinted && !tabLines.isEmpty()) {
//            logDebug("====== TAB LIST (" + tabLines.size() + " lines) ======");
//            for (int i = 0; i < Math.min(30, tabLines.size()); i++) {
//                logDebug("Line " + i + ": " + tabLines.get(i));
//            }
//            debugPrinted = true;
//
//            // Use JS matching approach for direct comparison
//            for (int i = 0; i < tabLines.size(); i++) {
//                String line = tabLines.get(i);
//
//                if (line.contains("Commissions")) {
//                    logDebug("Found possible commission header at line " + i + ": " + line);
//
//                    // Check next 5 lines directly with the JS approach
//                    for (int j = 1; j <= 5; j++) {
//                        if (i+j < tabLines.size()) {
//                            String commissionLine = tabLines.get(i+j);
//                            logDebug("JS Check line " + (i+j) + ": " + commissionLine);
//
//                            // Try direct matching like in JS
//                            if (commissionLine.matches(".*§r §r§f.*: §r(§..*%|§aDONE)§r.*")) {
//                                String commission = commissionLine.substring(7, commissionLine.length()-2);
//                                logDebug("JS matched commission: " + commission);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        boolean showPowder = AllConfig.INSTANCE.MINING_CONFIGS.get("mining_show_mithril_powder").Data instanceof Boolean &&
//                (Boolean) AllConfig.INSTANCE.MINING_CONFIGS.get("mining_show_mithril_powder").Data;
//
//        // Original commission parsing logic
//        for (int i = 0; i < tabLines.size(); i++) {
//            String line = tabLines.get(i);
//
//            // Check for mithril powder info
//            if (showPowder) {
//                Matcher powderMatcher = MITHRIL_POWDER_PATTERN.matcher(line);
//                if (powderMatcher.find()) {
//                    powderText = line.substring(7, line.length() - 2);
//                    logDebug("Found powder: " + powderText);
//                    continue;
//                }
//            }
//
//            // Check for commissions section - adapt to match actual format found
//            if (line.contains("§r§9§lCommissions") || line.equals("§r§9§lCommissions:§r")) {
//                logDebug("Found exact commission header at line " + i);
//
//                // Look for up to 5 commission entries using direct matching like in JS
//                for (int j = 1; j <= 5; j++) {
//                    if (i + j < tabLines.size()) {
//                        String commissionLine = tabLines.get(i + j);
//                        logDebug("Checking line " + (i+j) + ": " + commissionLine);
//
//                        // Pattern match from the JavaScript code directly
//                        if (commissionLine.matches(".*§r §r§f.*: §r(§..*%|§aDONE)§r.*")) {
//                            try {
//                                String commission = commissionLine.substring(7, commissionLine.length() - 2);
//                                output.add(commission);
//                                logDebug("Added commission to output: " + commission);
//                            } catch (IndexOutOfBoundsException e) {
//                                logDebug("Error extracting commission text: " + e.getMessage());
//                            }
//                        } else {
//                            logDebug("Line didn't match commission pattern");
//                        }
//                    }
//                }
//                break;
//            }
//        }
//
//        renderText = output;
//    }
//
//    private void renderCommissions() {
//        ScaledResolution scaled = new ScaledResolution(mc);
//        FontRenderer fontRenderer = mc.fontRendererObj;
//
//        // Get position and scale from config
//        UIPosition pos = (UIPosition) AllConfig.INSTANCE.MINING_CONFIGS.get("mining_commissions_position").Data;
//        float scale = ((Number) AllConfig.INSTANCE.MINING_CONFIGS.get("mining_commissions_scale").Data).floatValue();
//        int colorIndex = ((Number) AllConfig.INSTANCE.MINING_CONFIGS.get("mining_commissions_color").Data).intValue() - 1;
//        colorIndex = Math.max(0, Math.min(colorIndex, COLORS.length - 1));
//
//        int x = pos.x;
//        int y = pos.y;
//
//        GlStateManager.pushMatrix();
//        GlStateManager.scale(scale, scale, 1.0F);
//
//        float scaleFactor = 1.0f / scale;
//        int scaledX = (int) (x * scaleFactor);
//        int scaledY = (int) (y * scaleFactor);
//
//        if (renderText.isEmpty()) {
//            fontRenderer.drawStringWithShadow("§cNo commissions available.", scaledX, scaledY, 0xFFFFFF);
//        } else {
//            String color = COLORS[colorIndex];
//            for (int i = 0; i < renderText.size(); i++) {
//                fontRenderer.drawStringWithShadow(
//                        "§7[" + color + "⥈§7] " + color + renderText.get(i),
//                        scaledX,
//                        scaledY + (i * 10),
//                        0xFFFFFF
//                );
//            }
//
//            boolean showPowder = AllConfig.INSTANCE.MINING_CONFIGS.get("mining_show_mithril_powder").Data instanceof Boolean &&
//                    (Boolean) AllConfig.INSTANCE.MINING_CONFIGS.get("mining_show_mithril_powder").Data;
//
//            if (showPowder && !powderText.isEmpty()) {
//                fontRenderer.drawStringWithShadow(
//                        "§7[" + color + "᠅§7] " + color + powderText,
//                        scaledX,
//                        scaledY + (renderText.size() * 10),
//                        0xFFFFFF
//                );
//            }
//        }
//
//        GlStateManager.popMatrix();
//    }
//
//    private boolean isModuleEnabled() {
//        return BooleanSettings.isEnabled("mining_commissions_display");
//    }
//}
