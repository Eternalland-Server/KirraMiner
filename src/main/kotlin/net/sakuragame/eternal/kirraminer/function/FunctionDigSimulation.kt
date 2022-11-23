package net.sakuragame.eternal.kirraminer.function

import net.minecraft.server.v1_12_R1.TileEntitySkull
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld

object FunctionDigSimulation {

    fun getOreFromBlockData(block: Block): Ore? {
        if (block.type != Material.SKULL) {
            return null
        }
        val entity = (block.world as CraftWorld).getTileEntityAt(block.x, block.y, block.z) as? TileEntitySkull ?: return null
        val gameProfile = entity.gameProfile ?: return null
        val name = gameProfile.name
        return KirraMinerAPI.ores.values.find { it.digMetadata.oreIndex == name }
    }
}