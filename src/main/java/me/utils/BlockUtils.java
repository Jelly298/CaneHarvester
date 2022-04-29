package me.utils;

import me.CaneHarvester;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

import java.util.ArrayList;

public class BlockUtils {
    static  Minecraft mc = Minecraft.getMinecraft();
    public static int getUnitX() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 0;
        } else if (modYaw < 135) {
            return -1;
        } else if (modYaw < 225) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int getUnitZ() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 1;
        } else if (modYaw < 135) {
            return 0;
        } else if (modYaw < 225) {
            return -1;
        } else {
            return 0;
        }
    }
    public static boolean isWalkable(Block block) {
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.reeds;
    }

    // 0, 0 = initial block
    public static Block getBlockAround(int rightOffset, int frontOffset){
        return getBlockAround(rightOffset, frontOffset, 0);
    }
    public static Block getBlockAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        int X = (int)Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int)Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int)Math.round(Math.floor(mc.thePlayer.posZ));
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,//
                        getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());

    }
    public static BlockPos getBlockPosAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        int X = (int)Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int)Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int)Math.round(Math.floor(mc.thePlayer.posZ));
        return new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX()* frontOffset + X, Y + upOffset,
                getUnitX() * rightOffset + getUnitZ() * frontOffset + Z);

    }
    public static Block getFrontBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getFrontDownBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posX, mc.thePlayer.posY - 1,
                        mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getBackBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().zCoord * -1 + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getRightBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posZ)).getBlock());
    }

    public static Block getLeftBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().xCoord * -1 + mc.thePlayer.posZ)).getBlock());
    }
    public static int bedrockCount() {
        // if (System.currentTimeMillis() % 1000 < 20) {
        Minecraft mc = Minecraft.getMinecraft();
        int r = 4;
        int count = 0;
        BlockPos playerPos = mc.thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3i vec3i = new Vec3i(r, r, r);
        Vec3i vec3i2 = new Vec3i(r, r, r);
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i2))) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.bedrock) {
                count++;
            }
        }
        return count;
        // }
        // return 0;
    }

}
