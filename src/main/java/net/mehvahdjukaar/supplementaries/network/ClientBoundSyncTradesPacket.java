package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSyncTradesPacket {
    private final int containerId;
    public final MerchantOffers offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

    public ClientBoundSyncTradesPacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.offers = MerchantOffers.createFromStream(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    public ClientBoundSyncTradesPacket(int id, MerchantOffers offers, int level, int villagerXp, boolean showProgress, boolean canRestock) {
        this.containerId = id;
        this.offers = offers;
        this.villagerLevel = level;
        this.villagerXp = villagerXp;
        this.showProgress = showProgress;
        this.canRestock = canRestock;
    }

    public static void buffer(ClientBoundSyncTradesPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        message.offers.writeToStream(buf);
        buf.writeVarInt(message.villagerLevel);
        buf.writeVarInt(message.villagerXp);
        buf.writeBoolean(message.showProgress);
        buf.writeBoolean(message.canRestock);

    }

    public static void handler(ClientBoundSyncTradesPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu container = Minecraft.getInstance().player.containerMenu;
            if (message.containerId == container.containerId && container instanceof RedMerchantContainerMenu) {
                ((RedMerchantContainerMenu) container).setOffers(new MerchantOffers(message.offers.createTag()));
                ((RedMerchantContainerMenu) container).setXp(message.villagerXp);
                ((RedMerchantContainerMenu) container).setMerchantLevel(message.villagerLevel);
                ((RedMerchantContainerMenu) container).setShowProgressBar(message.showProgress);
                ((RedMerchantContainerMenu) container).setCanRestock(message.canRestock);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}