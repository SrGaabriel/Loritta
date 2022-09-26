package net.perfectdreams.loritta.legacy.commands.vanilla.magic

import net.perfectdreams.loritta.legacy.network.Databases
import net.perfectdreams.loritta.legacy.tables.GuildProfiles
import net.perfectdreams.loritta.legacy.tables.Mutes
import net.perfectdreams.loritta.legacy.tables.ServerConfigs
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.tables.servers.Giveaways
import net.perfectdreams.loritta.legacy.utils.DiscordUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object PurgeInactiveGuildsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive guilds <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "guilds")
			return@task false

		val badGuildData = mutableListOf<Long>()

		transaction(Databases.loritta) {
			ServerConfigs.selectAll().forEach {
				if (it[ServerConfigs.id].value != -1L) { // -1 = coisas na DM
					val shardId = DiscordUtils.getLorittaClusterForGuildId(it[ServerConfigs.id].value)

					if (shardId.id == net.perfectdreams.loritta.legacy.LorittaLauncher.loritta.lorittaCluster.id) {
						val guild = lorittaShards.getGuildById(it[ServerConfigs.id].value)

						if (guild == null)
							badGuildData.add(it[ServerConfigs.id].value)
					}
				}
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando guilds inativas..."
					)
			)

			val idList = badGuildData.chunked(30_000)

			for (ids in idList) {
				transaction(Databases.loritta) {
					GuildProfiles.deleteWhere {
						GuildProfiles.guildId inList ids
					}

					ServerConfigs.deleteWhere { ServerConfigs.id inList ids }

					Giveaways.deleteWhere {
						Giveaways.guildId inList ids
					}

					Mutes.deleteWhere {
						Mutes.guildId inList ids
					}
				}
			}

			reply(
					LorittaReply(
							"Feito! Todas as guilds inativas foram deletadas!"
					)
			)
		} else {
			reply(
					LorittaReply(
							"Existem **${badGuildData.size} guilds** inativas (Deveriam existir no cluster atual... mas cadê?)"
					)
			)
		}

		return@task true
	}
}
