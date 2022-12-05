package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginConfig.MySetting;
import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.JDBCUtils;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import kotlin.Triple;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Steal extends JSimpleCommand {
    @NotNull
    public static final Steal INSTANCE;

    public static final SQLiteJDBC jdbc;
    public static final Random random = new Random();
    public static final Set<Long> todayMap = new HashSet<>();
    private Integer updateTime = 0;


    static {
        INSTANCE = new Steal();
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("Others.db"));
    }

    private Steal() {
        super(PluginMain.INSTANCE, "steal", "偷彩炮");
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender, @NotNull Member member) {
        // 每天只能偷一次
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (today != updateTime) {
            todayMap.clear();
            updateTime = today;
        }
        Member messageSender = sender.getFromEvent().getSender();
        String toName = member.getNameCard().isEmpty() ? member.getNick() : member.getNameCard();
        String fromName = messageSender.getNameCard().isEmpty() ? messageSender.getNick() : messageSender.getNameCard();
        MessageChainBuilder builder = new MessageChainBuilder();
        long fromQQ = messageSender.getId();
        long toQQ = member.getId();
        if (todayMap.contains(fromQQ)) {
            sender.sendMessage("“你今天已经偷过彩炮了！偷东西是不好的行为！” ——小韭菜边偷你的彩炮边说道");
            return;
        }
        todayMap.add(fromQQ);
        builder.add(new At(fromQQ));

        // 25%的几率走错路
        if (random.nextInt(4) == 3) {
            ContactList<NormalMember> members = sender.getGroup().getMembers();
            // 最近说话的10个人里面随机一个
            List<NormalMember> memberList = new ArrayList<>(members.stream()
                    .sorted(Comparator.comparing(NormalMember::getLastSpeakTimestamp).reversed())
                    .toList()
                    .subList(0, 10));
            // 无论如何也有几率走到群猪的仓库
            int i = random.nextInt(11);
            Long newToQQ;
            String newToName;
            if (i == 10) {
                newToQQ = 1065412611L;
                newToName = "常盘台御坂美琴（碧蓝代肝）";
            } else {
                newToQQ = memberList.get(i).getId();
                newToName = memberList.get(i).getNameCard().isEmpty() ? memberList.get(i).getNick() : memberList.get(i).getNameCard();
            }
            if (!newToQQ.equals(toQQ)) {
                builder.add(String.format("\n你准备去偷%s的彩炮但是迷了路！结果走到了", toName));
                toQQ = newToQQ;
                toName = newToName;
                builder.add(new At(toQQ));
                builder.add("的家！");
            }
        }

        // 如果没偷过就初始20张彩炮
        Map<String, Object> fromMap = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", fromQQ + ""), "");
        if (fromMap.isEmpty()) {
            fromMap.put("QQ", fromQQ);
            fromMap.put("count", 20);
            fromMap.put("stole", 0);
            fromMap.put("be_stole", 0);
            fromMap.put("lose", 0);
            fromMap.put("pick_up", 0);
            fromMap.put("stole_count", 0);
            fromMap.put("be_stole_count", 0);
            fromMap.put("complete_count", 0);
            fromMap.put("name", "'" + fromName + "'");
            JDBCUtils.insert(jdbc, "ColorDrawing", fromMap);
        }
        Map<String, Object> toMap = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", toQQ + ""), "");
        if (toMap.isEmpty()) {
            toMap.put("QQ", toQQ);
            toMap.put("count", 20);
            toMap.put("stole", 0);
            toMap.put("be_stole", 0);
            toMap.put("lose", 0);
            toMap.put("pick_up", 0);
            toMap.put("stole_count", 0);
            toMap.put("be_stole_count", 0);
            toMap.put("complete_count", 0);
            toMap.put("name", "'" + toName + "'");
            JDBCUtils.insert(jdbc, "ColorDrawing", toMap);
        }
        String sql;
        if (toQQ == fromQQ) {
            builder.add("\n你尝试偷你自己的彩炮想要刷记录！但是被小韭菜偷了1张！你彩炮 - 1！");
            sql = "update ColorDrawing set name = '%s', count = count - 1, be_stole = be_stole + 1, stole_count = stole_count + 1, be_stole_count = be_stole_count + 1 where QQ = '%s'";
            jdbc.executeQuerySQL(String.format(sql, fromName, fromQQ));
        } else {
            int stealCount = 1;
            int hitCount = 0;
            do {
                // 25%强袭
                if (random.nextInt(4) == 3) {
                    builder.add("\n强袭偷王！");
                    stealCount = 2;
                }
                // 一次以上快速起飞
                if (hitCount++ > 0) {
                    builder.add("\n快速开偷！" + hitCount + "连偷！");
                }
                int i = random.nextInt(100);
                if (i < 16) {
                    if (i < 5)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是被反偷了%d张！你彩炮 - %d！%s彩炮 + %d！", toName, stealCount, stealCount, toName, stealCount));
                    else if (i < 10)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是发现%s太穷之后良心发现！反送给%s%d张！你彩炮 - %d！%s彩炮 + %d！", toName, toName, toName, stealCount, stealCount, toName, stealCount));
                    else if (i < 16)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是撞到了鬼！逃跑的时候掉到%s家%d张！你彩炮 - %d！%s彩炮 + %d！", toName, toName, stealCount, stealCount, toName, stealCount));
                    sql = "update ColorDrawing set name = '%s', count = count - %d, be_stole = be_stole + %d, stole_count = stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, fromName, stealCount, stealCount, fromQQ));
                    sql = "update ColorDrawing set name = '%s', count = count + %d, stole = stole + %d, be_stole_count = be_stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, toName, stealCount, stealCount, toQQ));
                } else if (i < 32) {
                    if (i < 20)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是被小韭菜发现了！偷取失败！", toName));
                    else if (i < 23)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是被校长发现了！偷取失败！", toName));
                    else if (i < 26)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是被韭菜阿姨发现了！偷取失败！", toName));
                    else if (i < 29)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是偷到的是假的！偷取失败！", toName));
                    else if (i < 32)
                        builder.add(String.format("\n你尝试偷%s的彩炮！但是被不̵̢͚́̈́̑͂͋̅͝͝可̵̙̜̰̖̼̗͒名̶̱̙̜̻̰̯͋̌̆̅͒́̎͘̕͝͝状̴̫̘͔͔̳͔̬̣͛̈́̊̿̽̀͌͑͑̐̚之̶̛̮̝͖͔̭̙̌̇͌̈̋̆̈́̏̕物̴̗̼̱̻͉̗̙̙̪̈́̌̆̈͌̚͝͝发现了！偷取失败！", toName));
                    sql = "update ColorDrawing set name = '%s', stole_count = stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, fromName, fromQQ));
                } else if (i < 48) {
                    if (i < 36)
                        builder.add(String.format("\n你偷到了%s的彩炮！但是藏到宝箱里忘了放哪了！%s彩炮 - %d！", toName, toName, stealCount));
                    else if (i < 40)
                        builder.add(String.format("\n你偷到了%s的彩炮！但是没抓稳被风吹走了！%s彩炮 - %d！", toName, toName, stealCount));
                    else if (i < 44)
                        builder.add(String.format("\n你偷到了%s的彩炮！但是掉水沟里被冲走了！%s彩炮 - %d！", toName, toName, stealCount));
                    else if (i < 48)
                        builder.add(String.format("\n你偷到了%s的彩炮！但是被外星人吸走了！%s彩炮 - %d！", toName, toName, stealCount));
                    sql = "update ColorDrawing set name = '%s', stole = stole + %d, lose = lose + %d, stole_count = stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, fromName, stealCount, stealCount, fromQQ));
                    sql = "update ColorDrawing set name = '%s', count = count - %d, be_stole = be_stole + %d, be_stole_count = be_stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, toName, stealCount, stealCount, toQQ));
                } else if (i < 64) {
                    if (i < 52)
                        builder.add(String.format("\n你准备偷%s的彩炮！但是在树丛中的宝箱里捡到了%d张彩炮！高高兴兴的回去了！你彩炮 + %d！", toName, stealCount, stealCount));
                    else if (i < 56)
                        builder.add(String.format("\n你准备偷%s的彩炮！但是风吹来了%d张彩炮！高高兴兴的回去了！你彩炮 + %d！", toName, stealCount, stealCount));
                    else if (i < 60)
                        builder.add(String.format("\n你准备偷%s的彩炮！但是在路边水沟里捡到了%d张彩炮！高高兴兴的回去了！你彩炮 + %d！", toName, stealCount, stealCount));
                    else if (i < 64)
                        builder.add(String.format("\n你准备偷%s的彩炮！但是在掉下来的飞碟里捡到了%d张彩炮！高高兴兴的回去了！你彩炮 + %d！", toName, stealCount, stealCount));
                    sql = "update ColorDrawing set name = '%s', count = count + %d, pick_up = pick_up + %d, stole_count = stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, fromName, stealCount, stealCount, fromQQ));
                    sql = "update ColorDrawing set name = '%s', be_stole_count = be_stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, toName, toQQ));
                } else {
                    builder.add(String.format("\n你偷到了%s的%d张彩炮！你彩炮 + %d！%s彩炮 - %d！", toName, stealCount, stealCount, toName, stealCount));
                    sql = "update ColorDrawing set name = '%s', count = count + %d, stole = stole + %d, stole_count = stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, fromName, stealCount, stealCount, fromQQ));
                    sql = "update ColorDrawing set name = '%s', count = count - %d, be_stole = be_stole + %d, be_stole_count = be_stole_count + 1 where QQ = '%s'";
                    jdbc.executeQuerySQL(String.format(sql, toName, stealCount, stealCount, toQQ));
                }
                // 15%快速起飞
            } while (random.nextInt(100) >= 85);
            // 1%彩蛋
            if (random.nextInt(100) == 99) {
                builder.add("\n你̵̨̢̛̼͚̻̹̇̈́͋被̶̧̱͕̝̣̘̘͈̘̓̓̒͆̌͗͊̀̏̚不̸̖̰̙̣͎͙̗̄̎̈́可̴͖̟̦͙̝͈̦͛̅͐̋́͛͊̃名̵̨̬͚̟̥̯̋͑̐͘状̵̨͎̗͖͉͔̘̾̈́͆́͌̓̊之̵̧̡͙̩̜̭͕̊̈̉͐̔̎̓物̸̢̛̺͎͖̘̙̪̬̹̐͆̑̑͑̐͘盯̵͕͈̍̈̆̚上̴̥̎了̸̧͓̲̭̘̗̤̥̀͂͑̄̌̍͠");
            }

        }
        sender.sendMessage(builder.build());

        fromMap = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", fromQQ + ""), "");
        int count = (int) fromMap.get("count");
        if (count >= 50 && fromQQ != MySetting.INSTANCE.getBotID()) {
            builder = new MessageChainBuilder();
            builder.add(new At(fromQQ));
            builder.add("\n你用50张图纸合成了一门彩炮！你彩炮 - 50！");
            sql = "update ColorDrawing set name = '%s', count = count - 50, complete_count = complete_count + 1 where QQ = '%s'";
            jdbc.executeQuerySQL(String.format(sql, fromName, fromQQ));
            sender.sendMessage(builder.build());
        }
        toMap = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", toQQ + ""), "");
        count = (int) toMap.get("count");
        if (count >= 50 && toQQ != MySetting.INSTANCE.getBotID()) {
            builder = new MessageChainBuilder();
            builder.add(new At(toQQ));
            builder.add("\n你用50张图纸合成了一门彩炮！你彩炮 - 50！");
            sql = "update ColorDrawing set name = '%s', count = count - 50, complete_count = complete_count + 1 where QQ = '%s'";
            jdbc.executeQuerySQL(String.format(sql, toName, toQQ));
            sender.sendMessage(builder.build());
        }
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender) {
        sender.sendMessage("没有指定要偷的目标！从空气中是偷不出彩炮的！");
    }
}
