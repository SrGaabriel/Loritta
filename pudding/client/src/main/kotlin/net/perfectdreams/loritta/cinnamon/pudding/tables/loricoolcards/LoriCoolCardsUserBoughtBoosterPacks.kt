package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsUserBoughtBoosterPacks : LongIdTable() {
    val user = long("user").index()
    val event = reference("event", LoriCoolCardsEvents).index()
    val boughtAt = timestampWithTimeZone("bought_at")
}