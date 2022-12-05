package com.navigatorTB_Nymph.command.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navigatorTB_Nymph.pluginMain.PluginMain;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RankQuery extends JSimpleCommand {
    @NotNull
    public static final RankQuery INSTANCE;

    public static final Map<String, Integer> serverMap = new HashMap<>();

    static {
        INSTANCE = new RankQuery();
        serverMap.put("总榜", 0);
        serverMap.put("莱茵演习", 1);
        serverMap.put("巴巴罗萨", 2);
        serverMap.put("霸王行动", 3);
        serverMap.put("冰山行动", 4);
        serverMap.put("彩虹计划", 5);
        serverMap.put("发电机计划", 6);
        serverMap.put("瞭望台行动", 7);
        serverMap.put("十字路口行动", 8);
        serverMap.put("朱诺行动", 9);
        serverMap.put("杜立特空袭", 10);
        serverMap.put("地狱犬行动", 11);
        serverMap.put("开罗宣言", 12);
        serverMap.put("奥林匹克行动", 13);
        serverMap.put("小王冠行动", 14);
        serverMap.put("波兹坦公告", 15);
        serverMap.put("白色方案", 16);
        serverMap.put("瓦尔基里行动", 17);
        serverMap.put("曼哈顿计划", 18);
        serverMap.put("八月风暴", 19);
        serverMap.put("秋季旅行", 20);
        serverMap.put("水星行动", 21);
        serverMap.put("莱茵河卫兵", 22);
    }

    private RankQuery() {
        super(PluginMain.INSTANCE, "rankQuery", "战力榜");
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender) {
        main(sender, "朱诺行动");
    }

    @Handler
    public final void main(@NotNull MemberCommandSenderOnMessage sender, @NotNull String serverName) {
        Integer server;
        if ((server = serverMap.get(serverName)) == null) {
            sender.sendMessage("查询的服务器名称有误!");
        } else {
            JSONObject param = new JSONObject();
            param.put("rankType", 0);
            param.put("serverId", server);
            HttpPost httpPost = new HttpPost("https://al.pelom.cn/api/rank");
            CloseableHttpClient client = HttpClients.createDefault();
            StringEntity entity = new StringEntity(param.toString(), "UTF-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            try {
                StringBuilder message = new StringBuilder();
                message.append(serverName).append(" 的战力榜前20：\n");
                HttpResponse response = client.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONArray result = JSON.parseArray(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    int i = 1;
                    for (Object o : result) {
                        JSONArray array = (JSONArray) o;
                        String name = array.get(1).toString();
                        String tab;
                        if (getStrLength(name) >= 14) {
                            tab = "\t";
                        } else if (getStrLength(name) >= 9) {
                            tab = "\t\t";
                        } else if (getStrLength(name) >= 4) {
                            tab = "\t\t\t";
                        } else {
                            tab = "\t\t\t\t";
                        }
                        message.append(String.format("%2d.%s(Lv.%d)%s%d%n", i++, name, array.get(3), tab, array.get(2)));
                    }
                    sender.sendMessage(message.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getStrLength(String str) {
        int strLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        for (int i = 0; i < str.length(); i++) {
            //判断是否是中文字符
            if (str.substring(i, i + 1).matches(chinese)) {
                strLength += 2;
            } else {
                strLength += 1;
            }
        }
        return strLength;
    }
}
