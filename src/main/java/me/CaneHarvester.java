package me;

import me.config.Config;
import me.gui.GUI;
import me.utils.Utils;
import me.webhook.DiscordWebhook;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
import scala.Int;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    public static boolean openedGUI = false;

    public int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public int keyBindSneak = mc.gameSettings.keyBindSneak.getKeyCode();


    static KeyBinding[] customKeyBinds = new KeyBinding[2];

    static volatile int totalSc = 0;
    static volatile int totalEsc = 0;
    static volatile int totalDEsc = 0;
    static volatile int totalMoney = 0;
    static volatile int prevMoney = -999;


    static volatile int moneyper10sec = 0;

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
        ExecuteRunnable(checkPriceChange);
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


    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            Utils.drawStringWithShadow(
                    EnumChatFormatting.GRAY + "--" + EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "PROFIT CALCULATOR" + EnumChatFormatting.GRAY + "--", 4, 25, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Profit/min : " + EnumChatFormatting.GOLD + "$" + Utils.formatNumber(moneyper10sec * 6), 4, 40, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Profit/hr : " + EnumChatFormatting.GOLD + "$" + Utils.formatNumber(moneyper10sec * 6 * 60), 4, 50, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Profit/12hr : " + EnumChatFormatting.GOLD + "$" + Utils.formatNumber(moneyper10sec * 6 * 60 * 12), 4, 60, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Profit/24hr : " + EnumChatFormatting.GOLD + "$" + Utils.formatNumber(moneyper10sec * 6 * 60 * 24), 4, 70, 0.8f, -1);

            Utils.drawStringWithShadow(
                    EnumChatFormatting.GRAY + "--" + EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "INVENTORY INFORMATION" + EnumChatFormatting.GRAY + "--", 4, 95, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Enchanted sugar : " + EnumChatFormatting.GREEN + Utils.formatNumber(totalEsc), 4, 110, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Enchanted sugar cane : " + EnumChatFormatting.GREEN + Utils.formatNumber(totalDEsc), 4, 120, 0.8f, -1);
            Utils.drawStringWithShadow(
                    EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Total inventory price : " + EnumChatFormatting.GREEN + "$" + Utils.formatNumber(totalMoney), 4, 130, 0.8f, -1);


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
                Utils.addCustomLog(Integer.toString(getJacobEventCounter()));
              // mc.displayGuiScreen(new GUI());
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

        }

        //script code
        if (enabled && mc.thePlayer != null && mc.theWorld != null) {

            if (getLocation() == location.ISLAND) {

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

                if (blockStandingOn == Blocks.bedrock) {
                    KeyBinding.setKeyBindState(keybindAttack, false);

                    ScheduleRunnable(EMERGENCY, 200, TimeUnit.MILLISECONDS);
                    inFailsafe = true;

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
                unpressKeybinds();

            }
        } else {
            locked = false;
        }


    }


    //multi-threads

    Runnable checkPriceChange = new Runnable() {
        @Override
        public void run() {

            if (!(prevMoney == -999) && (totalMoney - prevMoney >= 0)) {
                moneyper10sec = totalMoney - prevMoney;
            }
            prevMoney = totalMoney;
            ScheduleRunnable(checkPriceChange, 10, TimeUnit.SECONDS);
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

    Runnable EMERGENCY = new Runnable() {
        @Override
        public void run() {

            KeyBinding.setKeyBindState(keybindAttack, false);
            KeyBinding.setKeyBindState(keybindA, false);
            KeyBinding.setKeyBindState(keybindW, false);
            KeyBinding.setKeyBindState(keybindD, false);
            KeyBinding.setKeyBindState(keybindS, false);
            Utils.sendWebhook("Cage detected. Applying failsafe and closing minecraft");
            ScheduleRunnable(SHUTDOWN, 4123, TimeUnit.MILLISECONDS);
        }
    };

    Runnable SHUTDOWN = () -> mc.shutdown();

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


    void toggle() {

        mc.thePlayer.closeScreen();
        if (enabled) {
            Utils.addCustomChat("Stopped script");
            KeyBinding.setKeyBindState(keybindAttack, false);
            KeyBinding.setKeyBindState(keyBindSneak, false);
            unpressKeybinds();
        }
        enabled = !enabled;
        openedGUI = false;
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
            if (cleanedLine.contains("Village")) {
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
}
