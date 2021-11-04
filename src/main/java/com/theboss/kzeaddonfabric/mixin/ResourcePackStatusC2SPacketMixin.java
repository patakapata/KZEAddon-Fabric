package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@org.spongepowered.asm.mixin.Mixin(ResourcePackStatusC2SPacket.class)
public abstract class ResourcePackStatusC2SPacketMixin implements Packet<ServerPlayPacketListener> {
    @Shadow
    private ResourcePackStatusC2SPacket.Status status;

    /**
     * @author theBooooSS
     * @reason Ignore the server resource pack
     */
    @Overwrite
    public void write(PacketByteBuf buf) {
        if (KZEAddon.options.shouldIgnoreResourcePack) {
            buf.writeEnumConstant(ResourcePackStatusC2SPacket.Status.ACCEPTED);
        } else {
            buf.writeEnumConstant(this.status);
        }
    }
}
