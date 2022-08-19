package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScienceQuery extends JSimpleCommand {
    @NotNull
    private static final String usage;
    @NotNull
    public static final ScienceQuery INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new ScienceQuery();
        usage = CommandManager.INSTANCE.getCommandPrefix() + "科技点查询 [战列|正航|轻航|战巡|驱逐|重巡|轻巡|超巡|潜艇] [装填|炮击|航空|雷击|命中]";
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("AssetData.db"));
    }

    private ScienceQuery() {
        super(PluginMain.INSTANCE, "scienceQuery", "科技点查询");
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender, String type, String science) {

        List<String> types = Arrays.asList("战列", "正航", "轻航", "战巡", "驱逐", "重巡", "轻巡", "超巡", "潜艇", "战列");
        List<String> sciences = Arrays.asList("装填", "炮击", "航空", "雷击", "命中");
        StringBuilder message;
        if (!types.contains(type)) {
            message = new StringBuilder("舰船类型填写错误，请选择[战列|正航|轻航|战巡|驱逐|重巡|轻巡|超巡|潜艇]");
            sender.sendMessage(message.toString());
            return;
        }
        if ("耐久".equals(science)) {
            message = new StringBuilder("查询" + type + science + "是想累死小韭菜吗");
            sender.sendMessage(message.toString());
            return;
        }
        if (!sciences.contains(science)) {
            message = new StringBuilder("属性填写错误，请选择[装填|炮击|航空|雷击|命中]");
            sender.sendMessage(message.toString());
            return;
        }
        String sql = "select * from ShipScience where obtain like '%%%s%%%s%%' or fullLevel like '%%%s%%%s%%'";
        List<Map<String, Object>> maps = jdbc.executeQuerySQL(String.format(sql, type, science, type, science), "ScienceQuery:31");
        message = new StringBuilder("增加 " + type + science + " 的舰船有：\n");
        for (Map<String, Object> map : maps) {
            String name = map.get("name").toString();
            String obtain = map.get("obtain").toString();
            String fullLevel = map.get("fullLevel").toString();
            message.append(name);
            if (obtain.contains(science) && obtain.contains(type)) {
                message.append("\t获得 ").append(obtain);
            }
            if (fullLevel.contains(science) && fullLevel.contains(type)) {
                message.append("\t120级 ").append(fullLevel);
            }
            message.append("\n");
        }
        sender.sendMessage(message.toString());
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender, String searchName) {
        String sql = "select * from ShipScience where name like '%%%s%%'";
        List<Map<String, Object>> maps = jdbc.executeQuerySQL(String.format(sql, searchName), "ScienceQuery:75");
        StringBuilder message;
        message = new StringBuilder("名字包含 " + searchName + " 的舰船有：\n");
        for (Map<String, Object> map : maps) {
            String name = map.get("name").toString();
            String obtain = map.get("obtain").toString();
            String fullLevel = map.get("fullLevel").toString();
            message.append(name)
                    .append("\t获得 ")
                    .append(obtain)
                    .append("\t120级 ")
                    .append(fullLevel)
                    .append("\n");
        }
        sender.sendMessage(message.toString());
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender) {
        sender.sendMessage("参数不匹配, 你是否想执行:\n " + usage);
    }
}
