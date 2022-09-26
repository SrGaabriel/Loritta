package net.perfectdreams.loritta.legacy.commands.vanilla.social

import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.tables.BomDiaECiaWinners
import org.jetbrains.exposed.sql.select

class BomDiaECiaStatusCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("bomdiaecia status", "bd&c status", "bdc status"), CommandCategory.SOCIAL) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.bomdiaeciastatus"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val user = user(0) ?: this.message.author

            val votes = loritta.newSuspendedTransaction {
                BomDiaECiaWinners.select { BomDiaECiaWinners.userId eq user.id }.count()
            }

            if (user == this.message.author) {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.youWins", votes]
                    )
                )
            } else {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.userWins", user.asMention, votes]
                    )
                )
            }
        }
    }
}