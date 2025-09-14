package epie.homerun;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class GameCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> gameCommand = Commands.literal("homerun")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("start")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(10, 180))
                                .executes(context -> {
                                    int timeInSeconds = IntegerArgumentType.getInteger(context, "seconds");
                                    GameManager.startGame(context.getSource(), timeInSeconds);
                                    return 1;
                                })))
                .then(Commands.literal("stop")
                        .executes(context -> {
                            GameManager.stopGame(context.getSource());
                            return 1;
                        }))
                .then(Commands.literal("setting")
                        .executes(context -> {
                            GameManager.setupGame(context.getSource());
                            return 1;
                        }))
                .then(Commands.literal("multiplier")
                        .then(Commands.argument("multiplier", IntegerArgumentType.integer(1, 100))
                                .executes(context -> {
                                    int multiplier = IntegerArgumentType.getInteger(context, "multiplier");
                                    GameManager.setMultiplier(multiplier);
                                    context.getSource().sendSuccess(() -> Component.literal("倍率を1/" + multiplier + "に設定しました"), false);
                                    return 1;
                                })))
                .then(Commands.literal("delball")
                        .executes(context -> {
                            GameManager.balls.forEach(Entity::discard);
                            GameManager.balls.clear();
                            context.getSource().sendSuccess(() -> Component.literal("ボールをすべて削除しました"), false);
                            return 1;
                        }))
                .then(Commands.literal("setpos")
                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            if (GameManager.getGameState() != GameManager.GameState.STOPPED) {
                                                context.getSource().sendFailure(Component.literal("ゲーム実行中は設定できません"));
                                            }
                                            BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
                                            BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");
                                            GameManager.setPoses(pos1, pos2);
                                            context.getSource().sendSuccess(() -> Component.literal("範囲を" + pos1 + "から" + pos2 + "に設定しました"), false);
                                            return 1;
                                        }))));

        dispatcher.register(gameCommand);
    }
}
