package net.perfectdreams.loritta.legacy.website.views.subviews.api.config.types

import com.google.gson.JsonObject
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession

class EconomyPayload : ConfigPayloadType("economy") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		// applyReflection(payload.economyConfig)
	}
}