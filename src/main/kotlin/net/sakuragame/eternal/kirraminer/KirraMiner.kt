package net.sakuragame.eternal.kirraminer

import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.io.File

@Suppress("SpellCheckingInspection")
object KirraMiner : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val oresFile: ConfigFile
        get() {
            val file = File(plugin.dataFolder, "ores.yml")
            if (!file.exists()) {
                file.createNewFile()
            }
            return Configuration.loadFromFile(file)
        }

    override fun onEnable() {
        KirraMinerAPI.recycleAllOres()
    }

    override fun onDisable() {
        KirraMinerAPI.recycleAllOres()
    }
}