package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SkinQuery extends JSimpleCommand {
    @NotNull
    private static final String usage;
    @NotNull
    public static final SkinQuery INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new SkinQuery();
        usage = CommandManager.INSTANCE.getCommandPrefix() + "皮肤查询 [舰船名称|皮肤名称]";
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("AssetData.db"));
    }

    private SkinQuery() {
        super(PluginMain.INSTANCE, "skinQuery", "皮肤查询", "皮肤");
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender, String searchName) {
        StringBuilder message = new StringBuilder();
        if (searchName.contains("小韭菜")) {
            message.append("查我的皮肤是不是图谋不轨！");
            sender.sendMessage(message.toString());
            return;
        }
        if (searchName.contains("韭菜")) {
            message.append("查我妈的皮肤是不是图谋不轨！");
            sender.sendMessage(message.toString());
            return;
        }
        String sql = "select * from Skin where ship_name like '%%%s%%' or name like '%%%s%%'";
        List<Map<String, Object>> maps = jdbc.executeQuerySQL(String.format(sql, searchName, searchName), "SkinQuery:35");
        if (maps.isEmpty()) {
            message.append("没有查询到皮肤！");
            sender.sendMessage(message.toString());
        } else if (maps.size() > 1) {
            message.append("符合条件的皮肤有：\n");
            for (Map<String, Object> map : maps) {
                String shipName = map.get("ship_name").toString();
                String name = map.get("name").toString();
                String L2D = map.get("L2D").toString();
                String background = map.get("background").toString();
                String backgroundItem = map.get("background_item").toString();
                String difference = map.get("difference").toString();
                message.append(shipName).append("\t")
                        .append(name).append(L2D).append(background).append(backgroundItem).append(difference).append("\n");
            }
            sender.sendMessage(message.toString());
        } else {
            MessageChainBuilder builder = new MessageChainBuilder();
            Map<String, Object> map = maps.get(0);
            String shipName = map.get("ship_name").toString();
            String name = map.get("name").toString();
            String L2D = map.get("L2D").toString();
            String background = map.get("background").toString();
            String backgroundItem = map.get("background_item").toString();
            String difference = map.get("difference").toString();
            String price = map.get("price") == null ? "\\" : map.get("price").toString();
            String getWay = map.get("get_way").toString();
            String online_time = map.get("online_time").toString();
            String remark = map.get("remark").toString();
            message.append("皮肤名称: ").append(name).append(L2D).append(background).append(backgroundItem).append(difference).append("\n")
                    .append("舰船名称: ").append(shipName).append("\n")
                    .append("获取方式: ").append(getWay).append("\n")
                    .append("售价: ").append(price).append("\n")
                    .append("上架时间: ").append(online_time).append("\n")
                    .append("备注: ").append(remark).append("\n");
            File file = PluginMain.INSTANCE.resolveDataPath("skin/" + name + ".jpg").toFile();
            System.out.println(file.getAbsolutePath());
            if (!file.exists()) {
                file = PluginMain.INSTANCE.resolveDataPath("skin/1.gif").toFile();
            }
            Image image = Contact.uploadImage(sender.getGroup(), file);
            builder.add(message.toString());
            builder.add(image);
            sender.sendMessage(builder.build());
        }
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender) {
        sender.sendMessage("参数不匹配, 你是否想执行:\n " + usage);
    }
}
