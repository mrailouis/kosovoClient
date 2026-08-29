package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.sounds.CustomWinSound;
import com.mrailouis.kosovoclient.features.impl.visuals.Particles;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    private WorldClient clientWorldController;

    @Inject(method = "handleAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EffectRenderer;emitParticleAtEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;)V"))
    private void onVanillaParticleAnimation(S0BPacketAnimation packetIn, CallbackInfo ci) {
        if (this.clientWorldController != null) {
            Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityID());
            if (entity != null) {
                EnumParticleTypes type = packetIn.getAnimationType() == 4 ? EnumParticleTypes.CRIT : EnumParticleTypes.CRIT_MAGIC;
                Particles.getInstance().handleVanillaEmitter(entity, type);
            }
        }
    }

    @Inject(method = "handleTitle", at = @At("HEAD"))
    private void onHandleTitle(S45PacketTitle packetIn, CallbackInfo ci) {
        if (packetIn.getType() == S45PacketTitle.Type.TITLE || packetIn.getType() == S45PacketTitle.Type.SUBTITLE) {
            if (packetIn.getMessage() != null) {
                CustomWinSound.getInstance().checkTitle(packetIn.getMessage().getUnformattedText());
            }
        }
    }
}
