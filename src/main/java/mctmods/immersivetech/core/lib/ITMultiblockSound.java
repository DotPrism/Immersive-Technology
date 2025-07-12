package mctmods.immersivetech.core.lib;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.mixin.accessors.client.GuiSubtitleOverlayAccess;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BooleanSupplier;

public class ITMultiblockSound extends AbstractTickableSoundInstance
{
    private final BooleanSupplier active;
    private final BooleanSupplier valid;
    private final float maxVolume;
    private long subtitleMillis;

    public ITMultiblockSound(BooleanSupplier active, BooleanSupplier valid, Vec3 pos, SoundEvent sound, boolean loop, float maxVolume, float pitch) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.active = active;
        this.valid = valid;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.looping = loop;
        this.volume = 0.0F;
        this.maxVolume = maxVolume;
        this.pitch = pitch;
        this.subtitleMillis = Util.getMillis();;
    }

    public static BooleanSupplier startSound(BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound, float maxVolume, float pitch) {
        return startSound(active, valid, pos, sound, true, maxVolume, pitch);
    }

    public static BooleanSupplier startSound(BooleanSupplier active, BooleanSupplier valid, Vec3 pos, RegistryObject<SoundEvent> sound, boolean loop, float maxVolume, float pitch) {
        ITMultiblockSound instance = new ITMultiblockSound(active, valid, pos, (SoundEvent)sound.get(), loop, maxVolume, pitch);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(instance);
        return () -> soundManager.isActive(instance);
    }

    public boolean canStartSilent() {
        return true;
    }

    public void tick() {
        if (!this.valid.getAsBoolean()) {
            this.stop();
        } else if (this.active.getAsBoolean()) {
            long currentMillis = Util.getMillis();
            if (currentMillis - this.subtitleMillis > 1000L) {
                SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                WeighedSoundEvents weighedsoundevents = this.resolve(soundManager);
                if (weighedsoundevents != null) {
                    ((GuiSubtitleOverlayAccess) ClientUtils.mc().gui).getSubtitleOverlay().onPlaySound(this, weighedsoundevents);
                }

                this.subtitleMillis = currentMillis;
            }

            this.volume = this.maxVolume;

            // 🔊 Set pitch dynamically (example: based on time or a condition)
            this.pitch = 1.0f + 0.2f; // or use a dynamic value like Math.sin(), etc.
        } else {
            this.volume = 0.0F;
            this.pitch = 1.0f; // Optional: reset to normal when not active
        }
    }
}