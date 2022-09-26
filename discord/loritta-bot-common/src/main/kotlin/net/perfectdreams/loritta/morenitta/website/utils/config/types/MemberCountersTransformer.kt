package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.listeners.DiscordListener
import net.perfectdreams.loritta.morenitta.utils.counter.CounterThemes
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MemberCounterChannelConfig
import net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class MemberCountersTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "member_counter"
    override val configKey: String = "memberCounters"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val memberCounters = loritta.newSuspendedTransaction {
            MemberCounterChannelConfig.find {
                MemberCounterChannelConfigs.guild eq serverConfig.id
            }.toList()
        }

        val array = jsonArray()

        for (counter in memberCounters) {
            array.add(
                jsonObject(
                    "channelId" to counter.channelId,
                    "padding" to counter.padding,
                    "theme" to counter.theme.name,
                    "topic" to counter.topic
                )
            )
        }

        return array
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            MemberCounterChannelConfigs.deleteWhere {
                MemberCounterChannelConfigs.guild eq serverConfig.id
            }
        }

        val entries = payload["entries"].array

        for (entry in entries) {
            val id = entry["id"].nullLong ?: continue

            val obj = entry.obj
            if (obj.has("memberCounterConfig")) {
                val memberCounterConfig = obj["memberCounterConfig"].obj
                val topic = memberCounterConfig["topic"].string
                val theme = memberCounterConfig["theme"].string
                val padding = memberCounterConfig["padding"].int

                loritta.newSuspendedTransaction {
                    MemberCounterChannelConfigs.insert {
                        it[MemberCounterChannelConfigs.guild] = serverConfig.id
                        it[MemberCounterChannelConfigs.channelId] = id
                        it[MemberCounterChannelConfigs.topic] = topic
                        it[MemberCounterChannelConfigs.theme] = CounterThemes.valueOf(theme)
                        it[MemberCounterChannelConfigs.padding] = padding
                    }
                }
            }
        }

        // Queue update is the list is not empty
        if (entries.size() != 0)
            DiscordListener.queueTextChannelTopicUpdates(loritta, guild, serverConfig)
    }
}