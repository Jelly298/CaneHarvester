package me.mixins;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class MixinBlock {



    @Inject(method = {"shouldSideBeRendered"}, at = {@At("HEAD")}, cancellable = true)
    public void shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        //cir.cancel();

        //System.out.println("hi");

    }
    /*
    @SideOnly(Side.CLIENT)
    @Overwrite
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side){
        return false;
    }*/
    /*@Redirect(method = "shouldSideBeRendered", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0))
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side){
        return false;
    }*/





}
