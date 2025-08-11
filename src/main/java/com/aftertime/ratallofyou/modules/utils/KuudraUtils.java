//import ibxm.Player;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.settings.KeyBinding;
//import net.minecraft.entity.item.EntityArmorStand;
//import net.minecraft.entity.monster.EntityZombie;
//import net.minecraft.network.play.client.*;
//import net.minecraft.network.play.server.*;
//import net.minecraft.tileentity.TileEntitySkull;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.EnumParticleTypes;
//import net.minecraft.util.Vec3;
//import net.minecraftforge.client.event.MouseEvent;
//import net.minecraftforge.fml.common.gameevent.InputEvent;
//import org.lwjgl.input.Keyboard;
//
//import java.util.Base64;
//import java.util.List;
//
//public class Utils {
//    // Constants
//    public static final int RIGHT_CLICK = -99;
//    public static final String COAL_BLOCK = "minecraft:coal_block";
//    public static final String RED_STAINED_CLAY = "minecraft:stained_hardened_clay[color=red]";
//    public static final String[] returnOutRoomNames = {"No room found.", "Fairy", "Entrance", "Blood"};
//    public static final String[] rushSkullOwners = {
//            "http://textures.minecraft.net/texture/3bcbbf94d603743a1e7147026e1c1240bd98fe87cc4ef04dcab51a31c30914fd",
//            "http://textures.minecraft.net/texture/9d9d80b79442cf1a3afeaa237bd6adaaacab0c28830fb36b5704cf4d9f5937c4"
//    };
//    public static final String Prefix = "§1[§9tsPMO§1]§8";
//
//    // Key bindings
//    public static final int JUMP = Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode();
//    public static final int useItem = Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode();
//    public static final int SNEAK = Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode();
//    public static final KeyBinding sneakKeybind = new KeyBinding(Minecraft.getMinecraft().gameSettings.keyBindSneak);
//    public static final int[] movementKeys = {
//            Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(),
//            Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(),
//            Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(),
//            Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode()
//    };
//
//    // Packet sending
//    public static void sendWindowClick(int windowId, int slot, int clickType, int actionNumber) {
//        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
//                windowId != 0 ? windowId : Player.getContainer().getWindowId(),
//                slot,
//                clickType != 0 ? clickType : 0,
//                0,
//                null,
//                actionNumber
//        ));
//    }
//
//    // Distance calculations
//    public static double getDistance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
//        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
//    }
//
//    public static double getDistance2D(double x1, double z1, double x2, double z2) {
//        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2);
//    }
//
//    public static double getViewDistance3D(double x, double y, double z) {
//        return Math.sqrt(
//                Math.pow(x - Player.getX(), 2) +
//                        Math.pow(y - (Player.getY() + Player.getPlayer().getEyeHeight()), 2) +
//                        Math.pow(z - Player.getZ(), 2)
//        );
//    }
//
//    // Inventory utilities
//    public static int getItemSlotFromName(String itemName) {
//        List<ItemStack> items = Player.getInventory().getItems().subList(0, 9);
//        for (int i = 0; i < items.size(); i++) {
//            if (items.get(i) != null && items.get(i).getDisplayName().contains(itemName)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public static void swapToItem(String itemName) {
//        int itemSlot = getItemSlotFromName(itemName);
//        if (itemSlot == -1 || Player.getHeldItemIndex() == itemSlot) return;
//        Player.setHeldItemIndex(itemSlot);
//    }
//
//    // String utilities
//    public static Double strToDec(String str) {
//        try {
//            return Double.parseDouble(str);
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }
//
//    // Position utilities
//    public static boolean isPlayerInBox(double x1, double y1, double z1, double x2, double y2, double z2) {
//        double x = Player.getX();
//        double y = Player.getY();
//        double z = Player.getZ();
//        return (x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
//                (y >= Math.min(y1, y2) && y <= Math.max(y1, y2) &&
//                        (z >= Math.min(z1, z2) && z <= Math.max(z1, z2));
//    }
//
//    public static boolean isPlayerInBox(double x1, double y1, double z1, double x2, double y2, double z2,
//                                        double x, double y, double z) {
//        return (x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
//                y >= Math.min(y1, y2) && y <= Math.max(y1, y2) &&
//                z >= Math.min(z1, z2) && z <= Math.max(z1, z2));
//    }
//
//    // Interaction utilities
//    public static void rightClick() {
//        try {
//            Method rightClickMethod = Minecraft.class.getDeclaredMethod("rightClickMouse");
//            rightClickMethod.setAccessible(true);
//            rightClickMethod.invoke(Minecraft.getMinecraft());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Phase detection
//    public static int getPhaseIn(EntityPlayer player) {
//        if (player == null) return 0;
//
//        double x = player.posX;
//        double y = player.posY;
//        double z = player.posZ;
//
//        if (isPlayerInBox(108, 100, 144, 18, 150, 121, x, y, z)) {
//            return 2; // p2
//        } else if (isPlayerInBox(112, 103, 29, 89, 148, 122, x, y, z)) {
//            return 1; // p1
//        } else if (isPlayerInBox(20, 144, 51, -3, 105, 142, x, y, z)) {
//            return 3; // p3
//        } else if (isPlayerInBox(94, 103, 26, -3, 158, 50, x, y, z) ||
//                isPlayerInBox(51, 124, 50, 60, 103, 55, x, y, z)) {
//            return 4; // p4
//        } else if (isPlayerInBox(68, 145, 55, 46, 107, 119, x, y, z)) {
//            return 5; // core
//        } else if (isPlayerInBox(112, 18, 19, 0, 2, 130, x, y, z)) {
//            return 6; // p5
//        } else {
//            return 0;
//        }
//    }
//
//    // Block utilities
//    public static boolean isBlockInFront(double multiplier, double yLevel) {
//        double rads = Player.getYaw() * Math.PI / 180;
//        double plusX = -Math.sin(rads);
//        double plusZ = Math.cos(rads);
//        double dZ = Player.getPlayer().motionZ * multiplier;
//        double dX = Player.getPlayer().motionX * multiplier;
//
//        Vec3 playerVec = new Vec3(Player.getX(), Player.getY() + yLevel, Player.getZ());
//        Vec3 bVec = new Vec3(Player.getX() + dX + plusZ, Player.getY() + yLevel, Player.getZ() + dZ + plusX);
//
//        return Minecraft.getMinecraft().theWorld.rayTraceBlocks(playerVec, bVec, false, true, false) != null;
//    }
//
//    public static boolean doorCheck(TileEntitySkull skull) {
//        int skullRotation = skull.getSkullRotation();
//        int dx = 0;
//        int dz = 0;
//
//        switch (skullRotation) {
//            case 0: // north
//                dz = 1;
//                break;
//            case 4: // east
//                dx = -1;
//                break;
//            case 8: // south
//                dz = -1;
//                break;
//            case 12: // west
//                dx = 1;
//                break;
//        }
//
//        if (dx == 0) {
//            return blockMetadata(skull.getPos().getX() + 1, skull.getPos().getY(), skull.getPos().getZ() + dz).equals(COAL_BLOCK) ||
//                    blockMetadata(skull.getPos().getX() - 1, skull.getPos().getY(), skull.getPos().getZ() + dz).equals(COAL_BLOCK) ||
//                    blockMetadata(skull.getPos().getX() + 1, skull.getPos().getY(), skull.getPos().getZ() + dz).equals(RED_STAINED_CLAY) ||
//                    blockMetadata(skull.getPos().getX() - 1, skull.getPos().getY(), skull.getPos().getZ() + dz).equals(RED_STAINED_CLAY);
//        } else if (dz == 0) {
//            return blockMetadata(skull.getPos().getX() + dx, skull.getPos().getY(), skull.getPos().getZ() + 1).equals(COAL_BLOCK) ||
//                    blockMetadata(skull.getPos().getX() + dx, skull.getPos().getY(), skull.getPos().getZ() - 1).equals(COAL_BLOCK) ||
//                    blockMetadata(skull.getPos().getX() + dx, skull.getPos().getY(), skull.getPos().getZ() + 1).equals(RED_STAINED_CLAY) ||
//                    blockMetadata(skull.getPos().getX() + dx, skull.getPos().getY(), skull.getPos().getZ() - 1).equals(RED_STAINED_CLAY);
//        }
//        return false;
//    }
//
//    public static String blockMetadata(int x, int y, int z) {
//        BlockPos block = new BlockPos(x, y, z);
//        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(block);
//        return blockState.toString();
//    }
//
//    // Player utilities
//    public static double[] calcYawPitch(Vec3 blcPos, Vec3 plrPos) {
//        if (plrPos == null) plrPos = getEyePos();
//
//        Vec3 d = new Vec3(
//                blcPos.xCoord - plrPos.xCoord,
//                blcPos.yCoord - plrPos.yCoord,
//                blcPos.zCoord - plrPos.zCoord
//        );
//
//        double yaw = 0;
//        double pitch = 0;
//
//        if (d.xCoord != 0) {
//            if (d.xCoord < 0) {
//                yaw = 1.5 * Math.PI;
//            } else {
//                yaw = 0.5 * Math.PI;
//            }
//            yaw = yaw - Math.atan(d.zCoord / d.xCoord);
//        } else if (d.zCoord < 0) {
//            yaw = Math.PI;
//        }
//
//        double dXZ = Math.sqrt(Math.pow(d.xCoord, 2) + Math.pow(d.zCoord, 2));
//        pitch = -Math.atan(d.yCoord / dXZ);
//        yaw = -yaw * 180 / Math.PI;
//        pitch = pitch * 180 / Math.PI;
//
//        if (pitch < -90 || pitch > 90 || Double.isNaN(yaw) || Double.isNaN(pitch)) {
//            return null;
//        }
//
//        return new double[]{yaw, pitch};
//    }
//
//    public static Vec3 getEyePos() {
//        return new Vec3(
//                Player.getX(),
//                Player.getY() + Player.getPlayer().getEyeHeight(),
//                Player.getZ()
//        );
//    }
//
//    public static void snapTo(double yaw, double pitch) {
//        EntityPlayer player = Player.getPlayer();
//        player.rotationYaw = (float) yaw;
//        player.rotationPitch = (float) pitch;
//    }
//
//    public static void setKeyState(int key, boolean state) {
//        KeyBinding.setKeyBindState(key, state);
//    }
//
//    public static double convertFixedPoint(int fixedValue, int n) {
//        return fixedValue / (1 << n);
//    }
//
//    // Player info utilities
//    public static String getClass(String player) {
//        int index = -1;
//        if (player instanceof EntityPlayer) {
//            index = TabList.getNames().indexOf(TabList.getNames().stream()
//                    .filter(line -> line.contains(((EntityPlayer) player).getName()))
//                    .findFirst()
//                    .orElse(null));
//        } else {
//            index = TabList.getNames().indexOf(TabList.getNames().stream()
//                    .filter(line -> line.contains(player))
//                    .findFirst()
//                    .orElse(null));
//        }
//
//        if (index == -1) return null;
//
//        String line = TabList.getNames().get(index);
//        line = ChatColor.stripColor(line);
//        java.util.regex.Matcher matcher = Pattern.compile(".+ \\((.+) .+\\)").matcher(line);
//
//        if (!matcher.find()) return "EMPTY";
//        return matcher.group(1);
//    }
//
//    public static String getNameByClass(String playerClass) {
//        int index = -1;
//        for (int i = 0; i < TabList.getNames().size(); i++) {
//            if (TabList.getNames().get(i).toLowerCase().contains(playerClass.toLowerCase())) {
//                index = i;
//                break;
//            }
//        }
//
//        if (index == -1) return null;
//
//        String line = TabList.getNames().get(index);
//        line = ChatColor.stripColor(line);
//        java.util.regex.Matcher matcher = Pattern.compile("(?:\\[\\d+\\]\\s*)?(.+?) \\((.+?)\\)").matcher(line);
//
//        if (!matcher.find()) return "EMPTY";
//        return removeUnicode(matcher.group(1)).trim();
//    }
//
//    // Movement utilities
//    public static void stopMovement() {
//        for (int key : movementKeys) {
//            setKeyState(key, false);
//        }
//        // sets motion x & z to 0 while keeping motionY
//        Player.getPlayer().motionX = 0;
//        Player.getPlayer().motionZ = 0;
//    }
//
//    public static void restartMovement() {
//        for (int key : movementKeys) {
//            setKeyState(key, Keyboard.isKeyDown(key));
//        }
//    }
//
//    // String utilities
//    public static String removeUnicode(String string) {
//        if (string == null) return "";
//        return string.replaceAll("[^\\u0000-\\u007F]", "");
//    }
//
//    public static int getItemSlot(String itemName) {
//        List<ItemStack> items = Player.getInventory().getItems();
//        for (int i = 0; i < items.size(); i++) {
//            if (items.get(i) != null &&
//                    ChatColor.stripColor(items.get(i).getDisplayName()).toLowerCase()
//                            .contains(itemName.toLowerCase().trim())) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    // Coordinate utilities
//    public static double[] centerCoords(double[] blockCoords) {
//        return new double[]{
//                Math.floor(blockCoords[0]) + (Math.signum(blockCoords[0]) == 1 ? -0.5 : 0.5),
//                Math.floor(blockCoords[1]),
//                Math.floor(blockCoords[2]) + (Math.signum(blockCoords[2]) == 1 ? -0.5 : 0.5)
//        };
//    }
//}