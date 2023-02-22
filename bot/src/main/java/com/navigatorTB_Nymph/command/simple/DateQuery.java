package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class DateQuery extends JSimpleCommand {
    @NotNull
    public static final DateQuery INSTANCE;


    static {
        INSTANCE = new DateQuery();
    }

    private DateQuery() {
        super(PluginMain.INSTANCE, "dateQuery", "黄历");
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender, String s) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        switch (s) {
            case "科研":
                sender.sendMessage("五期科研已经开了" + (today + 365 - 194) + "天！还没出三门彩炮的可以退群了~");
                break;
            case "测试服":
            default:
                sender.sendMessage("今天是2022年7月" + (today + 365 - 181) + "日！测试服是在七月底开哦~");
        }
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        main(sender, "");
    }
}
