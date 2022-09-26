package net.perfectdreams.loritta.legacy.commands.vanilla.economy

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.DateUtils
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class DailyCommand : AbstractCommand("daily", listOf("diário", "bolsafamilia", "bolsafamília"), CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.daily.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "daily")
		
		// 1. Pegue quando o daily foi pego da última vez
		// 2. Pegue o tempo de quando seria amanhã
		// 3. Compare se o tempo atual é maior que o tempo de amanhã
		val (canGetDaily, tomorrow) = context.lorittaUser.profile.canGetDaily()

		if (!canGetDaily) {
			context.reply(
				LorittaReply(
					locale["commands.command.daily.pleaseWait", DateUtils.formatDateDiff(tomorrow, locale)],
					Constants.ERROR
				),
				LorittaReply(
					locale["commands.command.daily.pleaseWaitBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
					"\uD83D\uDCB3"
				)
			)
			return
		}

		val url = if (context.isPrivateChannel)
			"${loritta.instanceConfig.loritta.website.url}daily"
		else // Used for daily multiplier priority
			"${loritta.instanceConfig.loritta.website.url}daily?guild=${context.guild.idLong}"

		context.reply(
			LorittaReply(
				locale["commands.command.daily.dailyLink", url],
				Emotes.LORI_RICH
			),
			LorittaReply(
				context.locale["commands.command.daily.dailyWarning", "${loritta.instanceConfig.loritta.website.url}guidelines"],
				Emotes.LORI_BAN_HAMMER,
				mentionUser = false
			),
			LorittaReply(
				locale["commands.command.daily.dailyLinkBuySonhos", "<${loritta.instanceConfig.loritta.website.url}user/@me/dashboard/bundles>"],
				"\uD83D\uDCB3",
				mentionUser = false
			)
		)
	}
}