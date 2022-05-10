package net.sakuragame.eternal.kirraminer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.expansion.createHelper
import taboolib.module.chat.TellrawJson
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
    val pasteLoc = subCommand {
        execute<Player> { player, _, _ ->
            val loc = player.location.parseToString()
            player.sendMessage("&c[System] &7正在打印信息".colored())
            println(loc)
            TellrawJson()
                .append("&c[System] &7$loc".colored())
                .suggestCommand(loc)
                .sendTo(adaptCommandSender(player))
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