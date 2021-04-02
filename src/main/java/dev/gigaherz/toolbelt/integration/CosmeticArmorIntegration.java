package dev.gigaherz.toolbelt.integration;

import lain.mods.cos.api.CosArmorAPI;
import net.minecraft.entity.player.PlayerEntity;

public class CosmeticArmorIntegration
{
    public static boolean isHidden(PlayerEntity player, String modid, String id)
    {
        return CosArmorAPI.getCAStacksClient(player.getUniqueID()).isHidden(modid, id);
    }
}
