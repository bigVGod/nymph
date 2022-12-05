package com.navigatorTB_Nymph.command.simple;

import com.navigatorTB_Nymph.pluginMain.PluginMain;
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC;
import kotlin.Triple;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StealQuery extends JSimpleCommand {
    @NotNull
    public static final StealQuery INSTANCE;

    public static final SQLiteJDBC jdbc;

    static {
        INSTANCE = new StealQuery();
        jdbc = new SQLiteJDBC(PluginMain.INSTANCE.resolveDataPath("Others.db"));
    }

    private StealQuery() {
        super(PluginMain.INSTANCE, "stealQuery", "偷彩炮查询");
    }


    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender) {
        Member messageSender = sender.getFromEvent().getSender();
        long fromQQ = messageSender.getId();
        Map<String, Object> map = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", fromQQ + ""), "");
        MessageChainBuilder builder = new MessageChainBuilder();
        if (map.isEmpty()) {
            builder.add("你还没有偷过彩炮！也没有被偷过！可以偷自己一张试试先！");
            sender.sendMessage(builder.build());
        } else {
            At at = new At(fromQQ);
            builder.add(at);
            String result = "\n你仓库中彩炮数：" + map.get("count")
                    + "\n偷的次数：" + map.get("stole_count")
                    + "\n被偷次数：" + map.get("be_stole_count")
                    + "\n偷到的张数：" + map.get("stole")
                    + "\n被偷的张数：" + map.get("be_stole")
                    + "\n丢了的张数：" + map.get("lose")
                    + "\n捡到的张数：" + map.get("pick_up");
            if (map.get("complete_count") != null && Integer.parseInt(map.get("complete_count").toString()) > 0) {
                result += "\n哇啊，这个偷王已经合出来" + map.get("complete_count") + "门彩炮了，是时候给予正义的制裁了";
            }
            builder.add(result);
            sender.sendMessage(builder.build());
        }
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender, @NotNull Member member) {
        long toQQ = member.getId();
        String toName = member.getNameCard().isEmpty() ? member.getNick() : member.getNameCard();
        Member messageSender = sender.getFromEvent().getSender();
        Map<String, Object> map = jdbc.selectOne("ColorDrawing", new Triple<>("QQ", "=", toQQ + ""), "");
        MessageChainBuilder builder = new MessageChainBuilder();
        if (map.isEmpty()) {
            builder.add("这个人还没有偷过彩炮！也没有被偷过！可以偷他一张试试先！");
            sender.sendMessage(builder.build());
        } else {
            At at = new At(messageSender.getId());
            builder.add(at);
            String result = "\n" + toName + "仓库中彩炮数：" + map.get("count")
                    + "\n偷的次数：" + map.get("stole_count")
                    + "\n被偷次数：" + map.get("be_stole_count")
                    + "\n偷到的张数：" + map.get("stole")
                    + "\n被偷的张数：" + map.get("be_stole")
                    + "\n丢了的张数：" + map.get("lose")
                    + "\n捡到的张数：" + map.get("pick_up");
            if (map.get("complete_count") != null && Integer.parseInt(map.get("complete_count").toString()) > 0) {
                result += "\n哇啊，这个偷王已经合出来" + map.get("complete_count") + "门彩炮了，是时候给予正义的制裁了";
            }
            builder.add(result);
            sender.sendMessage(builder.build());
        }
    }
}
