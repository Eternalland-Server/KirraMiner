package net.sakuragame.eternal.kirraminer

import org.bukkit.Bukkit
import org.bukkit.Location

@Suppress("SpellCheckingInspection")
fun printToConsole(message: String) = Bukkit.getConsoleSender().sendMessage("[KirraMiner] $message")

fun Location.parseToString(): String {
    return "${world?.name}@$x@$y@$z@$yaw@$pitch"
}

fun String.parseToLoc(): Location? {
    val split = split("@")
    if (split.size != 6) {
        return null
    }
    val world = Bukkit.getWorld(split[0]) ?: return null
    val x = split[1].toDoubleOrNull() ?: return null
    val y = split[2].toDoubleOrNull() ?: return null
    val z = split[3].toDoubleOrNull() ?: return null
    val yaw = split[4].toFloatOrNull() ?: return null
    val pitch = split[5].toFloatOrNull() ?: return null
    return Location(world, x, y, z, yaw, pitch)
}