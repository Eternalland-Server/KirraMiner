package net.sakuragame.eternal.kirraminer

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.expansion.createHelper
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraMiner", aliases = ["km", "kminer"])
object Commands {

    private val summonWand by lazy {
        buildItem(Material.STICK) {
            name = "&e&l矿物召唤魔杖".colored()
            shiny()
        }
    }

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
    val getAllBlock = subCommand {
        execute<Player> { sender, _, _ ->
            KirraMinerAPI.ores
                .filter { it.value.loc == null }
                .filter { it.key.contains("dummy") }
                .map { it.value.digMetadata.name }
                .forEach {
                    sender.performCommand("km getBlock $it")
                }
        }
    }

    @CommandBody
    val getBlock = subCommand {
        dynamic(commit = "id") {
            suggestion<Player> { _, _ ->
                KirraMinerAPI.ores
                    .filter { it.value.loc == null }
                    .filter { it.key.contains("dummy") }
                    .map { it.value.digMetadata.name }
            }
            execute<Player> { sender, context, _ ->
                val id = context.get(1)
                val ore = KirraMinerAPI.ores.values.firstOrNull { it.digMetadata.name == id } ?: run {
                    sender.sendMessage("&c[System] &7矿物不存在!".colored())
                    return@execute
                }
                sender.inventory.addItem(summonWand.clone().modifyMeta<ItemMeta> {
                    displayName = "$displayName (${ore.digMetadata.oreIndex} - ${ore.digMetadata.name})"
                    modifyLore {
                        add("${ore.digMetadata.oreIndex} - ${ore.digMetadata.name}")
                    }
                })
            }
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
    val test = subCommand {
        dynamic(commit = "id") {
            execute<Player> { player, _, argument ->
                KirraMinerAPI.getNearestOreOfPlayer(player, argument)
            }
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