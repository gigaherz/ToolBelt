package gigaherz.toolbelt.newcaps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jdk.nashorn.internal.objects.annotations.Function;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

class NewCaps
{
    public static class FlexCapability<TClass, TDiscriminator>
    {

    }

    public static class CapabilityManager
    {

    }

    @FunctionalInterface
    public static interface ICapabilityChangeListener
    {
        <T, D> void capabilityMapChanged(FlexCapability<T, D key, @Nullable D discriminator, @Nullable T instanceRemoved, @Nullable T instanceAdded);
    }

    public static class CapabilityContainer
    {
        Map<Pair<FlexCapability,Object>, Object> capabilities = Maps.newLinkedHashMap();
        Map<Pair<FlexCapability,Object>, List<ICapabilityChangeListener>> listeners = Maps.newLinkedHashMap();

        public <T, D> void addCapbility(FlexCapability<T, D> key, @Nullable D discriminator, T instance)
        {
            Pair<FlexCapability<T, D>, D> pair = Pair.of(key, discriminator);

            @SuppressWarnings("unchecked")
            T old = (T)capabilities.get(pair);

            capabilities.put(Pair.of(key, discriminator), instance);

            listeners.get(pair).forEach(l -> l.capabilityMapChanged(key, discriminator, old, instance));
        }

        public <T, D> void removeCapability(FlexCapability<T, D> key, @Nullable D discriminator)
        {
            Pair<FlexCapability<T, D>, D> pair = Pair.of(key, discriminator);

            @SuppressWarnings("unchecked")
            T old = (T)capabilities.remove(Pair.of(key, discriminator));

            if (old != null)
                listeners.get(pair).forEach(l -> l.capabilityMapChanged(key, discriminator, old, null));
        }

        public <T, D> void addListener(FlexCapability<T, D> key, @Nullable D discriminator, ICapabilityChangeListener listener)
        {
            Pair<FlexCapability, Object> pair = Pair.of(key, discriminator);

            listeners.computeIfAbsent(pair, (k) -> new ArrayList<>()).add(listener);
        }

        public <T, D> void removeListener(FlexCapability<T, D> key, @Nullable D discriminator, ICapabilityChangeListener listener)
        {
            Pair<FlexCapability, Object> pair = Pair.of(key, discriminator);

            listeners.computeIfAbsent(pair, (k) -> new ArrayList<>()).add(listener);
        }

        public <T, D> LazyOptional<T> findCapability(FlexCapability<T, D> key, @Nullable D discriminator)
        {
            return null;
        }

        public <T, D> LazyOptional<T> findCapability(FlexCapability<T, D> key) {
            return findCapability(key, null);
        }

    }
}