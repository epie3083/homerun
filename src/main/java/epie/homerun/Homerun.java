package epie.homerun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Homerun.MOD_ID)
public class Homerun {
    public static final String MOD_ID = "homerun";

    public Homerun() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GameCommands.register(event.getDispatcher());
    }
}
