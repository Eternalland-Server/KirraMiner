package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.module.chat.colored

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraMiner", aliases = ["km", "kminer"])
object Commands {

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7正在重载...".colored())
            Loader.i()
            sender.sendMessage("&c[System] &7重载完毕.".colored())
        }
    }

    @CommandBody
    val add = subCommand {
        dynamic(commit = "oreID") {
            dynamic(commit = "refreshTime") {
                execute<Player> { player, context, _ ->
                    val oreId = context.get(1)
                    val refreshTime = IntInterval.fromString(context.get(2))
                    if (refreshTime == null) {
                        player.sendMessage("&c[System] &7刷新时间格式不正确.".colored())
                        return@execute
                    }
                    Loader.addOre(oreId, player.location, refreshTime)
                }
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7当前矿物列表.")
            sender.sendMessage("&c[System] &7${KirraMinerAPI.ores.keys}")
        }
    }
}