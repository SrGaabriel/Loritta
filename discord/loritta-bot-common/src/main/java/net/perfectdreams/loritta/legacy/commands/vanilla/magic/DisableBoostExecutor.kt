package net.perfectdreams.loritta.legacy.commands.vanilla.magic

import net.perfectdreams.loritta.legacy.utils.NitroBoostUtils
import net.perfectdreams.loritta.legacy.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.legacy.platform.discord.legacy.entities.jda.JDAUser

object DisableBoostExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "donation boost disable <user>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (this.args.getOrNull(0) != "donation")
			return@task false
		if (this.args.getOrNull(1) != "boost")
			return@task false
		if (this.args.getOrNull(2) != "disable")
			return@task false

		val context = this.checkType<DiscordCommandContext>(this)

		val user = context.user(3) ?: run {
			context.sendMessage("Usuário inexistente!")
			return@task true
		}
		user as JDAUser

		val member = context.discordMessage.guild.getMember(user.handle) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return@task true
		}

		NitroBoostUtils.onBoostDeactivate(member)

		context.reply(
				LorittaReply(
						"Vantagens de Booster Desativadas!"
				)
		)
		return@task true
	}
}