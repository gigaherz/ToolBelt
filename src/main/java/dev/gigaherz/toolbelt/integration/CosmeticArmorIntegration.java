package dev.gigaherz.toolbelt.integration;

//import lain.mods.cos.api.CosArmorAPI;
import net.minecraft.world.entity.player.Player;

public class CosmeticArmorIntegration
{
    public static boolean isHidden(Player player, String modid, String id)
    {
        return false; // TODO: CosArmorAPI.getCAStacksClient(player.getUUID()).isHidden(modid, id);
    }
}
