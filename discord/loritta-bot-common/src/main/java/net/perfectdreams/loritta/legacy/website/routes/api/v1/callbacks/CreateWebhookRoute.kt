package net.perfectdreams.loritta.legacy.website.routes.api.v1.callbacks

import io.ktor.server.application.*
import mu.KotlinLogging
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class CreateWebhookRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/discord-webhook") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val hostHeader = call.request.hostFromHeader()
		val code = call.parameters["code"]

		val auth = TemmieDiscordAuth(
				net.perfectdreams.loritta.legacy.utils.loritta.discordConfig.discord.clientId,
				net.perfectdreams.loritta.legacy.utils.loritta.discordConfig.discord.clientSecret,
				code,
				"https://$hostHeader/api/v1/callbacks/discord-webhook",
				listOf("webhook.incoming")
		)

		val authExchangePayload = auth.doTokenExchange()
		call.respondJson(authExchangePayload["webhook"])
	}
}