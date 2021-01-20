import io.github.mzdluo123.mirai.android.groovy.GroovyScript
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.jetbrains.annotations.NotNull

class A extends GroovyScript {
    @Override
    void onEnable(@NotNull Bot bot) {
        super.onEnable(bot)
        bot.eventChannel.registerListenerHost(this)
    }

    @EventHandler
    void onGroupMessage(GroupMessageEvent event) {

    }
}