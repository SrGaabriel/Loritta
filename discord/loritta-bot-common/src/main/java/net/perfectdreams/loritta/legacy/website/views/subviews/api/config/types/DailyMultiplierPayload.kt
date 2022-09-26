package net.perfectdreams.loritta.legacy.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.google.gson.JsonObject
import net.perfectdreams.loritta.legacy.dao.DonationConfig
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

class DailyMultiplierPayload : ConfigPayloadType("daily_multiplier") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
			}
			donationConfig.dailyMultiplier = payload["dailyMultiplier"].bool

			serverConfig.donationConfig = donationConfig
		}
	}
}