package github.kasuminova.novaeng.common.hypernet.proc.server.module;

import github.kasuminova.novaeng.common.hypernet.proc.server.CalculateServer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class ModuleExtensionCard extends ServerModule {

    public ModuleExtensionCard(final CalculateServer parent) {
        super(parent);
    }

    @Override
    public void readNBT(@Nonnull final NBTTagCompound nbt) {

    }

    @Override
    public void writeNBT(@Nonnull final NBTTagCompound nbt) {

    }

}
