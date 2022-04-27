package net.sakuragame.eternal.kirraminer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraMiner", aliases = ["km", "kminer"])
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7正在重载...".colored())
            KirraMiner.conf.reload()
            Loader.i()
            sender.sendMessage("&c[System] &7重载完毕.".colored())
        }
    }

    @CommandBody
    val pasteLocToConsole = subCommand {
        execute<Player> { player, _, _ ->
            player.sendMessage("&c[System] &7已将信息打印至后台.".colored())
            println("坐标: ${player.location.parseToString()}")
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7当前矿物列表.".colored())
            sender.sendMessage("&c[System] &7${KirraMinerAPI.ores.keys}".colored())
        }
    }
}