package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

public class ShipGetter extends JSimpleCommand {
    @NotNull
    public static final ShipGetter INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new ShipGetter();
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("AssetData.db"));
    }

    public ShipGetter() {
        super(PluginMain.INSTANCE, "获取方式", "获取");
    }

    @Handler
    public static void doCommand(@NotNull MemberCommandSenderOnMessage sender, String shipName) {
        // 先去库里模糊搜索
        StringBuilder sql = new StringBuilder("select name from shipScience where name like '%%%s%%' or alias like '%%%s%%'");
        List<Map<String, Object>> maps = jdbc.executeQuerySQL(String.format(sql.toString(), shipName, shipName));
        if (maps.isEmpty()) {
            sql = new StringBuilder(("select name from shipScience where 1 = 1"));
            for (char c : shipName.toCharArray()) {
                sql.append(String.format(" and name like '%%%s%%'", c));
            }
            maps = jdbc.executeQuerySQL(sql.toString());
            if (maps.isEmpty()) {
                sender.sendMessage("没有查找到舰船 " + shipName + "！");
                return;
            }
        }
        shipName = maps.get(0).get("name").toString();
        
        // 之后根据模糊搜索的名字去wiki查
        try {
            Document document = Jsoup.connect("https://wiki.biligame.com/blhx/" + shipName).get();
            Elements elements = document.getElementsByClass("wikitable sv-general");
            Element element = elements.get(0);
            Elements rows = element.getElementsByTag("tr");
            StringBuilder message = new StringBuilder();
            message.append(shipName).append(" 的获取方式有：");

            Element buildRow = rows.get(5);
            Elements build = buildRow.getElementsByTag("td");
            String buildSting = build.get(1).text().replace(" ", "\n    ");
            if (!buildSting.equals("") && !buildSting.equals("无法建造")) {
                message.append("\n建造时间：\n    ")
                        .append(buildSting);
            }

            Element simpleRow = rows.get(6);
            Elements simple = simpleRow.getElementsByTag("td");
            String simpleSting = simple.get(1).text().replace(" ", "\n    ");
            if (!simpleSting.equals("")) {
                message.append("\n主线打捞：\n    ")
                        .append(simpleSting);
            }

            Element archiveRow = rows.get(7);
            Elements archive = archiveRow.getElementsByTag("td");
            String archiveSting = archive.get(1).text().replace(" ", "\n    ");
            if (!archiveSting.equals("")) {
                message.append("\n活动档案：\n    ")
                        .append(archiveSting);
            }

            Element otherRow = rows.get(9);
            Elements other = otherRow.getElementsByTag("td");
            String otherTitle = other.get(0).text();
            String otherString = "";
            if (otherTitle.equals("其他途径")) {
                otherString = other.get(1).text().replace(" ", "\n    ");
            }
            if (!otherString.equals("")) {
                message.append("\n其他方式：\n    ")
                        .append(otherString);
            }
            sender.sendMessage(message.toString());
        } catch (Exception e) {
            sender.sendMessage("没有查找到舰船 " + shipName + "！");
        }
    }
}
