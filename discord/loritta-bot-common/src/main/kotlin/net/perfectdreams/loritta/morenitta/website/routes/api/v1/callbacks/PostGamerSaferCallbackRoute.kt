package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferSuccessfulVerifications
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferAdditionalData
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GuildInviteEvent
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.*

class PostGamerSaferCallbackRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/gamersafer") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		logger.info { "Received GamerSafer callback!" }
		val event = Json.decodeFromString<GuildInviteEvent>(call.receiveText().also { logger.info { it } })
		val (providerId, guildId, additionalDataAsJsonBase64) = event.payload.internalId.split("|||")
		val additionalData = Json.decodeFromString<GamerSaferAdditionalData>(Base64.getDecoder().decode(additionalDataAsJsonBase64.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))

		loritta.transaction {
			GamerSaferGuildMembers.insert {
				it[GamerSaferGuildMembers.guild] = guildId.toLong()
				it[GamerSaferGuildMembers.discordUser] = additionalData.userId
				it[GamerSaferGuildMembers.gamerSaferUser] = event.payload.guildMemberId
			}
		}

		/* if (token != "abc") {
			call.respondJson("{}", HttpStatusCode.Unauthorized)
			return
		}

		val verifyId = call.parameters["verifyId"]?.toLong()
		logger.info { "Verify ID: $verifyId" }

		if (verifyId == null) {
			call.respondJson("{}", HttpStatusCode.NotFound)
			return
		}

		loritta.transaction {
			val verificationUserData = GamerSaferRequiresVerificationUsers.select {
				GamerSaferRequiresVerificationUsers.id eq verifyId
			}.firstOrNull()

			if (verificationUserData == null) {
				call.respondJson("{}", HttpStatusCode.NotFound)
				return@transaction
			}

			GamerSaferRequiresVerificationUsers.deleteWhere { GamerSaferRequiresVerificationUsers.id eq verifyId }

			GamerSaferSuccessfulVerifications.insert {
				it[GamerSaferSuccessfulVerifications.user] = verificationUserData[GamerSaferRequiresVerificationUsers.user]
				it[GamerSaferSuccessfulVerifications.guild] = verificationUserData[GamerSaferRequiresVerificationUsers.guild]
				it[GamerSaferSuccessfulVerifications.role] = verificationUserData[GamerSaferRequiresVerificationUsers.role]
				it[GamerSaferSuccessfulVerifications.verifiedAt] = Instant.now()
			}

			val guild = loritta.lorittaShards.getGuildById(verificationUserData[GamerSaferRequiresVerificationUsers.guild])

			if (guild == null) {
				call.respondJson("{}", HttpStatusCode.NotFound)
				return@transaction
			}

			guild.addRoleToMember(UserSnowflake.fromId(verificationUserData[GamerSaferRequiresVerificationUsers.user]), guild.getRoleById(verificationUserData[GamerSaferRequiresVerificationUsers.role])!!)
				.await()
		}

		loritta.gamerSaferWaitingForCallbacks[verifyId]?.send(Unit)
		loritta.gamerSaferWaitingForCallbacks.remove(verifyId)  */

		call.respondText("{}")
	}
}