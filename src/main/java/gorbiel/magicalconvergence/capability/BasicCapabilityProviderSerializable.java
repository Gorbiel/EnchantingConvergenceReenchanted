package gorbiel.magicalconvergence.capability;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class BasicCapabilityProviderSerializable<C> extends BasicCapabilityProvider<C> implements ICapabilitySerializable<Tag> {

	public BasicCapabilityProviderSerializable(Capability<C> capability, NonNullSupplier<C> instanceSupplier) {
		super(capability, instanceSupplier);
	}

	protected C getInstance() {
		return this.instance.orElseThrow(() -> new IllegalStateException("Capability instance is not present"));
	}

	@Override
	public abstract Tag serializeNBT();

	@Override
	public abstract void deserializeNBT(Tag nbt);

}