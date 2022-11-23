package net.sakuragame.eternal.kirraminer

import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraMiner : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    val plugin by lazy {
        BukkitPlugin.getInstance()
    }
}