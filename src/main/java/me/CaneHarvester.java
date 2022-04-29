package me;

import me.config.Config;
import me.config.configTypes.JacobConfig;
import me.config.configTypes.MiscellaneousConfig;
import me.gui.GUI;
import me.gui.JellyGui.GuiComponents.GuiLineComponent;
import me.gui.JellyGui.GuiComponents.GuiMenuComponent;
import me.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = CaneHarvester.MODID, version = CaneHarvester.VERSION)
public class CaneHarvester {
    public static final String MODID = "scmath";
    public static final String NAME = "Cane Harvester";
    public static final String VERSION = "1.0";
    /*
     ** @author JellyLab
     */
    Minecraft mc = Minecraft.getMinecraft();


    public static boolean enabled = false;


    boolean locked = false;
    public static direction lastLaneDirection;
    public static direction currentDirection;
    public static boolean inFailsafe;
    public static boolean walkingForward;
    public static boolean pushedOff;
    public static boolean inTPPad;
    volatile static boolean error = false;
    volatile static boolean setcycled = false;
    volatile static boolean stuck = false;
    volatile static boolean rotating = false;
    volatile static boolean caged = false;
    volatile static boolean hubCaged;
    volatile static boolean selling;
    static boolean setspawnLag;


    public volatile static double beforeX = 0;
    public volatile static double beforeZ = 0;
    public volatile static double beforeY = 0;
    public volatile static double deltaX = 10000;
    public volatile static double deltaZ = 10000;
    public volatile static double deltaY = 0;
    public volatile static double initialX = 0;
    public volatile static double initialZ = 0;
    public static location currentLocation;

    public static boolean cookie = true;
    public static boolean godPot = true;
    public static IChatComponent header;
    public static IChatComponent footer;


    public static BlockPos targetBlockPos = new BlockPos(10000, 10000, 10000);


    public int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public int keyBindSneak = mc.gameSettings.keyBindSneak.getKeyCode();

    static volatile boolean bazaarLag = false;


    static KeyBinding[] customKeyBinds = new KeyBinding[2];
    List<GuiLineComponent> profitGuiDisplay;

    static volatile int totalSc = 0;
    static volatile int totalEsc = 0;
    static volatile int totalDEsc = 0;
    static volatile int totalMoney = 0;


    static volatile int lastCounter = 0;
    static volatile int moneypersec = 0;

    public static GuiMenuComponent profitGUI = new GuiMenuComponent(5, 5, 140, 0x70000000, null, null);

    long startTime = 0;
    long finalTime = 0;



    MouseHelper mouseHelper = new MouseHelper();
    static int playerYaw = 0;
    private static Logger logger;
    private static final Pattern PATTERN_ACTIVE_EFFECTS = Pattern.compile(
            "\u00a7r\u00a7r\u00a77You have a \u00a7r\u00a7cGod Potion \u00a7r\u00a77active! \u00a7r\u00a7d([0-9]*?:?[0-9]*?:?[0-9]*)\u00a7r");




    enum direction {
        RIGHT,
        LEFT,
        NONE //at the backmost lane of the farm
    }

