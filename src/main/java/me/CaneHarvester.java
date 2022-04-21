package me;

import me.config.Config;
import me.gui.GUI;
import me.gui.GuiDraggableComponent;
import me.gui.GuiLineComponent;
import me.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
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

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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


    public volatile static double beforeX = 0;
    public volatile static double beforeZ = 0;
    public volatile static double beforeY = 0;
    public volatile static double deltaX = 10000;
    public volatile static double deltaZ = 10000;
    public volatile static double deltaY = 0;
    public volatile static double initialX = 0;
    public volatile static double initialZ = 0;
    public static float walkForwardDis = 5.9f;
    public static location currentLocation;


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

    long startTime = 0;
    long finalTime = 0;

    MouseHelper mouseHelper = new MouseHelper();
    static int playerYaw = 0;
    private static Logger logger;




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

    }


    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new CaneHarvester());
    }


    @EventHandler
    public void init(FMLInitializationEvent event) {
        customKeyBinds[0] = new KeyBinding("Open GUI", Keyboard.KEY_RSHIFT, "CaneHarvester");
        customKeyBinds[1] = new KeyBinding("Toggle script", Keyboard.KEY_GRAVE, "CaneHarvester");
        ClientRegistry.registerKeyBinding(customKeyBinds[0]);
        ClientRegistry.registerKeyBinding(customKeyBinds[1]);
        try{
            Config.readConfig();
        }catch(Exception e){
            Config.writeConfig();
        }

        GUI.draggableProfitGUI.addLine(null);
        GUI.draggableProfitGUI.addLine(null);
        GUI.draggableProfitGUI.addLine(null);
        GUI.draggableProfitGUI.addLine(null);
        GUI.draggableProfitGUI.addLine(null);
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
        if ((event.message.getFormattedText().contains("DYNAMIC") || (event.message.getFormattedText().contains("Couldn't warp you")) && inFailsafe)) {
            Utils.sendWebhook("Error while warping. Applying failsafe");
            error = true;
        }
        if ((event.message.getFormattedText().contains("SkyBlock Lobby") && !inFailsafe && enabled)) {
            Utils.sendWebhook("Lobby detected. Applying failsafe");
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 10, TimeUnit.SECONDS);
        }
        if (event.message.getFormattedText().contains("This server is too laggy")) {
            bazaarLag = true;
        }


    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {

        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {


          /*  GUI.drawRect(0,  2, 200, 77, new Color(0, 0, 0, 100).getRGB());
            Utils.drawStringWithShadow(
                    EnumChatFormatting.DARK_GREEN + "« " + EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "Cane Harvester" + EnumChatFormatting.DARK_GREEN + " »", 5, 5, 1.2f, -1);
            Utils.drawInfo("Profit/hr", "$" + Utils.formatNumber(moneypersec * 60 * 60), 20);
            Utils.drawInfo("Profit/24hrs", "$" + Utils.formatNumber(moneypersec * 60 * 60 * 24), 35);
            Utils.drawInfo("Inventory price", "$" + Utils.formatNumber(totalMoney), 50);
            Utils.drawInfo("Hoe counter", Utils.formatNumber(getHoeCounter()), 65);*/

            GUI.draggableProfitGUI.setLine(new GuiLineComponent(5, 3, EnumChatFormatting.DARK_GREEN + "« " + EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "Cane Harvester" + EnumChatFormatting.DARK_GREEN + " »", -1, 1.2f), 0);
            GUI.draggableProfitGUI.setLine(new GuiLineComponent(5, 18, Utils.formatInfo("Profit/hr", "$" + Utils.formatNumber(moneypersec * 60 * 60)), -1, 1), 1);
            GUI.draggableProfitGUI.setLine(new GuiLineComponent(5, 33, Utils.formatInfo("Profit/24hrs", "$" + Utils.formatNumber(moneypersec * 60 * 60 * 24)), -1, 1), 2);
            GUI.draggableProfitGUI.setLine(new GuiLineComponent(5, 48, Utils.formatInfo("Inventory price", "$" + Utils.formatNumber(totalMoney)), -1, 1), 3);
            GUI.draggableProfitGUI.setLine(new GuiLineComponent(5, 63, Utils.formatInfo("Hoe counter", "$" + Utils.formatNumber(getHoeCounter())), -1, 1), 4);
            GUI.draggableProfitGUI.draw();

        }

    }

    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {

        if (!rotating) {
            if (customKeyBinds[1].isPressed()) {


                if (!enabled) {
                    if (getLocation() == location.ISLAND) {
                        Utils.addCustomChat("Starting script");
                        toggle();
                    } else {
                        Utils.addCustomChat("Wrong location detected");
                    }
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
                playerYaw = Math.round(Utils.get360RotationYaw() / 90) < 4 ? Math.round(Utils.get360RotationYaw() / 90) * 90 : 0;

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
                moneypersec = (int) ((getHoeCounter() - lastCounter) / (160 * 160 * 1.0d) * 51200);
                lastCounter = getHoeCounter();
                startTime = System.nanoTime();
            }
        }

        //script code
        if (enabled && mc.thePlayer != null && mc.theWorld != null) {


            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu || mc.currentScreen instanceof GUI) {
                Utils.addCustomLog("In inventory/chat/pause, pausing");
                updateKeybinds(false, false, false, false);
                deltaX = 1000;
                deltaZ = 1000;
                return;
            }
            if (getLocation() == location.ISLAND) {
                if(!rotating){
                    try {
                        if(Integer.parseInt(Config.jacobThreshold) != 0) {
                            if (getJacobEventCounter() > Integer.parseInt(Config.jacobThreshold)) {
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
                mc.thePlayer.inventory.currentItem = 0;
                mc.gameSettings.gammaSetting = 100;



                //angles (locked)
                if (!inFailsafe) {
                    mc.thePlayer.rotationPitch = 0;
                    Utils.hardRotate(playerYaw);
                    KeyBinding.setKeyBindState(keybindAttack, true);
                }
                //INITIALIZE
                if (!locked) {
                    initializeVaraibles();
                    Utils.addCustomLog("Going : " + calculateDirection());
                    walkForwardDis = calculateDirection() == direction.NONE ? 1.1f : 5.9f;
                    locked = true;
                }

                //TP pad
                if (blockIn == Blocks.end_portal_frame && mc.thePlayer.posX != initialX && mc.thePlayer.posZ != initialZ) {
                    inTPPad = true;
                    Utils.addCustomChat("TP pad detected", EnumChatFormatting.BLUE);
                    ExecuteRunnable(changeLayer);
                }
                if (falling && !rotating && !inFailsafe &&
                        ((!Utils.isWalkable(Utils.getLeftBlock()) && !Utils.isWalkable(Utils.getFrontBlock())) || (!Utils.isWalkable(Utils.getRightBlock()) && !Utils.isWalkable(Utils.getFrontBlock())))) {
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
                if (blockStandingOn == Blocks.bedrock && !inFailsafe && !caged && bedrockCount() > 1) {
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
                        KeyBinding.setKeyBindState(keyBindSneak, !Utils.isWalkable(blockStandingOn));

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


                        walkForwardDis = calculateDirection() == direction.NONE ? 1.1f : 5.9f;
                        Utils.addCustomLog("Walking forward, walking dis = " + walkForwardDis);

                        pushedOff = false;
                        initialX = mc.thePlayer.posX;
                        initialZ = mc.thePlayer.posZ;
                    }
                }

                //chagnge back to left/right
                if ((Math.abs(initialX - mc.thePlayer.posX) > walkForwardDis || Math.abs(initialZ - mc.thePlayer.posZ) > walkForwardDis) && walkingForward) {

                    mc.thePlayer.sendChatMessage("/setspawn");
                    if (!Utils.isWalkable(Utils.getLeftBlock()) || !Utils.isWalkable(Utils.getBlockAround(-2, 0))) {
                        //set last lane dir
                        currentDirection = direction.RIGHT;
                        lastLaneDirection = direction.RIGHT;
                        updateKeybinds(false, false, false, true);
                    } else if (!Utils.isWalkable(Utils.getRightBlock()) || !Utils.isWalkable(Utils.getBlockAround(2, 0))) {
                        currentDirection = direction.LEFT;
                        lastLaneDirection = direction.LEFT;
                        updateKeybinds(false, false, true, false);
                    }

                    Utils.addCustomLog("Changing motion : Going " + currentDirection);
                    ScheduleRunnable(PressS, 200, TimeUnit.MILLISECONDS);
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
                int waitTime = getRemainingJacobTime();
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
    Runnable reSync = new Runnable() {
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
                Thread.sleep(350);
                activateFailsafe();
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
                        Utils.smoothRotateClockwise(180);
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


    Runnable PressS = new Runnable() {
        @Override
        public void run() {

            if (stuck || inFailsafe || walkingForward)
                return;
            try {
                do {
                    Utils.addCustomLog("Pressing S");
                    updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), true, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown());
                    Thread.sleep(50);
                }
                while (Utils.isWalkable(Utils.getBackBlock()) && (!Utils.isWalkable(Utils.getFrontBlock()) || !Utils.isWalkable(Utils.getBlockAround(0, 2))));

                updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown());
                ScheduleRunnable(checkDensity, 2, TimeUnit.SECONDS);


            } catch (Exception e) {
                e.printStackTrace();

            }

        }
    };

    Runnable checkDensity = () -> {
        Utils.addCustomLog("Checking density : " + getDensityPercentage(currentDirection));
        if(getDensityPercentage(currentDirection) > 49){
            ExecuteRunnable(reSync);
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


    Runnable islandCage = () -> {
        try {
            Utils.addCustomLog("Cage detected");
            Utils.sendWebhook("Cage detected. Applying failsafes");
            Thread.sleep(400);
            updateKeybinds(false, false, false, false);
            Thread.sleep(800);
            updateKeybinds(false, false, true, false);
            Utils.sineRotateCW(45, 0.4);
            Thread.sleep(100);
            updateKeybinds(false, false, false, false);
            Thread.sleep(1500);
            Utils.sineRotateAWC(84, 0.5);
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
            Utils.smoothRotateAnticlockwise(77, 2);
            Thread.sleep(1000);
            updateKeybinds(true, false, false, false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            while (Utils.getFrontBlock() != Blocks.spruce_stairs) {
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
                clickWindow(mc.thePlayer.openContainer.windowId, 0);
                Thread.sleep(1000);
                clickWindow(mc.thePlayer.openContainer.windowId, 12);
                Thread.sleep(1000);
                clickWindow(mc.thePlayer.openContainer.windowId, 10);
                Thread.sleep(1000);
                clickWindow(mc.thePlayer.openContainer.windowId, 10);
                Thread.sleep(1000);
                clickWindow(mc.thePlayer.openContainer.windowId, 12);
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

    int bedrockCount() {
        int r = 4;
        int count = 0;
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3i vec3i = new Vec3i(r, r, r);
        Vec3i vec3i2 = new Vec3i(r, r, r);
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i2))) {
            IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.bedrock) {
                count++;
            }
        }
        Utils.addCustomLog("Counted bedrock: " + count);
        return count;
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
        for (String line : Utils.getSidebarLines()) {
            String cleanedLine = Utils.cleanSB(line);
            if (cleanedLine.contains("Village") || cleanedLine.contains("Bazaar")) {
                return location.HUB;
            } else if (cleanedLine.contains("Island")) {
                return location.ISLAND;
            }
        }
        return location.LOBBY;
    }

    int getJacobEventCounter(){
        try {
            for (String line : Utils.getSidebarLines()) {
                String cleanedLine = Utils.cleanSB(line);
                if (cleanedLine.contains("with")) {
                    return Integer.parseInt(cleanedLine.substring(cleanedLine.lastIndexOf(" ") + 1).replace(",", ""));
                }

            }
        }catch(Exception e) {
        }
        return 0;
    }

    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!Utils.isWalkable(Utils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!Utils.isWalkable(Utils.getBlockAround(i, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.size() > 1 && hasPosAndNeg(unwalkableBlocks)) {
            return direction.NONE;
        } else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }

    boolean shouldWalkForward() {
        return (Utils.isWalkable(Utils.getBackBlock()) && Utils.isWalkable(Utils.getFrontBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getLeftBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getLeftBlock()));
    }

    boolean hasPosAndNeg(ArrayList<Integer> ar) {
        boolean hasPos = false;
        boolean hasNeg = false;
        for (Integer integer : ar) {
            if (integer < 0)
                hasNeg = true;
            else
                hasPos = true;
        }
        return hasPos && hasNeg;

    }

    int getDensityPercentage(direction oppositeDir) {
        try {
            ArrayList<Block> blocks = new ArrayList<>();
            if (oppositeDir == direction.LEFT) {
                for (int i = 3; i < 6; i++)
                    blocks.add(Utils.getBlockAround(i, 0, 1));
            } else {
                for (int i = 3; i < 6; i++)
                    blocks.add(Utils.getBlockAround(-i, 0, 1));
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
    void clickWindow(int windowID, int slotID) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.playerController.windowClick(windowID, slotID, 0, 0, mc.thePlayer);
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
        walkForwardDis = 5.9f;
        rotating = false;
        bazaarLag = false;
        caged = false;
        hubCaged = false;
    }
    int getHoeCounter() {
        try {
            if (mc.thePlayer.getHeldItem().getDisplayName().contains("Turing")) {
                final ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
                if (stack != null && stack.hasTagCompound()) {
                    final NBTTagCompound tag = stack.getTagCompound();
                    if (tag.hasKey("ExtraAttributes", 10)) {
                        final NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                        if (ea.hasKey("mined_crops", 99)) {
                            return ea.getInteger("mined_crops");
                        } else if (ea.hasKey("farmed_cultivating", 99)) {
                            return ea.getInteger("farmed_cultivating");
                        }
                    }
                }
            }
        } catch(Exception e){
        }
        return 0;
    }
    int getRemainingJacobTime(){
        try {
            String myData = "";
            for (String line : Utils.getSidebarLines()) {
                String cleanedLine = Utils.cleanSB(line);
                if (cleanedLine.contains("Sugar Cane")) {
                    myData = cleanedLine;
                }
            }
            myData = myData.substring(myData.lastIndexOf(" ") + 1);
            myData = myData.substring(0, myData.length() - 1);
            String[] time = myData.split("m");
            return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);

        }catch(Exception e) {
        }

        return 0;
    }
}

