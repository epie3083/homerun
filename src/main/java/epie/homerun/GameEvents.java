package epie.homerun;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GameEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            GameManager.serverTick(event.getServer().overworld());
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (!event.getTarget().getTags().contains("homerun_trader")) {
            return;
        }

        if (GameManager.getGameState() == GameManager.GameState.KNOCKBACK) {
            Player player = event.getEntity();
            ItemStack heldItem = player.getMainHandItem();

            if (!(heldItem.getItem() == Items.STICK && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, heldItem) > 0)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity target = event.getEntity();
        if (!target.getTags().contains("homerun_trader")) {
            return;
        }

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player)) {
            return;
        }

        GameManager.GameState currentState = GameManager.getGameState();

        if (currentState == GameManager.GameState.SCORING) {
            GameManager.onTraderAttacked(event.getAmount());
            event.setCanceled(true);
        } else if (currentState == GameManager.GameState.KNOCKBACK) {
            if (event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() == Items.STICK) {
                GameManager.onTradeKnockedBack();
            } else {
            event.setCanceled(true);
            }
        } else if (currentState == GameManager.GameState.WAITING || currentState == GameManager.GameState.TOTALLING || currentState == GameManager.GameState.STARTING) {
            event.setCanceled(true);
        }
    }
}