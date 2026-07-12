package net.masked.createacidic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FizzSoundPacket {
    private final BlockPos pos;
    private final boolean start;

    public FizzSoundPacket(BlockPos pos, boolean start) {
        this.pos = pos;
        this.start = start;
    }

    public static void encode(FizzSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.start);
    }

    public static FizzSoundPacket decode(FriendlyByteBuf buf) {
        return new FizzSoundPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(FizzSoundPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                net.masked.createacidic.client.sound.FizzSoundManager.handle(msg.pos, msg.start));
        ctx.setPacketHandled(true);
    }
}