    enum location {
        ISLAND,
        HUB,
        LOBBY
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CaneHarvester());
    }


    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        customKeyBinds[0] = new KeyBinding("Open GUI", Config.get("openguikey") instanceof Long ? ((Long) Config.get("openguikey")).intValue() : Config.get("openguikey"), "CaneHarvester");
        customKeyBinds[1] = new KeyBinding("Toggle script",Config.get("togglekey") instanceof Long ? ((Long) Config.get("togglekey")).intValue() : Config.get("togglekey"), "CaneHarvester");
        ClientRegistry.registerKeyBinding(customKeyBinds[0]);
        ClientRegistry.registerKeyBinding(customKeyBinds[1]);
    }


    @EventHandler
    public void init(FMLInitializationEvent event) {
        Config.init();
        GUI.init();

        ExecuteRunnable(checkPosChange);

    }




    @SubscribeEvent
    public void onOpenGui(final GuiOpenEvent event) {
        if (event.gui instanceof GuiDisconnected) {
            enabled = false;
        }
    }


    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {

        if (event.message.getFormattedText().contains("You were spawned in Limbo") && !inFailsafe && enabled) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 8, TimeUnit.SECONDS);
            Utils.sendWebhook("Limbo detected. Applying failsafe");

        }
        if ((event.message.getFormattedText().contains("Sending to server") && !inFailsafe && enabled)) {
            activateFailsafe();
            ScheduleRunnable(WarpHome, 10, TimeUnit.SECONDS);
            Utils.sendWebhook("Hub detected. Applying failsafe");
        }
        if ((event.message.getFormattedText().contains("DYNAMIC") || (event.message.getFormattedText().contains("Couldn't warp you")) || (event.message.getFormattedText().contains("sending commands too fast"))) && inFailsafe) {
            Utils.sendWebhook("Error while warping. Applying failsafe");
            error = true;
        }
        if ((event.message.getFormattedText().contains("SkyBlock Lobby") && !inFailsafe && enabled)) {
            Utils.sendWebhook("Lobby detected. Applying failsafe");
            activateFailsafe();
            ScheduleRunnable(Rejoin, 10, TimeUnit.SECONDS);
        }
        if (event.message.getFormattedText().contains("This server is too laggy")) {
            bazaarLag = true;
        }
        if (event.message.getFormattedText().contains("spawn location has been set")) {
            setspawnLag = false;
        }


    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {

        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            profitGUI.setLine(new GuiLineComponent(12,  EnumChatFormatting.DARK_GREEN + "« " + EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "Cane Harvester" + EnumChatFormatting.DARK_GREEN + " »", -1), 0);
            profitGUI.setLine(new GuiLineComponent(12,  Utils.formatInfo("Profit/hr", "$" + Utils.formatNumber(moneypersec * 60 * 60)), -1), 1);
            profitGUI.setLine(new GuiLineComponent(12, Utils.formatInfo("Profit/24hrs", "$" + Utils.formatNumber(moneypersec * 60 * 60 * 24)), -1), 2);
            profitGUI.setLine(new GuiLineComponent(12, Utils.formatInfo("Inventory price", "$" + Utils.formatNumber(totalMoney)), -1), 3);
            profitGUI.setLine(new GuiLineComponent(12,  Utils.formatInfo("Hoe counter",  Utils.formatNumber(SkyblockUtils.getHoeCounter())), -1), 4);
            if(mc.currentScreen == null) profitGUI.draw();

        }

    }
    @SubscribeEvent
    public void changeKeybind(GuiScreenEvent.KeyboardInputEvent.Post event)
    {
        if(event.gui instanceof GuiControls)
        {
            Config.set("openguikey", CaneHarvester.customKeyBinds[0].getKeyCode());
            Config.set("togglekey", CaneHarvester.customKeyBinds[1].getKeyCode());

        }
    }

    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {

        if (!rotating) {
            if (customKeyBinds[1].isPressed()) {


                if (!enabled) {
                    if (getLocation() == location.ISLAND) {
                        if(mc.thePlayer.inventoryContainer.inventorySlots.get(42).getStack() == null) {
                            Utils.addCustomChat("Starting script");
                            toggle();
                        } else
                            Utils.addCustomLog("Clear inventory slot 7");
                    } else
                        Utils.addCustomChat("Wrong location detected");

                } else
                    toggle();

            }
            if (customKeyBinds[0].isPressed()) {
                mc.displayGuiScreen(new GUI());
            }

        }
    }

    //
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void OnTickPlayer(TickEvent.ClientTickEvent event) { //Client -> player

        if (event.phase != TickEvent.Phase.START) return;



        // profit calculator && angle caculation
        if (mc.thePlayer != null && mc.theWorld != null) {

            currentLocation = getLocation();
            if (!rotating)
                playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;

            int tempEsc = 0;
            int tempDEsc = 0;
            int tempsc = 0;
            for (int i = 0; i < 35; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null) {
                    if (stack.getDisplayName().contains("Enchanted Sugar"))
                        tempEsc = tempEsc + stack.stackSize;

                    if (stack.getDisplayName().contains("Enchanted Sugar Cane"))
                        tempDEsc = tempDEsc + stack.stackSize;

                    if (stack.getItem().equals(Items.reeds))
                        tempsc = tempsc + stack.stackSize;
                }

            }
            totalDEsc = tempDEsc;
            totalEsc = tempEsc;
            totalSc = tempsc;
            totalMoney = tempDEsc * 51200 + tempEsc * 320 + tempsc * 2;
            if (caged) {
                if (currentLocation == location.HUB) {
                    if (!hubCaged) {
                        hubCaged = true;
                        Utils.addCustomLog("Bedrock cage - At hub, going to buy from bazaar");
                        ExecuteRunnable(hubCage);
                    }
                }
                return;
            }
            if(TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) > 1000){
                moneypersec = (int) ((SkyblockUtils.getHoeCounter() - lastCounter) / (160 * 160 * 1.0d) * 51200);
                lastCounter = SkyblockUtils.getHoeCounter();
                startTime = System.nanoTime();
            }
        }

        if (enabled && mc.thePlayer != null && mc.theWorld != null) {


            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu || mc.currentScreen instanceof GUI) {
                Utils.addCustomLog("In inventory/chat/pause, pausing");
                updateKeybinds(false, false, false, false);
                deltaX = 1000;
                deltaZ = 1000;
                return;
            }
            if(selling)
                return;
            if(setspawnLag)
                return;
            if (getLocation() == location.ISLAND && !selling) {
                if(!rotating){
                    try {
                        if(Config.<Boolean>get("jacob")) {
                            if (SkyblockUtils.getJacobEventCounter() > Integer.parseInt(Config.get("jacob"))) { //Jacob Config add back
                                ExecuteRunnable(JacobFailsafe);
                            }
                        }
                    }catch(Exception e){
                    }
                }

                //always
                Block blockIn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock();
                Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();

                double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
                double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
                double dy = Math.abs(mc.thePlayer.posY - mc.thePlayer.lastTickPosY);
                boolean falling = blockIn == Blocks.air && dy != 0;


                mc.gameSettings.pauseOnLostFocus = false;
                mc.thePlayer.inventory.currentItem = Utils.getHoeSlot();
                mc.gameSettings.gammaSetting = 100;



                //angles (locked)
                if (!inFailsafe) {
                    mc.thePlayer.rotationPitch = 0;
                    AngleUtils.hardRotate(playerYaw);
                    KeyBinding.setKeyBindState(keybindAttack, true);
                }
                //INITIALIZE
                if (!locked) {
                    initializeVaraibles();
                    Utils.addCustomLog("Going : " + calculateDirection());
                    locked = true;
                }

                //TP pad
                if (blockIn == Blocks.end_portal_frame && mc.thePlayer.posX != initialX && mc.thePlayer.posZ != initialZ) {
                    inTPPad = true;
                    Utils.addCustomChat("TP pad detected", EnumChatFormatting.BLUE);
                    ExecuteRunnable(changeLayer);
                }
                if (falling && !rotating && !inFailsafe &&
                        ((!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) && !BlockUtils.isWalkable(BlockUtils.getFrontBlock())) || (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getFrontBlock())))) {
                    Utils.addCustomChat("New layer detected", EnumChatFormatting.BLUE);
                    ExecuteRunnable(changeLayer);
                    enabled = false;

                }
                //antistuck
                if (deltaX < 0.2d && deltaZ < 0.2d && deltaY < 0.0001d && !inFailsafe && !stuck && !rotating) {
                    enabled = false;
                    stuck = true;
                    ExecuteRunnable(UnStuck);
                    unpressKeybinds();
                }

                //bedrock failsafe
                if (blockStandingOn == Blocks.bedrock && !inFailsafe && !caged && BlockUtils.bedrockCount() > 1) {
                    enabled = false;
                    KeyBinding.setKeyBindState(keybindAttack, false);
                    ScheduleRunnable(islandCage, 157, TimeUnit.MILLISECONDS);
                    inFailsafe = true;
                    caged = true;
                }

                //states
                if (dy == 0 && !inFailsafe && !stuck) {
                    if (!walkingForward) { //normal

                        KeyBinding.setKeyBindState(keyBindSneak, false);
                        if (currentDirection.equals(direction.RIGHT))
                            KeyBinding.setKeyBindState(keybindD, true);
                        else if (currentDirection.equals(direction.LEFT))
                            KeyBinding.setKeyBindState(keybindA, true);
                        else
                            walkingForward = true;
                    } else { // walking forward

                        //hole drop fix (prevent sneaking at the hole)
                        KeyBinding.setKeyBindState(keyBindSneak, !BlockUtils.isWalkable(blockStandingOn));

                        //unleash keys
                        if (lastLaneDirection.equals(direction.LEFT))
                            updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), false);
                        else
                            updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), false, mc.gameSettings.keyBindRight.isKeyDown());

                        //push keys so the next tick it will unleash
                        while (!pushedOff && !lastLaneDirection.equals(direction.NONE)) {
                            if (lastLaneDirection.equals(direction.LEFT)) {
                                Utils.addCustomLog("Bouncing to the right");
                                updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), true);
                            } else {
                                Utils.addCustomLog("Bouncing to the left");
                                updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), true, mc.gameSettings.keyBindRight.isKeyDown());
                            }
                            pushedOff = true;
                        }
                        KeyBinding.setKeyBindState(keybindW, true);
                    }
                }


                //change to walk forward
                if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0 && !inFailsafe && !rotating) {
                    if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) {
                        updateKeybinds(true, false, false, false);
                        walkingForward = true;
                        targetBlockPos = calculateTargetBlockPos();
                        Utils.addCustomLog("Target block : " + targetBlockPos.toString());
                        pushedOff = false;
                        initialX = mc.thePlayer.posX;
                        initialZ = mc.thePlayer.posZ;
                    }
                }

                //chagnge back to left/right
                if ((int)mc.thePlayer.posX == targetBlockPos.getX() && (int)mc.thePlayer.posZ == targetBlockPos.getZ() && walkingForward && Utils.isInCenterOfBlock()) {
                    updateKeybinds(false, false, false, false);
                    if(InventoryUtils.getFirstSlotStone() != -1 && Config.<Boolean>get("dropstone")){
                        activateFailsafe();
                        ExecuteRunnable(clearStone);
                        return;
                    }

                    setspawnLag = true;
                    mc.thePlayer.sendChatMessage("/setspawn");

                    initialX = mc.thePlayer.posX;
                    initialZ = mc.thePlayer.posZ;

                    if (!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(-2, 0))) {
                        //set last lane dir
                        currentDirection = direction.RIGHT;
                        lastLaneDirection = direction.RIGHT;
                    } else if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(2, 0))) {
                        currentDirection = direction.LEFT;
                        lastLaneDirection = direction.LEFT;
                    }

                    Utils.addCustomLog("Changing motion : Going " + currentDirection);
                    if(Config.<Boolean>get("resync"))
                        ScheduleRunnable(checkDensity, 2, TimeUnit.SECONDS);
                    ExecuteRunnable(checkFooter);
                    ExecuteRunnable(CheckFullInventory);
                    walkingForward = false;
                }
            } else {
                if(!caged) {
                    unpressKeybinds();
                }
            }
        } else {
            locked = false;
        }


    }
    //multi-threads

    Runnable JacobFailsafe = new Runnable() {
        @Override
        public void run() {
            try {
                Utils.addCustomChat("Crop limit reached. Preparing to warp to lobby...");
                int waitTime = SkyblockUtils.getRemainingJacobTime();
                Utils.sendWebhook("Crop limit reached. Waiting " + waitTime + " seconds");
                activateFailsafe();
                Utils.addCustomLog("Waiting " + waitTime + " seconds");
                ScheduleRunnable(LeaveSBIsand, waitTime, TimeUnit.SECONDS);
                Thread.sleep(1000);
                mc.thePlayer.sendChatMessage("/lobby");
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    };
    Runnable Resync = new Runnable() {
        @Override
        public void run() {
            try {
                Utils.addCustomChat("Initializing resync...");
                if (rotating || walkingForward || stuck || inFailsafe || currentDirection == direction.NONE) {
                    Utils.addCustomChat("Can't resync now");
                    return;
                }

                Utils.addCustomChat("Resyncing...");
                Utils.sendWebhook("Detected desync. Resyncing...");
                activateFailsafe();
                Thread.sleep(350);
                Thread.sleep(650);
                ScheduleRunnable(WarpHub, 3, TimeUnit.SECONDS);
            } catch (Exception e) {

            }
        }
    };

    Runnable checkPosChange = new Runnable() {
        @Override
        public void run() {
            try {
                deltaX = Math.abs(mc.thePlayer.posX - beforeX);
                deltaZ = Math.abs(mc.thePlayer.posZ - beforeZ);
                deltaY = Math.abs(mc.thePlayer.posY - beforeY);
                Thread.sleep(500);
                beforeX = mc.thePlayer.posX;
                beforeZ = mc.thePlayer.posZ;
                beforeY = mc.thePlayer.posY;
                Thread.sleep(7500);
            } catch(Exception e){
            } finally {
                ExecuteRunnable(checkPosChange);
            }
        }
    };

    Runnable changeLayer = new Runnable() {
        @Override
        public void run() {
            if (!inFailsafe) {
                try {
                    rotating = true;
                    unpressKeybinds();
                    enabled = false;
                    Thread.sleep(1000);
                    if (!inTPPad) {
                        playerYaw = Math.round(Math.abs(playerYaw - 180));
                        AngleUtils.smoothRotateClockwise(180);
                    }
                    Thread.sleep(5000);
                    rotating = false;
                    initializeVaraibles();
                    enabled = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Runnable checkDensity = () -> {
        if (stuck || inFailsafe || walkingForward)
            return;
        if(!selling) {
            Utils.addCustomLog("Checking density : " + getDensityPercentage(currentDirection));
            if (getDensityPercentage(currentDirection) > 49) {
                ExecuteRunnable(Resync);
            }
        }
    };


    Runnable LeaveSBIsand = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/l");
            ScheduleRunnable(Rejoin, 5, TimeUnit.SECONDS);
        }
    };
    Runnable WarpHub = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp hub");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable Rejoin = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/play sb");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable WarpHome = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp home");
            ScheduleRunnable(afterRejoin1, 3, TimeUnit.SECONDS);
        }
    };


    Runnable afterRejoin1 = new Runnable() {
        @Override
        public void run() {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            if (!error) {
                ScheduleRunnable(afterRejoin2, 1, TimeUnit.SECONDS);
            } else {
                Utils.addCustomLog("Error detected. Waiting 20 seconds");
                ScheduleRunnable(WarpHome, 20, TimeUnit.SECONDS);
                error = false;
            }

        }
    };
    Runnable afterRejoin2 = () -> {

        KeyBinding.setKeyBindState(keyBindSneak, false);


        mc.inGameHasFocus = true;
        mouseHelper.grabMouseCursor();
        mc.displayGuiScreen((GuiScreen) null);
        Field f = null;
        f = FieldUtils.getDeclaredField(mc.getClass(), "leftClickCounter", true);
        try {
            f.set(mc, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        enabled = true;
    };
    public Runnable CheckFullInventory = () -> {
        int count = 0;
        int total = 0;
        int elapsed = 0;
        Minecraft mc = Minecraft.getMinecraft();
        try {
            while (elapsed < 6000) {
                if (mc.thePlayer.inventory.getFirstEmptyStack() == -1) {
                    count++;
                }
                total++;
                elapsed += 10;
                Thread.sleep(10);
            }
            if (((float) count / total) > 0.60 && !selling && Config.<Boolean>get("autosell") && !inFailsafe && enabled) {
                selling = true;
                Utils.sendWebhook("Inventory full, Auto Selling!");
                ExecuteRunnable(autoSell);
                unpressKeybinds();
            } else {
               // checkFull = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };



    Runnable islandCage = () -> {
        try {
            Utils.addCustomLog("Cage detected");
            Utils.sendWebhook("Cage detected. Applying failsafes");
            Thread.sleep(400);
            updateKeybinds(false, false, false, false);
            Thread.sleep(800);
            updateKeybinds(false, false, true, false);
            AngleUtils.sineRotateCW(45, 0.4f);
            Thread.sleep(100);
            updateKeybinds(false, false, false, false);
            Thread.sleep(1500);
            AngleUtils.sineRotateACW(84, 0.5f);
            updateKeybinds(false, false, false, true);
            Thread.sleep(100);
            updateKeybinds(false, false, false, false);
            Thread.sleep(500);
            updateKeybinds(false, false, false, false);
            mc.thePlayer.sendChatMessage("/hub");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    Runnable hubCage = () -> {
        try {
            Utils.addCustomLog("Waiting till rotate head");
            Thread.sleep(4000);
            AngleUtils.smoothRotateAnticlockwise(77, 2);
            Thread.sleep(1000);
            updateKeybinds(true, false, false, false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            while (BlockUtils.getFrontBlock() != Blocks.spruce_stairs) {
                Utils.addCustomLog("Not reached bazaar");
                Thread.sleep(50);
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            updateKeybinds(false, false, false, false);
            KeyBinding.setKeyBindState(keyBindSneak, true);
            Thread.sleep(300);
            KeyBinding.setKeyBindState(keyBindSneak, false);
            bazaarLag = false;
            while (!(mc.thePlayer.openContainer instanceof ContainerChest) && !bazaarLag) {
                Utils.addCustomLog("Attempting to open gui");
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                Thread.sleep(600);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                Thread.sleep(600);
            }
            if (mc.thePlayer.openContainer instanceof ContainerChest) {
                InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, 0);
                Thread.sleep(1000);
                InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, 12);
                Thread.sleep(1000);
                InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, 10);
                Thread.sleep(1000);
                InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, 10);
                Thread.sleep(1000);
                InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, 12);
                Thread.sleep(1000);
                mc.thePlayer.closeScreen();
            }
            bazaarLag = false;
            Thread.sleep(3000);
            currentLocation = getLocation();
            while (currentLocation == location.HUB && caged) {
                mc.thePlayer.sendChatMessage("/is");
                ScheduleRunnable(afterRejoin1, 10, TimeUnit.SECONDS);
                caged = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };


    Runnable UnStuck = () -> {
        try {
            Utils.addCustomChat("Detected stuck");
            Utils.sendWebhook("Detected stuck");
            Utils.addCustomLog("DeltaX : " + deltaX + " DeltaZ : " + deltaZ);
            Utils.addCustomLog("BeforeX : " + beforeX + " BeforeZ : " + beforeZ);
            Utils.addCustomLog("BeforeX : " + " BeforeZ : " + beforeZ);
            Thread.sleep(100);
            KeyBinding.setKeyBindState(keybindD, true);
            Thread.sleep(200);
            KeyBinding.setKeyBindState(keybindD, false);
            KeyBinding.setKeyBindState(keybindA, true);
            Thread.sleep(200);
            KeyBinding.setKeyBindState(keybindA, false);
            KeyBinding.setKeyBindState(keybindS, true);
            Thread.sleep(200);
            KeyBinding.setKeyBindState(keybindS, false);
            KeyBinding.setKeyBindState(keybindW, true);
            Thread.sleep(200);
            KeyBinding.setKeyBindState(keybindW, false);
            deltaX = 100;
            deltaZ = 100;
            stuck = false;
            enabled = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    Runnable clearStone = () -> {
        try {
            int slotID = InventoryUtils.getFirstSlotStone();
            if(slotID != -1) {
                Utils.sendWebhook("Found stone. Attempting to remove it");
                Utils.addCustomLog("Found stone. Attempting to remove it");
                if (slotID < 9) {
                    slotID = 36 + slotID;
                }
                boolean right = false;
                Thread.sleep(500);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotID, 0, 0, mc.thePlayer);
                Thread.sleep(300);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotID, 0, 6, mc.thePlayer);
                Thread.sleep(300);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 35 + 7, 0, 0, mc.thePlayer);
                Thread.sleep(300);
                // mc.thePlayer.closeScreen();
                if (BlockUtils.isWalkable(BlockUtils.getRightBlock())) {
                    right = true;
                    AngleUtils.smoothRotateAnticlockwise(90, 2.5f);
                } else{
                    right = false;
                    AngleUtils.smoothRotateClockwise(90, 2.5f);
                }
                Thread.sleep(400);
                mc.thePlayer.inventory.currentItem = -1 + 7;
                Thread.sleep(400);
                mc.thePlayer.dropOneItem(true);
                Utils.addCustomLog("Dropped successfully");
                Thread.sleep(100);
                if (right) {
                    AngleUtils.smoothRotateClockwise(90, 2.5f);
                    Thread.sleep(1000);
                } else {
                    AngleUtils.smoothRotateAnticlockwise(90, 2.5f);
                    Thread.sleep(1000);
                }
                Utils.addCustomLog("Re-enabling script");
                deltaX = 100;
                deltaZ = 100;
                inFailsafe = false;
                stuck = false;
                enabled = true;
            } else {
                Utils.addCustomLog("No stone found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
    public static Runnable checkFooter = () -> {
        Utils.addCustomLog("Looking for godpot/cookie");
        boolean foundGodPot = false;
        boolean foundCookieText = false;
        if (footer != null) {
            String formatted = footer.getFormattedText();
            for (String line : formatted.split("\n")) {
                Matcher activeEffectsMatcher = PATTERN_ACTIVE_EFFECTS.matcher(line);
                if (activeEffectsMatcher.matches()) {
                    foundGodPot = true;
                } else if (line.contains("\u00a7d\u00a7lCookie Buff")) {
                    foundCookieText = true;
                } else if (foundCookieText && line.contains("Not active! Obtain")) {
                    Utils.addCustomLog("Cookie buff not active!");
                    foundCookieText = false;
                    cookie = false;
                } else if (foundCookieText) {
                    Utils.addCustomLog("Cookie active!");
                    foundCookieText = false;
                }
            }
            if (!foundGodPot) {
                Utils.addCustomLog("God pot buff not active!");
                godPot = false;
            } else {
                Utils.addCustomLog("God pot buff active!");
            }
        }
    };
    public static Runnable autoSell = () -> {
        try {

            Minecraft mc = Minecraft.getMinecraft();
            selling = true;
            int hoeSlot = mc.thePlayer.inventory.currentItem;

            Integer[] NPCSellSlots = {11, 16, 21, 23};
            Integer[] NPCSellSlotCounts = {0, 0, 0, 0};

            Integer[] BZSellSlots = {10, 13, 19};
            Integer[] BZSellSlotCounts = {0, 0, 0};

            if (!cookie) {
                Utils.addCustomLog("You need a cookie for auto sell!");
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                Thread.sleep(100);
                return;
            }

            sellInventory();

            if (InventoryUtils.findItemInventory("Large Enchanted Agronomy Sack") == -1) {
                Utils.addCustomLog("No sack detected, resuming");
                Thread.sleep(100);
                mc.thePlayer.inventory.currentItem = hoeSlot;
                Thread.sleep(100);
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                Thread.sleep(100);
                selling = false;
                return;
            }
            Utils.addCustomLog("Preparing to open sack");
            Thread.sleep(1000);
            PlayerUtils.openSack();

            Utils.addCustomLog("Counting");
            // Count all items in sack NPC
            for (int i = 0; i < NPCSellSlots.length; i++) {
                NPCSellSlotCounts[i] = InventoryUtils.countSack(NPCSellSlots[i]);
                Utils.addCustomLog("NPCSellSlotCount : " + NPCSellSlotCounts[i]);
            }


            // Count all items in sack BZ
            for (int i = 0; i < BZSellSlots.length; i++) {
                BZSellSlotCounts[i] = InventoryUtils.countSack(BZSellSlots[i]);
                Utils.addCustomLog("BazaarSellSlotCount : " + BZSellSlotCounts[i]);
            }

            // Claim items with counts
            for (int i = 0; i < NPCSellSlots.length; i++) {
                while (NPCSellSlotCounts[i] != 0) {
                    if (!(mc.currentScreen instanceof GuiContainer)) {
                        PlayerUtils.openSack();
                    }
                    while (mc.thePlayer.inventory.getFirstEmptyStack() != -1 && NPCSellSlotCounts[i] != 0) {
                        Utils.addCustomLog("Collecting");
                        InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, NPCSellSlots[i]);
                        InventoryUtils.waitForItem(NPCSellSlots[i], "");
                        Thread.sleep(100);
                        NPCSellSlotCounts[i] = InventoryUtils.countSack(NPCSellSlots[i]);
                    }
                    sellInventory();
                }

            }

            // If any remaining in sack, sell to bazaar
            for (int i = 0; i < BZSellSlots.length; i++) {
                if (BZSellSlotCounts[i] != 0) {
                    PlayerUtils.openBazaar();
                    InventoryUtils.waitForItemClick(11, "Selling whole inventory", 39, "Sell Sacks Now");
                    InventoryUtils.waitForItemClick(11, "Items sold!", 11, "Selling whole inventory");
                }
            }


            mc.thePlayer.closeScreen();
            mc.thePlayer.inventory.currentItem = hoeSlot;
            Thread.sleep(100);
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            Thread.sleep(100);
            selling = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static void sellInventory() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            Utils.addCustomLog("Selling Inventory");
            // Sell to NPC
            PlayerUtils.openTrades();
            Thread.sleep(500);
            for (int j = 0; j < 36; j++) {
                ItemStack sellStack = mc.thePlayer.inventory.getStackInSlot(j);
                if (sellStack != null) {
                    String name = sellStack.getDisplayName();
                    if (name.contains("Brown Mushroom") || name.contains("Enchanted Brown Mushroom") || name.contains("Brown Mushroom Block") || name.contains("Brown Enchanted Mushroom Block") ||
                            name.contains("Red Mushroom") || name.contains("Enchanted Red Mushroom") || name.contains("Red Mushroom Block") || name.contains("Red Enchanted Mushroom Block") ||
                            name.contains("Nether Wart") || name.contains("Enchanted Nether Wart") || name.contains("Mutant Nether Wart") ||
                            name.contains("Sugar Cane") || name.contains("Enchanted Sugar") || name.contains("Enchanted Sugar Cane") ||
                            name.contains("Stone")
                    ) {
                        if(!name.contains("Hoe")) {
                            Utils.addCustomLog("Found stack, selling");
                            InventoryUtils.clickWindow(mc.thePlayer.openContainer.windowId, (j < 9 ? j + 45 + 36 : j + 45));
                            Thread.sleep(200);
                        }
                    }
                }
                Thread.sleep(20);
            }
            mc.thePlayer.closeScreen();

            // Sell to Bazaar
            for (int j = 0; j < 36; j++) {
                ItemStack sellStack = mc.thePlayer.inventory.getStackInSlot(j);
                if (sellStack != null) {
                    String name = sellStack.getDisplayName();
                    if (name.contains("Carrot") && !name.contains("Hoe")) {
                        Utils.addCustomLog("Found carrots, selling");
                        PlayerUtils.openBazaar();
                        InventoryUtils.waitForItemClick(12, "Carrot", 0, "Farming");
                        InventoryUtils.waitForItemClick(29, "Sell Inventory Now", 12, "Carrot", "Enchanted");
                        InventoryUtils.waitForItemClick(11, "Selling whole inventory", 29, "Sell Inventory Now");
                        InventoryUtils.waitForItemClick(11, "Items sold!", 11, "Selling whole inventory");
                        Utils.addCustomLog("Successfully sold all carrots");
                    }
                    if (name.contains("Potato") && !name.contains("Hoe")) {
                        Utils.addCustomLog("Found potatoes, selling");
                        PlayerUtils.openBazaar();
                        InventoryUtils.waitForItemClick(13, "Potato", 0, "Farming");
                        InventoryUtils.waitForItemClick(29, "Sell Inventory Now", 13, "Potato", "Enchanted");
                        InventoryUtils.waitForItemClick(11, "Selling whole inventory", 29, "Sell Inventory Now");
                        InventoryUtils.waitForItemClick(11, "Items sold!", 11, "Selling whole inventory");
                        Utils.addCustomLog("Successfully sold all potatoes");
                    }
                    if ((name.contains("Wheat") || name.contains("Hay Bale") || name.contains("Bread")) && !name.contains("Hoe")) {
                        Utils.addCustomLog("Found wheat, selling");
                        PlayerUtils.openBazaar();
                        InventoryUtils.waitForItemClick(11, "Wheat & Seeds", 0, "Farming");
                        InventoryUtils.waitForItemClick(29, "Sell Inventory Now", 11, "Wheat & Seeds");
                        InventoryUtils.waitForItemClick(11, "Selling whole inventory", 29, "Sell Inventory Now");
                        InventoryUtils.waitForItemClick(11, "Items sold!", 11, "Selling whole inventory");
                        Utils.addCustomLog("Successfully sold all wheat");
                    }
                }
                Thread.sleep(20);
            }
            mc.thePlayer.closeScreen();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    void toggle() {

        mc.thePlayer.closeScreen();
        if (enabled) {
            Utils.addCustomChat("Stopped script");
            KeyBinding.setKeyBindState(keybindAttack, false);
            KeyBinding.setKeyBindState(keyBindSneak, false);
            unpressKeybinds();
        }
        enabled = !enabled;
        currentDirection = calculateDirection();
    }


    void unpressKeybinds() {
        updateKeybinds(false, false, false, false);
        KeyBinding.setKeyBindState(keyBindSneak, false);
    }

    void activateFailsafe() {
        inFailsafe = true;
        stuck = false;
        walkingForward = false;
        enabled = false;
        unpressKeybinds();
    }

    void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    void ExecuteRunnable(Runnable r) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.execute(r);
        eTemp.shutdown();
    }



    void updateKeybinds(boolean forward, boolean backward, boolean left, boolean right) {
        KeyBinding.setKeyBindState(keybindW, forward);
        KeyBinding.setKeyBindState(keybindA, left);
        KeyBinding.setKeyBindState(keybindD, right);
        KeyBinding.setKeyBindState(keybindS, backward);
    }

    location getLocation() {
        for (String line : SkyblockUtils.getSidebarLines()) {
            String cleanedLine = SkyblockUtils.cleanSB(line);
            if (cleanedLine.contains("Village") || cleanedLine.contains("Bazaar")) {
                return location.HUB;
            } else if (cleanedLine.contains("Island")) {
                return location.ISLAND;
            }
        }
        return location.LOBBY;
    }

    BlockPos calculateTargetBlockPos(){
        if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
            if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
                return BlockUtils.getBlockPosAround(0, 1, 0);
            } else {
                if(!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0))) {
                    Utils.addCustomLog("Detected one block off");
                    return BlockUtils.getBlockPosAround(0, 5, 0);
                }
                else {
                    return BlockUtils.getBlockPosAround(0, 6, 0);
                }

            }
        }

        Utils.addCustomLog("can't calculate target block!");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);


    }

    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.size() > 1 && Utils.arrayHasPosAndNeg(unwalkableBlocks)) {
            return direction.NONE;
        } else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }

    boolean shouldWalkForward() {
        return (BlockUtils.isWalkable(BlockUtils.getBackBlock()) && BlockUtils.isWalkable(BlockUtils.getFrontBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock()));
    }




    void initializeVaraibles() {
        deltaX = 10000;
        deltaZ = 10000;
        deltaY = 0;

        pushedOff = false;
        lastLaneDirection = calculateDirection();
        currentDirection = calculateDirection();
        inTPPad = false;
        setcycled = false;
        inFailsafe = false;
        walkingForward = false;
        beforeX = mc.thePlayer.posX;
        beforeZ = mc.thePlayer.posZ;
        initialX = mc.thePlayer.posX;
        initialZ = mc.thePlayer.posZ;
        rotating = false;
        bazaarLag = false;
        caged = false;
        hubCaged = false;
        selling = false;
        cookie = true;
        godPot = true;

        setspawnLag = false;
    }


    int getDensityPercentage(direction oppositeDir) {
        try {
            ArrayList<Block> blocks = new ArrayList<>();
            if (oppositeDir == direction.LEFT) {
                for (int i = 3; i < 6; i++)
                    blocks.add(BlockUtils.getBlockAround(i, 0, 1));
            } else {
                for (int i = 3; i < 6; i++)
                    blocks.add(BlockUtils.getBlockAround(-i, 0, 1));
            }

            int totalBlock = blocks.size();
            int totalSugarcaneBlock = 0;
            for (Block block : blocks) {
                if (block.equals(Blocks.reeds))
                    totalSugarcaneBlock++;
            }
            if(totalSugarcaneBlock == 0 || totalBlock == 0)
                return 0;

            return (int) (totalSugarcaneBlock / (totalBlock * 1.0d) * 100);
        } catch (Exception e) {

        }
        return -1;

    }

    void reconnect() {
        FMLClientHandler.instance().connectToServer((GuiScreen)new GuiMultiplayer((GuiScreen)new GuiMainMenu()), new ServerData("Hypixel", "mc.hypixel.net", false));
    }


}

