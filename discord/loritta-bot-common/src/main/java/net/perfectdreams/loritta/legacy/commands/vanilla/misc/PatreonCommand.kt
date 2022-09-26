package net.perfectdreams.loritta.legacy.commands.vanilla.misc

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.utils.Emotes
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor", "doar", "donate"), category = CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.donate.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val patrons = "Veja todos os doadores em https://loritta.website/donate (tem tantos doadores que não cabe nesta mensagem! ${Emotes.LORI_CRYING})"

		val embed = EmbedBuilder().apply {
			setThumbnail("https://loritta.website/assets/img/fanarts/Loritta_-_Heathecliff.png")
			setTitle("${Emotes.LORI_RICH} ${context.locale["commands.command.donate.thanks"]}")
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.locale["commands.command.donate.doYouWannaHelp"], context.locale["commands.command.donate.howToHelp", "${loritta.instanceConfig.loritta.website.url}donate", Emotes.LORI_HEART, Emotes.LORI_CRYING, Emotes.LORI_RICH], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}