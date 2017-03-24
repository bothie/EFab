package mcjty.efab.blocks.rfstorage;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import mcjty.efab.blocks.GenericEFabTile;
import mcjty.efab.blocks.IEFabEnergyStorage;
import mcjty.efab.config.GeneralConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class RFStorageTE extends GenericEFabTile implements IEnergyProvider, IEnergyReceiver, IEFabEnergyStorage {

    private int energy = 0;

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        energy = tagCompound.getInteger("energy");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("energy", energy);
    }

    @Override
    public boolean extractEnergy(int amount) {
        if (amount > energy) {
            return false;
        }
        energy -= amount;
        markDirtyQuick();
        return true;
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        int toreceive = Math.min(maxReceive, GeneralConfiguration.rfStorageInputPerTick);
        int newenergy = energy + toreceive;
        if (newenergy > GeneralConfiguration.rfStorageMax) {
            toreceive -= newenergy - GeneralConfiguration.rfStorageMax;
            newenergy = GeneralConfiguration.rfStorageMax;
        }
        if (!simulate && energy != newenergy) {
            energy = newenergy;
            markDirtyQuick();
        }
        return toreceive;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return energy;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return GeneralConfiguration.rfStorageMax;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    private IEnergyStorage handler = null;

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            if (handler == null) {
                handler = new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return 0;
                    }

                    @Override
                    public int extractEnergy(int maxExtract, boolean simulate) {
                        return RFStorageTE.this.extractEnergy(facing, maxExtract, simulate);
                    }

                    @Override
                    public int getEnergyStored() {
                        return RFStorageTE.this.getEnergyStored(facing);
                    }

                    @Override
                    public int getMaxEnergyStored() {
                        return RFStorageTE.this.getMaxEnergyStored(facing);
                    }

                    @Override
                    public boolean canExtract() {
                        return false;
                    }

                    @Override
                    public boolean canReceive() {
                        return true;
                    }
                };
                return CapabilityEnergy.ENERGY.cast(handler);
            }
        }
        return super.getCapability(capability, facing);
    }

}