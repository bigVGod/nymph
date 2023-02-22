package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.Timer.GuessTimer;
import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class GuessSkill extends JSimpleCommand {
    @NotNull
    public static final GuessSkill INSTANCE;

    public static final SQLiteJDBC jdbc;
    public static final SQLiteJDBC jdbc1;


    public Map<Long, String> guessAnswer = new HashMap<>();
    public Map<Long, MessageChain> guessSkill = new HashMap<>();
    private static final Set<String> noSkillSet = new HashSet<>();
    private static final List<Map<String, Object>> ships;

    private static int lastDay = 0;

    static {
        INSTANCE = new GuessSkill();
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("AssetData.db"));
        jdbc1 = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("Others.db"));
        ships = jdbc.executeQuerySQL("select * from ShipScience");
    }

    private GuessSkill() {
        super(PluginMain.INSTANCE, "猜技能");
    }

    @Handler
    public void doCommand(@NotNull MemberCommandSenderOnMessage sender) {
        // 隔天刷新猜过的记录
        int day = new Date().getDay();
        if (day != lastDay) {
            initSet();
            lastDay = day;
        }
        if (GuessTimer.count <= 0) {
            sender.sendMessage("小韭菜已经猜不动惹");
            return;
        }
        GuessTimer.count--;

        Member messageSender = sender.getFromEvent().getSender();
        long QQ = messageSender.getId();
        String senderName = messageSender.getNameCard().isEmpty() ? messageSender.getNick() : messageSender.getNameCard();
        List<Map<String, Object>> maps = jdbc1.executeQuerySQL(String.format("select * from GuessSkill where QQ = '%s'", QQ));
        if (maps.isEmpty()) {
            jdbc1.insert("GuessSkill", new String[]{"QQ", "name", "score", "trueCount", "getCount"}, new String[]{"'" + QQ + "'", "'" + senderName + "'", "0", "0", "0"}, "");
        }
        jdbc1.executeQuerySQL(String.format("update GuessSkill set score = score - 1, getCount = getCount + 1, name = '%s' where QQ = '%s'", senderName, QQ));

        try {
            long groupId = sender.getGroup().getId();
            if (!guessAnswer.containsKey(groupId)) {
                // 从船的池子里随机一艘船的所有技能 直到随机到有不在排除范围的技能的船 之后从这个船的技能中随机一个
                List<String> skills = new ArrayList<>();
                do {
                    Map<String, Object> ship = ships.get(new Random().nextInt(ships.size()));
                    String skill1 = ship.get("skill1").toString();
                    String skill2 = ship.get("skill2").toString();
                    String skill3 = ship.get("skill3").toString();
                    String skill4 = ship.get("skill4").toString();
                    guessAnswer.put(groupId, ship.get("name").toString());
                    if (!noSkillSet.contains(skill1)) {
                        skills.add(skill1);
                    }
                    if (!noSkillSet.contains(skill2)) {
                        skills.add(skill2);
                    }
                    if (!noSkillSet.contains(skill3)) {
                        skills.add(skill3);
                    }
                    if (!noSkillSet.contains(skill4)) {
                        skills.add(skill4);
                    }
                } while (skills.isEmpty());
                String skill = skills.get(new Random().nextInt(skills.size()));
                System.out.println(guessAnswer.get(groupId));
                System.out.println(skill);
                // 猜过的技能当天不再出现
                noSkillSet.add(skill);

                // 从wiki拿技能图片
                String name = "文件:Skillicon_" + skill + ".png";
                String url = "https://wiki.biligame.com/blhx/" + name.replace("?", "%3F");
                Document document = Jsoup.connect(url).get();
                Elements alt = document.getElementsByAttributeValue("alt", name.replace("_", " "));
                String imgUrl = alt.get(0).attr("src");

                InputStream inputStream = new URL(imgUrl).openConnection().getInputStream();
                Image image = Contact.uploadImage(sender.getGroup(), inputStream);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.add(image);
                builder.add("\n" + skill);
                sender.sendMessage(builder.build());
                guessSkill.put(groupId, builder.build());
            } else {
                sender.sendMessage(guessSkill.get(groupId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void guessTrue(Long QQ, String name) {
        List<Map<String, Object>> maps = jdbc1.executeQuerySQL(String.format("select * from GuessSkill where QQ = '%s'", QQ));
        if (maps.isEmpty()) {
            jdbc1.insert("GuessSkill", new String[]{"QQ", "name", "score", "trueCount", "getCount"}, new String[]{"'" + QQ + "'", "'" + name + "'", "0", "0", "0"}, "");
        }
        jdbc1.executeQuerySQL(String.format("update GuessSkill set score = score + 2, trueCount = trueCount + 1, name = '%s' where QQ = '%s'", name, QQ));
    }

    private void initSet() {
        noSkillSet.clear();
        noSkillSet.add("");
        noSkillSet.add("额外供氧");
        noSkillSet.add("然而什么都没有发生");
        noSkillSet.add("装填指挥·先锋");
        noSkillSet.add("装填指挥·驱逐舰");
        noSkillSet.add("装填指挥·巡洋舰");
        noSkillSet.add("装填指挥·轻航");
        noSkillSet.add("炮术指挥·先锋");
        noSkillSet.add("炮术指挥·主力");
        noSkillSet.add("炮术指挥·巡洋舰");
        noSkillSet.add("炮术指挥·驱逐舰");
        noSkillSet.add("雷击指挥·先锋");
        noSkillSet.add("雷击指挥·巡洋舰");
        noSkillSet.add("雷击指挥·驱逐舰");
        noSkillSet.add("防空指挥·先锋");
        noSkillSet.add("防空指挥·主力");
        noSkillSet.add("防空指挥·巡洋舰");
        noSkillSet.add("防空指挥·驱逐舰");
        noSkillSet.add("战术指挥·巡洋舰");
        noSkillSet.add("战术指挥·驱逐舰");
        noSkillSet.add("全弹发射");
        noSkillSet.add("全弹发射改");
        noSkillSet.add("炮术指挥");
        noSkillSet.add("主炮连射");
        noSkillSet.add("重点打击");
        noSkillSet.add("强袭号令");
        noSkillSet.add("航空预备");
        noSkillSet.add("防空模式");
        noSkillSet.add("Code:Hikari");
        noSkillSet.add("快速装填");
        noSkillSet.add("鱼雷连射");
        noSkillSet.add("快速起飞");
        noSkillSet.add("强袭空母");
        noSkillSet.add("BIG SEVEN");
        noSkillSet.add("bili看板娘");
        noSkillSet.add("五航战");
        noSkillSet.add("先手必胜");
        noSkillSet.add("千之羽");
        noSkillSet.add("变迁之秘");
        noSkillSet.add("吸引火力");
        noSkillSet.add("姐妹同心");
        noSkillSet.add("战舰护航");
        noSkillSet.add("救援组");
        noSkillSet.add("旗舰掩护");
        noSkillSet.add("无限之darkness");
        noSkillSet.add("水雷战队");
        noSkillSet.add("浴火重生");
        noSkillSet.add("火力全开");
        noSkillSet.add("火力覆盖");
        noSkillSet.add("烟雾弹");
        noSkillSet.add("空母护航");
        noSkillSet.add("空袭引导");
        noSkillSet.add("穿甲弹精通");
        noSkillSet.add("紧急回避");
        noSkillSet.add("航母猎手");
        noSkillSet.add("航空先驱");
        noSkillSet.add("舰炮掩护");
        noSkillSet.add("舰队空母");
        noSkillSet.add("袖珍战列舰");
        noSkillSet.add("装填号令");
        noSkillSet.add("防空支援");
        noSkillSet.add("重点防护");
        noSkillSet.add("锁之巫女");
        noSkillSet.add("防空警戒");
        noSkillSet.add("除恶务尽");
        noSkillSet.add("驱逐战强化");
        noSkillSet.add("鹰之顽抗");
        noSkillSet.add("一航战");
        noSkillSet.add("二航战");
        noSkillSet.add("制空支援");
        noSkillSet.add("双影无双");
        noSkillSet.add("巨兽猎手");
        noSkillSet.add("布里发动了技能！");
        noSkillSet.add("抗击重樱");
        noSkillSet.add("机动制压");
        noSkillSet.add("狼群战术");
        noSkillSet.add("机动护卫");
        noSkillSet.add("鹰之合击");
        noSkillSet.add("火力干扰");
        noSkillSet.add("鱼雷发射");
        noSkillSet.add("照明弹");
        noSkillSet.add("BIG SEVEN-樱");
        noSkillSet.add("预备雷击");
        noSkillSet.add("定期维护");
        noSkillSet.add("塞壬之敌I（III）");
        noSkillSet.add("防护装甲");
        noSkillSet.add("侧翼掩护");
        noSkillSet.add("六驱精锐·晓");
        noSkillSet.add("六驱精锐·响");
        noSkillSet.add("六驱精锐·雷");
        noSkillSet.add("六驱精锐·电");
        noSkillSet.add("装甲防护");
        noSkillSet.add("穿甲防护");
        noSkillSet.add("花之牌");
        noSkillSet.add("集火信号-鱼雷");
    }
}
