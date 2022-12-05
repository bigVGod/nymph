package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import kotlin.Triple;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Test extends JSimpleCommand {
    @NotNull
    public static final Test INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new Test();
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("Others.db"));
    }

    private Test() {
        super(PluginMain.INSTANCE, "test");
    }


    @Handler
    public final void main() {
        Map<String, Object> toMap = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", "907634014"), "");
        System.out.println(toMap);
        System.out.println(toMap.get("count").getClass());
        System.out.println(toMap.get("count"));
    }
}
