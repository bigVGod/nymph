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

public class EquipQuery extends JSimpleCommand {
    @NotNull
    private static final String usage;
    @NotNull
    public static final EquipQuery INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new EquipQuery();
        usage = CommandManager.INSTANCE.getCommandPrefix() + "装备查询 名称";
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("AssetData.db"));
    }

    private EquipQuery() {
        super(PluginMain.INSTANCE, "equipQuery", "装备查询", "装备");
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender, String key) {
        String sql = "select * from equip where alias like '%%%s%%'";
        List<Map<String, Object>> maps = jdbc.executeQuerySQL(String.format(sql, key), "EquipQuery:37");
        StringBuilder message = new StringBuilder();
        if (maps.isEmpty()) {
            message.append(String.format("没有查找到 %s !", key));
            sender.sendMessage(message.toString());
        } else if (maps.size() > 1) {
            sql = "select * from equip where alias = '%s'";
            maps = jdbc.executeQuerySQL(String.format(sql, key), "EquipQuery:37");
            if (maps.size() != 1) {
                message.append(String.format("没有查找到 %s !", key));
                sender.sendMessage(message.toString());
            } else {
                Map<String, Object> map = maps.get(0);
                String name = map.get("name").toString();
                String alias = map.get("alias").toString();
                String source = map.get("source").toString();
                message.append("\n装备名称：").append(name).append("\n")
                        .append("装备别名：").append(alias).append("\n")
                        .append("获取方式：").append(source);
                File file = PluginMain.INSTANCE.resolveDataPath("equip/" + name + ".jpg").toFile();
                System.out.println(file.getAbsolutePath());
                if (!file.exists()) {
                    file = PluginMain.INSTANCE.resolveDataPath("skin/1.gif").toFile();
                }
                MessageChainBuilder builder = new MessageChainBuilder();
                Image image = Contact.uploadImage(sender.getGroup(), file);
                builder.add(image);
                builder.add(message.toString());
                sender.sendMessage(builder.build());
            }
        } else {
            Map<String, Object> map = maps.get(0);
            String name = map.get("name").toString();
            String alias = map.get("alias").toString();
            String source = map.get("source").toString();
            message.append("\n装备名称：").append(name).append("\n")
                    .append("装备别名：").append(alias).append("\n")
                    .append("获取方式：").append(source);
            File file = PluginMain.INSTANCE.resolveDataPath("equip/" + name + ".jpg").toFile();
            System.out.println(file.getAbsolutePath());
            if (!file.exists()) {
                file = PluginMain.INSTANCE.resolveDataPath("skin/1.gif").toFile();
            }
            MessageChainBuilder builder = new MessageChainBuilder();
            Image image = Contact.uploadImage(sender.getGroup(), file);
            builder.add(image);
            builder.add(message.toString());
            sender.sendMessage(builder.build());
        }
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender) {
        sender.sendMessage("参数不匹配, 你是否想执行:\n " + usage);
    }
}
