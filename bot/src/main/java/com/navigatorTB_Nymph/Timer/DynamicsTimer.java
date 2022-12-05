package com.navigatorTB_Nymph.Timer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navigatorTB_Nymph.pluginConfig.MySetting;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jsoup.Jsoup;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class DynamicsTimer extends TimerTask {
    private static Long lastDate = new Date().getTime();

    @Override
    public void run() {
        try {
            Bot bot = Bot.getInstance(MySetting.INSTANCE.getBotID());
            ContactList<Group> groups = bot.getGroups();

            String s = Jsoup.connect("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=233114659")
                    .ignoreContentType(true)
                    .execute()
                    .body();
            JSONObject jsonObject = JSON.parseObject(s);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray cards = data.getJSONArray("cards");
            JSONObject firstCard = cards.getJSONObject(0);

            JSONObject desc = firstCard.getJSONObject("desc");
            Integer type = desc.getInteger("type");
            Long timestamp = desc.getLong("timestamp") * 1000;
            // type=2是带图动态 其他的暂时先不管
            if (lastDate < timestamp) {
                if (type == 2) {
                    lastDate = timestamp;
                    JSONObject card = firstCard.getJSONObject("card");
                    JSONObject item = card.getJSONObject("item");
                    String description = item.getString("description");
                    JSONArray pictures = item.getJSONArray("pictures");
                    JSONObject picture = pictures.getJSONObject(0);

                    InputStream is = new URL(picture.getString("img_src")).openConnection().getInputStream();
                    Image image = Contact.uploadImage(groups.iterator().next(), is);
                    for (Group group : groups) {
                        MessageChainBuilder builder = new MessageChainBuilder();
                        builder.add("动态时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)) + "\n");
                        builder.add(description);
                        builder.add(image);
                        group.sendMessage(builder.build());
                    }
                } else if (type == 64) {
                    lastDate = timestamp;
                    JSONObject card = firstCard.getJSONObject("card");
                    String title = card.getString("title");
                    String summary = card.getString("summary");
                    String image_urls = card.getJSONArray("image_urls").getString(0);

                    InputStream is = new URL(image_urls).openConnection().getInputStream();
                    Image image = Contact.uploadImage(groups.iterator().next(), is);
                    for (Group group : groups) {
                        MessageChainBuilder builder = new MessageChainBuilder();
                        builder.add("专栏时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)) + "\n");
                        builder.add(image);
                        builder.add(title + "\n");
                        builder.add(summary);
                        group.sendMessage(builder.build());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
