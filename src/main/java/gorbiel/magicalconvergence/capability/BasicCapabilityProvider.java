package gorbiel.magicalconvergence.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public abstract class BasicCapabilityProvider<C> implements ICapabilityProvider {

	public final Capability<C> capability;
	public final LazyOptional<C> instance;

	public BasicCapabilityProvider(Capability<C> capability, NonNullSupplier<C> instanceSupplier) {
		this.capability = capability;
		this.instance = LazyOptional.of(instanceSupplier);
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		return cap == this.capability ? this.instance.cast() : LazyOptional.empty();
	}

}
