package me;

import me.config.Config;
import me.gui.GUI;
import me.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = CaneHarvester.MODID, version = CaneHarvester.VERSION)
public class CaneHarvester {
    public static final String MODID = "nwmath";
    public static final String NAME = "Farm Helper";
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
    volatile static boolean error = false;
    volatile static boolean setcycled = false;
    volatile static boolean stuck = false;
    volatile static boolean rotating = false;


    volatile static double beforeX = 0;
    volatile static double beforeZ = 0;
    volatile static double beforeY = 0;
    volatile static double deltaX = 10000;
    volatile static double deltaZ = 10000;
    volatile static double deltaY = 0;
    volatile static double initialX = 0;
    volatile static double initialZ = 0;

    public static boolean openedGUI = false;

    public int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public int keyBindSneak = mc.gameSettings.keyBindSneak.getKeyCode();

    static KeyBinding[] customKeyBinds = new KeyBinding[2];

    static volatile int totalMnw = 0;
    static volatile int totalEnw = 0;
    static volatile int totalMoney = 0;
    static volatile int prevMoney = -999;
    static int cycles = 0;
    static volatile int moneyper10sec = 0;

    MouseHelper mouseHelper = new MouseHelper();
    static int playerYaw = 0;
    private static Logger logger;


    enum direction {
        RIGHT,
        LEFT,
        NONE
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

        }
        if ((event.message.getFormattedText().contains("Sending to server") && !inFailsafe && enabled)) {
            activateFailsafe();
            ScheduleRunnable(WarpHome, 10, TimeUnit.SECONDS);
        }
        if ((event.message.getFormattedText().contains("DYNAMIC") || (event.message.getFormattedText().contains("Couldn't warp you")) && inFailsafe)) {
            error = true;
        }
        if ((event.message.getFormattedText().contains("SkyBlock Lobby") && !inFailsafe && enabled)) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 10, TimeUnit.SECONDS);
        }


    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            mc.fontRendererObj.drawString("Angle : " + playerYaw, 4, 4, -1);
            mc.fontRendererObj.drawString("Minecraft yaw : " + mc.thePlayer.rotationYaw, 4, 16, -1);
            mc.fontRendererObj.drawString("KeyBindW : " + (mc.gameSettings.keyBindForward.isKeyDown() ? "Pressed" : "Not pressed"), 4, 28, -1);
            mc.fontRendererObj.drawString("KeyBindS : " + (mc.gameSettings.keyBindBack.isKeyDown() ? "Pressed" : "Not pressed"), 4, 40, -1);
            mc.fontRendererObj.drawString("KeyBindA : " + (mc.gameSettings.keyBindLeft.isKeyDown() ? "Pressed" : "Not pressed"), 4, 52, -1);
            mc.fontRendererObj.drawString("KeyBindD : " + (mc.gameSettings.keyBindRight.isKeyDown() ? "Pressed" : "Not pressed"), 4, 64, -1);
            mc.fontRendererObj.drawString("Walking forward : " + walkingForward, 4, 76, -1);

        }

    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void OnTickPlayer(TickEvent.ClientTickEvent event) { //Client -> player

        if (event.phase != TickEvent.Phase.START) return;


        // profit calculator && angle caculation
        if (mc.thePlayer != null && mc.theWorld != null) {
            if (!rotating)
                playerYaw = Math.round(Utils.get360RotationYaw() / 90) < 4 ? Math.round(Utils.get360RotationYaw() / 90) * 90 : 0;

        }

        //script code
        if (enabled && mc.thePlayer != null && mc.theWorld != null) {

            //always
            Block blockIn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock();

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
                locked = true;
                ScheduleRunnable(checkChange, 3, TimeUnit.SECONDS);
            }

            if (falling && !rotating && !inFailsafe && dx == 0 && dz == 0) {
                cycles = 0;
                Utils.addCustomChat("New layer detected", EnumChatFormatting.BLUE);
                ExecuteRunnable(changeLayer);
                enabled = false;

            }
            //antistuck
            if (deltaX < 0.5d && deltaZ < 0.5d && deltaY < 0.0001d && !inFailsafe && !stuck) {
                Utils.addCustomChat("Detected stuck");
                stuck = true;

                unpressKeybinds();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            KeyBinding.setKeyBindState(keybindD, true);
                            Thread.sleep(200);
                            KeyBinding.setKeyBindState(keybindD, false);
                            KeyBinding.setKeyBindState(keybindA, true);
                            Thread.sleep(200);
                            KeyBinding.setKeyBindState(keybindA, false);
                            if (shouldWalkForward()) {
                                Utils.addCustomLog("Restarting : Going forward");
                                lastLaneDirection = calculateDirection().equals(direction.LEFT) ? direction.RIGHT : direction.LEFT;
                                walkingForward = true;
                                initialX = mc.thePlayer.posX;
                                initialZ = mc.thePlayer.posZ;

                            } else {
                                currentDirection = direction.RIGHT;
                                lastLaneDirection = direction.LEFT;
                                walkingForward = false;
                                Utils.addCustomLog("Restarting : Going right");
                            }
                            stuck = false;
                            deltaX = 10000;
                            deltaZ = 10000;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }

            //bedrock failsafe
            Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
            if (blockStandingOn == Blocks.bedrock) {
                KeyBinding.setKeyBindState(keybindAttack, false);

                ScheduleRunnable(EMERGENCY, 200, TimeUnit.MILLISECONDS);
                inFailsafe = true;

            }

            //states
            if(dy == 0 && !inFailsafe && !stuck){
                if(!walkingForward) {
                    KeyBinding.setKeyBindState(keyBindSneak, false);
                    if (currentDirection.equals(direction.RIGHT))
                        KeyBinding.setKeyBindState(keybindD, true);
                    else if(currentDirection.equals(direction.LEFT))
                        KeyBinding.setKeyBindState(keybindA, true);
                } else{
                    KeyBinding.setKeyBindState(keyBindSneak, true);
                    if(lastLaneDirection.equals(direction.LEFT))
                        updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(),  false);
                    else
                        updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), false, mc.gameSettings.keyBindRight.isKeyDown());

                    while(!pushedOff && !lastLaneDirection.equals(direction.NONE))
                    {
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
            if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0 && !inFailsafe  && !rotating) {
                if (shouldWalkForward() && !walkingForward) {
                    updateKeybinds(true, false, false, false);
                    walkingForward = true;
                    Utils.addCustomLog("Walking forward");

                    pushedOff = false;
                    initialX = mc.thePlayer.posX;
                    initialZ = mc.thePlayer.posZ;
                }
            }
            //chagnge back to left/right
            if((Math.abs(initialX - mc.thePlayer.posX) > 5.75f || Math.abs(initialZ - mc.thePlayer.posZ) > 5.75f) && walkingForward) {
                if(lastLaneDirection == direction.LEFT) {
                    //set last lane dir
                    currentDirection = direction.RIGHT;
                    lastLaneDirection = direction.RIGHT;
                    updateKeybinds(false, false, false, true);
                }
                else {
                    currentDirection = direction.LEFT;
                    lastLaneDirection = direction.LEFT;
                    updateKeybinds(false, false, true, false);
                }
                Utils.addCustomLog("Changing motion : Going " + currentDirection);
                ScheduleRunnable(PressS, 200, TimeUnit.MILLISECONDS);
                walkingForward = false;
            }



            //resync
            if (cycles == 6 && Config.resync && !rotating)
                ExecuteRunnable(reSync);
            else if (cycles == 6 && Config.resync)
                cycles = 0;
        } else {
            locked = false;
        }


    }


    //multi-threads

    Runnable reSync = new Runnable() {
        @Override
        public void run() {
            try {
                cycles = 0;
                if (rotating) {
                    return;
                }
                Utils.addCustomChat("Resyncing...");
                Thread.sleep(350);
                activateFailsafe();
                Thread.sleep(650);
                ScheduleRunnable(WarpHub, 3, TimeUnit.SECONDS);
            } catch (Exception e) {

            }
        }
    };

    Runnable checkChange = new Runnable() {
        @Override
        public void run() {

            if (!inFailsafe && enabled) {
                deltaX = Math.abs(mc.thePlayer.posX - beforeX);
                deltaZ = Math.abs(mc.thePlayer.posZ - beforeZ);
                deltaY = Math.abs(mc.thePlayer.posY - beforeY);

                beforeX = mc.thePlayer.posX;
                beforeZ = mc.thePlayer.posZ;
                beforeY = mc.thePlayer.posY;

                ScheduleRunnable(checkChange, 5, TimeUnit.SECONDS);

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
                    playerYaw = Math.round(Math.abs(playerYaw - 180));
                    Utils.smoothRotateClockwise(180);
                    Thread.sleep(2000);
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
            if(stuck || inFailsafe || walkingForward)
                return;
            try {

                cycles ++;
                Utils.addCustomLog("Pressing S");
                updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), true, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown());
                Thread.sleep(300);
                mc.thePlayer.sendChatMessage("/setspawn");
                updateKeybinds(mc.gameSettings.keyBindForward.isKeyDown(), false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown());
            } catch (Exception e) {
                e.printStackTrace();

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
            ScheduleRunnable(WarpHome, 3, TimeUnit.SECONDS);
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
                ScheduleRunnable(WarpHome, 20, TimeUnit.SECONDS);
                error = false;
            }

        }
    };
    Runnable afterRejoin2 = new Runnable() {
        @Override
        public void run() {

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

            initializeVaraibles();

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

            ScheduleRunnable(checkChange, 3, TimeUnit.SECONDS);
        }
    };


    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {

        if (!rotating) {
            if (customKeyBinds[1].isPressed()) {
                if (!enabled)
                    Utils.addCustomChat("Starting script");

                toggle();
            }
            if (customKeyBinds[0].isPressed()) {
                mc.displayGuiScreen(new GUI());
            }


        }


    }

    Runnable EMERGENCY = new Runnable() {
        @Override
        public void run() {

            KeyBinding.setKeyBindState(keybindAttack, false);
            KeyBinding.setKeyBindState(keybindA, false);
            KeyBinding.setKeyBindState(keybindW, false);
            KeyBinding.setKeyBindState(keybindD, false);
            KeyBinding.setKeyBindState(keybindS, false);

            // mc.thePlayer.addChatMessage(ScreenShotHelper.saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer()));

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.schedule(SHUTDOWN, 4123, TimeUnit.MILLISECONDS);


        }
    };

    Runnable SHUTDOWN = new Runnable() {
        @Override
        public void run() {
            mc.shutdown();
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
    }

    void activateFailsafe() {
        inFailsafe = true;
        stuck = false;
        walkingForward = false;
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
        setcycled = false;
        inFailsafe = false;
        walkingForward = false;
        beforeX = mc.thePlayer.posX;
        beforeZ = mc.thePlayer.posZ;
        initialX = mc.thePlayer.posX;
        initialZ = mc.thePlayer.posZ;


        cycles = 0;
        rotating = false;

    }

    void updateKeybinds(boolean forward, boolean backward, boolean left, boolean right) {

        KeyBinding.setKeyBindState(keybindW ,forward);
        KeyBinding.setKeyBindState(keybindA ,left);
        KeyBinding.setKeyBindState(keybindD ,right);
        KeyBinding.setKeyBindState(keybindS ,backward);


    }

    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<Integer>();
        for (int i = -5; i < 5; i++) {
            if (!Utils.isWalkable(Utils.getBlockAround(i, 0))) {
                unwalkableBlocks.add(i);
            }
        }

        if(unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.size() > 1) {
            return direction.NONE;
        }
        else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }

    boolean shouldWalkForward(){
        return (Utils.isWalkable(Utils.getBackBlock()) && Utils.isWalkable(Utils.getFrontBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getLeftBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getLeftBlock()));
    }
}
