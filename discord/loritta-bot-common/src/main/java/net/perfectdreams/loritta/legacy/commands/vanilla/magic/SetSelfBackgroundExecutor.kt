package net.perfectdreams.loritta.legacy.commands.vanilla.magic

import net.perfectdreams.loritta.legacy.network.Databases
import net.perfectdreams.loritta.legacy.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordCommandContext
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object SetSelfBackgroundExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "set self_background <internalType>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "set")
			return@task false
		if (args.getOrNull(1) != "self_background")
			return@task false

		val context = checkType<DiscordCommandContext>(this)
		transaction(Databases.loritta) {
			context.lorittaUser.profile.settings.activeBackgroundInternalName = EntityID(args[2], Backgrounds)
		}

		context.reply(
				LorittaReply(
						"Background alterado!"
				)
		)
		return@task true
	}
}