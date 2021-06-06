/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info


@MiraiExperimentalApi
@ConsoleExperimentalApi
object Test : SimpleCommand(
    PluginMain, "Test", "测试",
    description = "功能测试命令"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(string: String) {
        PluginMain.logger.info { "测试命令执行" }
        sendMessage("This is test,input is $string")
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(image: Image) {
        PluginMain.logger.info { "测试命令执行" }
        sendMessage("this is test,Image downloadURL is ${image.queryUrl()}")
    }

    @Handler
    fun MemberCommandSenderOnMessage.main() {
        PluginMain.logger.info {
            "测试命令执行"
        }

//        minesweeperGame.flag((1..10).random(), (1..10).random())
//        subject.sendImage(minesweeperGame.getImage())
    }

    @Handler
    fun MemberCommandSenderOnMessage.main(x: Int, y: Int) {
        PluginMain.logger.info {
            "测试命令执行"
        }
    }

    @Handler
    fun MemberCommandSenderOnMessage.main(x: Int, y: Int, flag: Int) {
        PluginMain.logger.info {
            "测试命令执行"
        }
    }
}

