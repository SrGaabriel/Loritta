package net.perfectdreams.loritta.legacy.api.commands

import net.perfectdreams.loritta.legacy.api.LorittaBot
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory

abstract class LorittaAbstractCommandBase(
        val loritta: LorittaBot,
        labels: List<String>,
        category: CommandCategory
) : AbstractCommandBase<CommandContext, CommandBuilder<CommandContext>>(labels, category) {
    final override fun create(builder: CommandBuilder<CommandContext>.() -> Unit): Command<CommandContext> {
        val cmd = command(
                loritta,
                labels,
                category
        ) {
            builder.invoke(this)
        }

        return cmd
    }
}