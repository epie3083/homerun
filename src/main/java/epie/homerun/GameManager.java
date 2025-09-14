package epie.homerun;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {
    private static GameState currentState = GameState.STOPPED;
    @Nullable
    private static WanderingTrader gameTrader = null;
    @Nullable
    private static UUID gameTraderUUID = null;
    @Nullable
    private static Vec3 knockbackStartPosition = null;

    private static int multiplier = 30;
    private static UUID batterUUID = null;
    private static BlockPos pos1 = null;
    private static BlockPos pos2 = null;

    private static int startCountdown = 100;
    private static int timer = 0;
    private static float score = 0;

    public static List<WanderingTrader> balls = new ArrayList<>();

    public enum GameState {
        STOPPED,
        WAITING,
        STARTING,
        SCORING,
        KNOCKBACK,
        TOTALLING,
        FINISHED
    }

    public static GameState getGameState() {
        return currentState;
    }

    public static void setMultiplier(int multiplier) {
        GameManager.multiplier = multiplier;
    }

    public static void setPoses(BlockPos pos1, BlockPos pos2) {
        GameManager.pos1 = pos1;
        GameManager.pos2 = pos2;
    }

    public static void setupGame(CommandSourceStack source) {
        if (currentState != GameState.STOPPED) {
            source.sendFailure(Component.literal("すでに始まっているゲームがあります"));
            return;
        }

        ServerLevel level = source.getLevel();
        resetGame();

        gameTrader = EntityType.WANDERING_TRADER.create(level);
        if (gameTrader != null) {
            gameTrader.addTag("homerun_trader");


            Vec3 position = new Vec3(
                    (double) (pos1.getX() + pos2.getX()) / 2 + 0.5,
                    Math.min(pos1.getY(), pos2.getY()),
                    (double) (pos1.getZ() + pos2.getZ()) / 2 + 0.5
            );
            gameTrader.setPos(position);
            knockbackStartPosition = position;
            level.addFreshEntity(gameTrader);
            gameTraderUUID = gameTrader.getUUID();
            balls.add(gameTrader);

            ServerPlayer batter = (ServerPlayer) level.getNearestPlayer(gameTrader, -1);
            if (batter != null) {
                batterUUID = batter.getUUID();
                batter.getInventory().add(new ItemStack(Items.WOODEN_SWORD));
            } else {
                source.sendFailure(Component.literal("プレイヤーが見つかりません。やり直してください"));
                resetGame();
                return;
            }

            walls(level, Blocks.BARRIER);

            currentState = GameState.WAITING;

            String posStr = String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z);
            source.sendSuccess(() -> Component.literal("準備完了(座標:" + posStr + ")"), false);
        } else {
            source.sendFailure(Component.literal("球の配置に失敗しました。やり直してください"));
        }
    }

    public static void startGame(CommandSourceStack source, int timeInSeconds) {
        if (currentState != GameState.WAITING) {
            source.sendFailure(Component.literal("設定を先にしてください(/homerun setting x y z)"));
            return;
        }
        if (gameTrader == null || gameTrader.isRemoved()) {
            source.sendFailure(Component.literal("球が見つかりません。再度設定してください"));
            resetGame();
            return;
        }

        score = 0;
        timer = timeInSeconds * 20;
        currentState = GameState.STARTING;

        source.getLevel().getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("準備完了!(ゲーム時間: " + timeInSeconds + "秒)"),
                false
        );
    }

    public static void stopGame(CommandSourceStack source) {
        if (currentState == GameState.STOPPED) {
            source.sendFailure(Component.literal("進行中のゲームはありません"));
            return;
        }
        resetGame();
        walls(source.getLevel(), Blocks.AIR);
        source.sendSuccess(() -> Component.literal("ゲームを中止しました"), true);
    }

    public static void serverTick(ServerLevel level) {
        if (currentState == GameState.STOPPED || currentState == GameState.KNOCKBACK || currentState == GameState.FINISHED)
            return;

        if (gameTrader == null || gameTrader.isRemoved()) {
            if (gameTraderUUID != null) {
                gameTrader = (WanderingTrader) level.getEntity(gameTraderUUID);
            }
            if (gameTrader == null) {
                level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("球を消失しました。再度設定してください"), false);
                resetGame();
                return;
            }
        }

        if (currentState == GameState.SCORING) {
            if (timer > 0) {
                if (timer % 20 == 0) {
                    Component message = Component.literal("残り" + timer / 20 + "秒");
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
                    for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                        player.connection.send(packet);
                    }
                }
                timer--;
            } else {
                transitionToKnockback(level);
            }
        } else if (currentState == GameState.STARTING) {
            if (startCountdown > 0) {
                if (startCountdown % 20 == 0) {
                    Component message = Component.literal("開始まで" + startCountdown / 20 + "秒");
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
                    for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                        player.connection.send(packet);
                    }
                }
                startCountdown--;
            } else {
                currentState = GameState.SCORING;
                level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("力をためろ!").withStyle(ChatFormatting.GOLD), false);
            }
        }

        if (currentState == GameState.TOTALLING) {
            if (gameTrader.onGround() && gameTrader.getDeltaMovement().lengthSqr() < 0.1) {
                calculateDistanceAndFinish(level);
            } else if (gameTrader.isInWater() && !gameTrader.onGround()) {
                Vec3 motion = gameTrader.getDeltaMovement();
                double horizontalMotionSqr = motion.x * motion.x + motion.z * motion.z;
                if (horizontalMotionSqr < 0.1) {
                    handleWaterLanding(level);
                }
            }
        }
    }

    public static void onTraderAttacked(float damage) {
        score += damage;
    }

    public static void onTradeKnockedBack() {
        currentState = GameState.TOTALLING;
        if (gameTrader == null || gameTrader.isRemoved()) {
            return;
        }
        gameTrader.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 255, false, false, false));
    }

    private static void transitionToKnockback(ServerLevel level) {
        currentState = GameState.KNOCKBACK;

        ItemStack knockbackStick = new ItemStack(Items.STICK);
        knockbackStick.enchant(Enchantments.KNOCKBACK, (int) (score / multiplier) + 1);
        knockbackStick.setHoverName(Component.literal("ホームランバッド").withStyle(ChatFormatting.GOLD));

        walls(level, Blocks.AIR);

        if (batterUUID != null) {
            ServerPlayer batter = level.getServer().getPlayerList().getPlayer(batterUUID);
            if (batter != null) {
                batter.getInventory().add(knockbackStick);
            } else {
                level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("プレイヤーがいないためゲームを中止します"), false);
                resetGame();
            }
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("ホームランを打て!").withStyle(ChatFormatting.GOLD), false);
        }
    }

    private static void handleWaterLanding(ServerLevel level) {
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("池ポチャ(´•ω•̥`)"),
                false
        );
        resetGame();
    }

    private static void calculateDistanceAndFinish(ServerLevel level) {
        if (knockbackStartPosition == null || gameTrader == null) return;
        if (currentState == GameState.FINISHED) return;

        Vec3 endPos = gameTrader.position();
        double distance = knockbackStartPosition.distanceTo(endPos);
        String formattedDistance = String.format("%.2f", distance);

        level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("ホームラン! 飛距離: " + formattedDistance + "m"), false);

        currentState = GameState.FINISHED;
        resetGame();
    }

    private static void walls(ServerLevel level, Block wallBlock) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        BlockState wallBlockState = wallBlock.defaultBlockState();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                mutableBlockPos.set(minX, y, z);
                level.setBlock(mutableBlockPos, wallBlockState, 2);
            }
        }
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                mutableBlockPos.set(maxX, y, z);
                level.setBlock(mutableBlockPos, wallBlockState, 2);
            }
        }
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX + 1; x < maxX; x++) {
                mutableBlockPos.set(x, y, minZ);
                level.setBlock(mutableBlockPos, wallBlockState, 2);
            }
        }
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX + 1; x < maxX; x++) {
                mutableBlockPos.set(x, y, maxZ);
                level.setBlock(mutableBlockPos, wallBlockState, 2);
            }
        }
    }

    private static void resetGame() {
        currentState = GameState.STOPPED;
        gameTrader = null;
        gameTraderUUID = null;
        knockbackStartPosition = null;
        batterUUID = null;
        timer = 0;
        score = 0;
        startCountdown = 100;
    }
}
