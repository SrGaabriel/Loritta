package net.perfectdreams.loritta.legacy.commands.vanilla.images

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.imageData
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CocieloChavesCommand(m: LorittaDiscord) : DiscordAbstractCommandBase(
	m,
	listOf("cocielochaves"),
	CommandCategory.VIDEOS
) {
	val heavyGenerationMutexMap = Caffeine.newBuilder()
		.expireAfterAccess(1L, TimeUnit.MINUTES)
		.build<Long, Mutex>()
		.asMap()

	override fun command() = create {
		localizedDescription("commands.command.cocielochaves.description")
		localizedExamples("commands.command.cocielochaves.examples")

		executesDiscord {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "chaves cocielo")

			val mutex = heavyGenerationMutexMap.getOrPut(this.guild.idLong) { Mutex() }

			if (mutex.isLocked)
				fail(locale["commands.commandAlreadyBeingExecutedInGuild"])

			mutex.withLock {
				val imagesData = (0 until 5).map {
					imageData(it) ?: run {
						if (args.isEmpty())
							explainAndExit()
						else
							fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
					}
				}

				val response = loritta.httpWithoutTimeout.post("https://gabriela.loritta.website/api/v1/videos/cocielo-chaves") {
					setBody(
						buildJsonObject {
							putJsonArray("images") {
								for (data in imagesData)
									add(data)
							}
						}.toString()
					)
				}

				// If the status code is between 400.499, then it means that it was (probably) a invalid input or something
				if (response.status.value in 400..499)
					fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
				else if (response.status.value !in 200..299) // This should show the error message because it means that the server had a unknown error
					fail(locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING], "\uD83E\uDD37")

				sendFile(response.body<InputStream>(), "cocielo_chaves.mp4")
			}
		}
	}
}