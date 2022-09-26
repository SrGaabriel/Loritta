package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedChristmas2019Points : LongIdTable() {
	val user = reference("user", Profiles).index()
	val guildId = long("guild")
	val channelId = long("channel")
	val messageId = long("message")
}