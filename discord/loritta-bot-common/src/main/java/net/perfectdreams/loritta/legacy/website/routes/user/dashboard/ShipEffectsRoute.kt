package net.perfectdreams.loritta.legacy.website.routes.user.dashboard

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.legacy.dao.ShipEffect
import net.perfectdreams.loritta.legacy.tables.ShipEffects
import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and

class ShipEffectsRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/ship-effects") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)

		val userId = userIdentification.id

		val user = lorittaShards.retrieveUserById(userId)!!
		val lorittaProfile = net.perfectdreams.loritta.legacy.utils.loritta.getOrCreateLorittaProfile(userId)

		variables["profileUser"] = user
		variables["lorittaProfile"] = lorittaProfile
		variables["saveType"] = "ship_effects"
		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)
		val shipEffects = loritta.newSuspendedTransaction {
			ShipEffect.find {
				(ShipEffects.buyerId eq user.idLong) and
						(ShipEffects.expiresAt greaterEq System.currentTimeMillis())
			}.toMutableList()
		}

		variables["ship_effects_json"] =
				gson.toJson(
						shipEffects.map {
							jsonObject(
									"buyerId" to it.buyerId,
									"user1Id" to it.user1Id,
									"user2Id" to it.user2Id,
									"editedShipValue" to it.editedShipValue,
									"expiresAt" to it.expiresAt
							)
						}
				)

		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)

		call.respondHtml(evaluate("profile_dashboard_ship_effects.html", variables))
	}
}