package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedTwitchAccounts : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val twitchUserId = long("twitch_user_id").index()
    val message = text("message")
    val webhookUrl = text("webhook_url").nullable()
    val addedAt = timestampWithTimeZone("added_at").nullable()
    val editedAt = timestampWithTimeZone("edited_at").nullable()
}