package net.sakuragame.eternal.kirraminer.ore

data class DigMetadata(
    val weight: Int,
    val oreIndex: String,
    val entityName: String,
    val digLevel: Int,
    val digTime: Int,
    val digResult: DigResult,
    val providedExp: Double,
    val costDurability: IntRange
)