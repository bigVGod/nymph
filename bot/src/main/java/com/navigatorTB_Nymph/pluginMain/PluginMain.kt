package com.navigatorTB_Nymph.pluginMain

import com.mayabot.nlp.module.summary.KeywordSummary
import com.mayabot.nlp.segment.Lexers
import com.navigatorTB_Nymph.Timer.DynamicsTimer
import com.navigatorTB_Nymph.Timer.GuessTimer
import com.navigatorTB_Nymph.command.composite.AI
import com.navigatorTB_Nymph.command.composite.GroupPolicy
import com.navigatorTB_Nymph.command.composite.WikiAzurLane
import com.navigatorTB_Nymph.command.simple.*
import com.navigatorTB_Nymph.data.Role
import com.navigatorTB_Nymph.data.UserPolicy
import com.navigatorTB_Nymph.data.UserResponsible
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginConfig.MySetting.prohibitedWord
import com.navigatorTB_Nymph.pluginData.*
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info
import java.util.*


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.navigatorTB_Nymph", name = "navigatorTB", version = "0.22.0"
    )
) {
    // 分词功能
    val LEXER = Lexers.coreBuilder().withPos() //词性标注功能
        .withPersonName() // 人名识别功能
//        .withNer() // 命名实体识别
        .build()

    // 关键词提取
    val KEYWORD_SUMMARY = KeywordSummary()

    @OptIn(MiraiExperimentalApi::class)
    override fun onEnable() {

        val timer = Timer();
        // 30秒轮巡动态
        timer.schedule(DynamicsTimer(), 0, 30000);
        // 每个小时+10次猜技能次数
        timer.schedule(GuessTimer(), 0, 3600000);

        MySetting.reload()

        MyPluginData.reload()
        UsageStatistics.reload()
        ActiveGroupList.reload()
        Article.reload()
        AiTemplate.reload()
        MirrorWorldUser.reload()

        Class.forName("org.sqlite.JDBC")

        if (MyPluginData.initialization) {  // 首次启动初始化数据库
            dataBastInit()
            MyPluginData.initialization = false
        } else {                            // 重置状态数据防止出现状态锁定
            MyPluginData.AcgImageRun.clear()
        }

        SignIn.register()           // 签到
        GroupPolicy.register()      // 群策略
        GroupWife.register()        // 群老婆
        Construction.register()     // 建造时间
        ShipMap.register()          // 打捞地图
        WikiAzurLane.register()     // 碧蓝Wiki
        CalculationExp.register()   // 经验计算器
        Birthday.register()         // 舰船下水日
        Roster.register()           // 碧蓝和谐名
        AI.register()               // 图灵数据库增删改查
        SimulateConstruction.register()// 模拟建造

        ScienceQuery.INSTANCE.register()
        SkinQuery.INSTANCE.register()
        Steal.INSTANCE.register()
        StealQuery.INSTANCE.register()
        RankQuery.INSTANCE.register()
        DateQuery.INSTANCE.register()
        DynamicQuery.INSTANCE.register()
        EquipQuery.INSTANCE.register()
        ShipGetter.INSTANCE.register()
        GuessSkill.INSTANCE.register()
//        Test.INSTANCE.register()

        MyHelp.register()           // 帮助功能

        this.globalEventChannel().subscribeAlways<Event> {
            when (this) {
                is MemberJoinEvent.Invite -> {
                    if (groupId in ActiveGroupList.user && memberJoinEvent(groupId) == 1 && group.botMuteRemaining <= 0)
                        group.sendMessage("哇啊,${invitor.nameCardOrNick}拐卖到了${member.nameCardOrNick}")
                }          // 邀请加入播报
                is MemberJoinEvent.Active -> {
                    if (groupId in ActiveGroupList.user && memberJoinEvent(groupId) == 1 && group.botMuteRemaining <= 0)
                        group.sendMessage("欢迎${member.nameCardOrNick}自投罗网")
                }          // 主动加入播报
                is MemberJoinEvent.Retrieve -> {
                    if (groupId in ActiveGroupList.user && memberJoinEvent(groupId) == 1 && group.botMuteRemaining <= 0)
                        group.sendMessage("欢迎群主${member.nameCardOrNick}回归")
                }        // 恢复加入播报
                is MemberLeaveEvent.Kick -> {
                    if (groupId in ActiveGroupList.user && memberLeave(groupId) == 1 && group.botMuteRemaining <= 0)
                        group.sendMessage("哇啊,${member.nameCardOrNick}(${member.id})被${(operator ?: bot).nameCardOrNick}鲨掉惹")
                }           // 成员移出播报
                is MemberLeaveEvent.Quit -> {
                    if (groupId in ActiveGroupList.user && memberLeave(groupId) == 1 && group.botMuteRemaining <= 0)
                        group.sendMessage("哇啊,${member.nameCardOrNick}(${member.id})自己跑掉惹")
                }           // 成员退群播报
                is BotInvitedJoinGroupRequestEvent -> {
                    accept()
                    ActiveGroupList.user.add(groupId)
                } // 入群审核
                is NudgeEvent -> {
                    nudge()
                }                    // 戳一戳
                is BotLeaveEvent.Kick -> {
                    cleanGroupInfo(group.id)
                    bot.getFriend(MySetting.AdminID)?.sendMessage("被移出群:${group.name}\nGroupID：${group.id}")
                }              // 退群 - 被移出群
                is BotLeaveEvent.Active -> {
                    logger.info { "###\n事件—主动退出群:\n- 群ID：${group.id}\n###" }
                    bot.getFriend(MySetting.AdminID)?.sendMessage("主动退出:${group.name}\nGroupID：${group.id}")
                }            // 退群 - 主动退出
                is BotLeaveEvent.Disband -> {
                    logger.info { "###\n事件—群被解散:\n- 群ID：${group.id}\n###" }
                    bot.getFriend(MySetting.AdminID)?.sendMessage("群被解散:${group.name}\nGroupID：${group.id}")
                }           // 退群 - 群被解散
                is BotJoinGroupEvent -> {
                    addGroupInfo(groupId)
                    bot.getFriend(MySetting.AdminID)?.sendMessage("GroupName:${group.name}\nGroupID：${groupId}\nPASS")
                }               // 加群后添加信息
//                is MemberJoinRequestEvent -> {
//                      group.sendMessage("$fromNick($fromId)申请加入本群")
//                }          // 他人入群申请
            }
        }

        // 聊天触发
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.LOWEST) {
            atBot {
                if (group.botMuteRemaining > 0 || group.id !in ActiveGroupList.user) return@atBot
                val filterMessageList: List<Message> = message.filter { it !is At }
                val filterMessageChain: MessageChain = filterMessageList.toMessageChain()
                if ((1..5).random() <= 2) {
                    if (filterMessageChain.content.trim().contains(
                            prohibitedWord.joinToString("|").toRegex()
                        ) && group.botPermission > this.sender.permission
                    ) {
                        this.sender.mute((300..900).random())
                        group.sendMessage(
                            arrayOf(
                                "给爷爬╰（‵□′）╯",
                                "爬远点(ノ｀Д)ノ",
                                "再您妈的见(#◠‿◠)",
                                "给大佬递口球~(￣▽￣)~*",
                                "可是，这值得吗(⊙o⊙)？",
                                "一条指令，一切都索然无味┑(￣Д ￣)┍",
                                "￣へ￣"
                            ).random()
                        )
                        if (sender !is AnonymousMember)
                            MirrorWorldUser.userData.getOrPut(sender.id) { Role(sender.nameCardOrNick) }.abuse()
                        return@atBot
                    }
                }
                AI.dialogue(subject, filterMessageChain.content.trim(), true)
                if (sender !is AnonymousMember)
                    MirrorWorldUser.userData.getOrPut(sender.id) { Role(sender.nameCardOrNick) }.chat()
            }
            atBot().not().invoke {
                var answerMap = GuessSkill.INSTANCE.guessAnswer;
                if (answerMap.containsKey(group.id) && answerMap[group.id] == message.content.trim()) {
                    subject.sendMessage(this.sender.nameCardOrNick + "猜对惹");
                    GuessSkill.INSTANCE.guessTrue(this.sender.id, this.sender.nameCardOrNick);
                    answerMap.remove(group.id);
                    return@invoke;
                }
                if (group.botMuteRemaining > 0 || group.id !in ActiveGroupList.user) return@invoke
                val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
                val policy = dbObject.selectOne(
                    "Policy", Triple("groupID", "=", "${group.id}"), "聊天触发:\nFile:PluginMain.Kt\tLine:276"
                ).run { UserPolicy(this) }
                dbObject.closeDB()
                val v1 = (1..100).random()
                val v2 = if (policy.acgImgAllowed == 1) (1..100).random() else 0
                if (v1 <= policy.triggerProbability)
                    AI.dialogue(subject, message.content.trim())
                if (v1 <= 99) return@invoke
                val supply = when (v2) {
                    in 1..7 -> 3
                    in 8..19 -> 2
                    in 20..46 -> 1
                    else -> 0
                }
                if (supply > 0) subject.sendMessage(AcgImage.getReplenishment(subject.id, supply))
            }
        }
        // 常驻任务
//        if (MySetting.resident) residentTask()

//        PluginMain.launch {
//            CronJob.start()
//        }

        ActiveGroupList.activationStatusUpdate(false)
    }

    private fun addGroupInfo(groupId: Long) {
        SQLiteJDBC(resolveDataPath("User.db")).apply {
            insert("Policy", arrayOf("groupID"), arrayOf("$groupId"), "加群数据录入:\nFile:PluginMain.kt\tLine:173")
            insert(
                "SubscribeInfo",
                arrayOf("groupID"),
                arrayOf("$groupId"),
                "加群数据录入:\nFile:PluginMain.Kt\tLine:174"
            )
            insert("ACGImg", arrayOf("groupID"), arrayOf("$groupId"), "加群数据录入:\nFile:PluginMain.Kt\tLine:180")
            insert(
                "Responsible",
                arrayOf("groupID"),
                arrayOf("$groupId"),
                "加群数据录入:\nFile:PluginMain.Kt\tLine:181"
            )
        }.closeDB()
    }

    private fun cleanGroupInfo(groupId: Long) {
        SQLiteJDBC(resolveDataPath("User.db")).run {
            val responsible = selectOne(
                "Responsible", Triple("groupID", "=", "$groupId"), "退群清理:\nFile:PluginMain.Kt\tLine:195"
            ).let { UserResponsible(it) }
            delete("Policy", Pair("groupID", "$groupId"), "退群数据清理:\nFile:PluginMain.Kt\tLine:200")
            delete("SubscribeInfo", Pair("groupID", "$groupId"), "退群数据清理:\nFile:PluginMain.Kt\tLine:201")
            delete("Responsible", Pair("groupID", "$groupId"), "退群数据清理:\nFile:PluginMain.Kt\tLine:202")
            delete("ACGImg", Pair("groupID", "$groupId"), "退群数据清理:\nFile:PluginMain.Kt\tLine:203")
            closeDB()
            logger.info { "###\n事件—被移出群:\n- 群ID：${groupId}\n- 相关群负责人：${responsible.principalID}\n###" }
        }
    }

    private suspend fun NudgeEvent.nudge() {
        if (target == bot && from != bot) {
            runCatching {
                if ((1..5).random() <= 4) {
                    subject.sendMessage(
                        arrayOf(
                            "指挥官，请不要做出这种行为",
                            "这只是全息交互界面",
                            "指挥官，请专心于工作",
                            "全息投影是不会被接触到的",
                            "指挥官，我一直陪着你哦",
                            "可望不可及",
                            "请不要试图干扰全息投影",
                            "传输...信.号...数据...干扰..."
                        ).random()
                    )
                } else {
                    from.nudge().sendTo(subject)
                    subject.sendMessage("戳回去")
                }
            }.onFailure {
                logger.info { "File:PluginMain.kt\tLine:241\n发送消息失败，在该群被禁言" }
            }.onSuccess {
                MirrorWorldUser.userData.getOrPut(from.id) { Role(from.nameCardOrNick) }.nudge()
            }

        }
    }

    private fun memberLeave(groupId: Long): Int {
        val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
        val policy = dbObject.selectOne(
            "Policy", Triple("groupID", "=", "$groupId"), "退群播报\nFile:PluginMain.kt\tLine:151"
        ).run { UserPolicy(this) }
        dbObject.closeDB()
        return policy.groupNotification
    }

    private fun memberJoinEvent(groupId: Long): Int {
        val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
        val policy = dbObject.selectOne(
            "Policy", Triple("groupID", "=", "$groupId"), "入群播报\nFile:PluginMain.kt\tLine:133"
        ).run { UserPolicy(this) }
        dbObject.closeDB()
        return policy.groupNotification
    }

    private fun dataBastInit() {
        SQLiteJDBC(resolveDataPath("User.db")).apply {
            createTable(
                """
                CREATE TABLE "ACGImg" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "score"	INTEGER NOT NULL DEFAULT 0,
                "date"	NUMERIC NOT NULL DEFAULT 0,
                PRIMARY KEY("groupID")
                );
                """.trimIndent(), "初始化数据库\nFile:PluginMain.kt\tLine:459"
            )
            createTable(
                """
                CREATE TABLE "Policy" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "tellTimeMode"	INTEGER NOT NULL DEFAULT 0,
                "dailyReminderMode"	INTEGER NOT NULL DEFAULT 0,
                "teaching"	INTEGER NOT NULL DEFAULT 0,
                "triggerProbability"	INTEGER NOT NULL DEFAULT 33,
                "acgImgAllowed"	INTEGER NOT NULL DEFAULT 0,
                "undisturbed"	INTEGER NOT NULL DEFAULT 0,
                "groupNotification"	INTEGER NOT NULL DEFAULT 0
                );
                """.trimIndent(), "初始化数据库\nFile:PluginMain.kt\tLine:470"
            )
            createTable(
                """
                CREATE TABLE "Responsible" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "principalID"	NUMERIC NOT NULL DEFAULT 0,
                "active"	INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY("groupID")
                );
                """.trimIndent(), "初始化数据库\nFile:PluginMain.kt\tLine:485"
            )
            createTable(
                """
                CREATE TABLE "SubscribeInfo" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "azurLane"	INTEGER DEFAULT 0.0,
                "arKnights"	INTEGER DEFAULT 0.0,
                "fateGrandOrder"	INTEGER DEFAULT 0.0,
                "genShin"	INTEGER DEFAULT 0.0
                );
                """.trimIndent(), "初始化数据库\nFile:PluginMain.kt\tLine:496"
            )
        }.closeDB()
        SQLiteJDBC(resolveDataPath("AI.db")).apply {
            createTable(
                """
            CREATE TABLE "Corpus" (
            "id"	INTEGER NOT NULL UNIQUE,
            "answer"	TEXT NOT NULL,
            "question"	TEXT NOT NULL,
            "keys"	TEXT NOT NULL,
            "fromGroup"	INTEGER NOT NULL,
            PRIMARY KEY("id" AUTOINCREMENT)
            );
            """.trimIndent(), "初始化数据库\nFile:PluginMain.kt\tLine:509"
            )
        }.closeDB()
        logger.info("初始化基础数据库完成")
        logger.warning("请自行检查 AssetData.db 是否存在于 Data 目录")
    }
}