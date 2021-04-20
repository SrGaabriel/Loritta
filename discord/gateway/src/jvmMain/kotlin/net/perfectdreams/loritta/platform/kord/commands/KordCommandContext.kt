package net.perfectdreams.loritta.platform.kord.commands

import net.perfectdreams.loritta.common.builder.LorittaMultiReplyBuilder
import net.perfectdreams.loritta.common.builder.LorittaReplyBuilder
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.kord.LorittaKord
import net.perfectdreams.loritta.platform.kord.entities.KordUser

class KordCommandContext(
    override val loritta: LorittaKord,
    locale: BaseLocale,
    user: User,
    channel: MessageChannel
) : CommandContext(loritta, locale, user, channel) {
    override suspend fun sendReply(block: LorittaReplyBuilder.() -> Unit) {
        val builder = LorittaReplyBuilder().apply(block)
        sendMessage("${builder.prefix} **|** ${builder.content}")
    }

    override suspend fun sendMultiReply(block: LorittaMultiReplyBuilder.() -> Unit) {
        val builder = LorittaMultiReplyBuilder().apply(block)
        sendMessage(builder.replies.joinToString("\n") { "${it.prefix} **|** ${it.content}" })
    }
}