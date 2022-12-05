package com.navigatorTB_Nymph.command.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navigatorTB_Nymph.pluginMain.PluginMain;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DynamicQuery extends JSimpleCommand {
    @NotNull
    public static final DynamicQuery INSTANCE;


    static {
        INSTANCE = new DynamicQuery();
    }

    private DynamicQuery() {
        super(PluginMain.INSTANCE, "dynamicQuery", "动态");
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender) {
        main(sender, 0);
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender, int index) {
        if (index < 0) {
            sender.sendMessage("就算是小韭菜也预测不到未来的事情！");
            return;
        }
        if (index < 0) {
            sender.sendMessage("就算是小韭菜也记不得那么久之前的事情了！");
            return;
        }
        try {
            String s = Jsoup.connect("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=233114659")
                    .ignoreContentType(true)
                    .execute()
                    .body();
            JSONObject jsonObject = JSON.parseObject(s);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray cards = data.getJSONArray("cards");
            JSONObject firstCard = cards.getJSONObject(index);

            JSONObject desc = firstCard.getJSONObject("desc");
            Integer type = desc.getInteger("type");
            Long timestamp = desc.getLong("timestamp") * 1000;
            // 2-带图动态 64-专栏 其他的暂时先不管
            if (type == 2) {
                JSONObject card = firstCard.getJSONObject("card");
                JSONObject item = card.getJSONObject("item");
                String description = item.getString("description");
                JSONArray pictures = item.getJSONArray("pictures");
                JSONObject picture = pictures.getJSONObject(0);

                InputStream is = new URL(picture.getString("img_src")).openConnection().getInputStream();
                Image image = Contact.uploadImage(sender.getGroup(), is);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.add("动态时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)) + "\n");
                builder.add(description);
                builder.add(image);
                sender.sendMessage(builder.build());
            } else if (type == 64) {
                JSONObject card = firstCard.getJSONObject("card");
                String title = card.getString("title");
                String summary = card.getString("summary");
                String image_urls = card.getJSONArray("image_urls").getString(0);

                InputStream is = new URL(image_urls).openConnection().getInputStream();
                Image image = Contact.uploadImage(sender.getGroup(), is);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.add("专栏时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)) + "\n");
                builder.add(image);
                builder.add(title + "\n");
                builder.add(summary + "...");
                sender.sendMessage(builder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
