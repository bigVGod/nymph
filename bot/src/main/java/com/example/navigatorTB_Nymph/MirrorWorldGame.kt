package com.example.navigatorTB_Nymph

import com.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object MirrorWorldGame {

    fun register() {
        PlayerInfo.register()
        PlayerBuild.register()
        PvPBattle.register()
    }

    fun unregister() {
        PlayerInfo.unregister()
        PlayerBuild.unregister()
        PvPBattle.unregister()
    }

    object PlayerInfo : SimpleCommand(
        PluginMain, "PlayerInfo", "我的信息",
        description = "用户信息"
    ) {

        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().gamerInfo(this)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PlayerBuild : SimpleCommand(
        PluginMain, "PlayerBuild", "建立角色",
        description = "玩家角色建立"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().characterCreation(this)
            } else sendMessage("缺少依赖DLC")
        }
    }


    object PvPBattle : SimpleCommand(
        PluginMain, "PvP", "玩家对战",
        description = "玩家对战"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(user: User) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().pvp(this, user)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PlayerTransfer : SimpleCommand(
        PluginMain, "TransferP", "金币转账",
        description = "玩家金币转移"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(user: User, amount: Int) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().transfer(this, user, amount)
            } else sendMessage("缺少依赖DLC")
        }
    }
}