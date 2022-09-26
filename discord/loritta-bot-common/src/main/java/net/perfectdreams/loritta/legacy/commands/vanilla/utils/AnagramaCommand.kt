package net.perfectdreams.loritta.legacy.commands.vanilla.utils

import com.google.common.math.BigIntegerMath
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils


class AnagramaCommand : AbstractCommand("anagram", listOf("anagrama"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.anagram"
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "anagram")

		if (context.args.isNotEmpty()) {
			val currentWord = context.args.joinToString(separator = " ")

			var shuffledChars = currentWord.toCharArray().toList()

			while (shuffledChars.size != 1 && shuffledChars.joinToString("") == currentWord && currentWord.groupBy { it }.size >= 2)
				shuffledChars = shuffledChars.shuffled()

			val shuffledWord = shuffledChars.joinToString(separator = "")

			var exp = 1.toBigInteger()
			currentWord.groupingBy { it }.eachCount().forEach { (_, value) ->
				exp = exp.multiply(BigIntegerMath.factorial(value))
			}

			val max = BigIntegerMath.factorial(currentWord.length).divide(exp)

			context.reply(
                    LorittaReply(
                            message = context.locale["$LOCALE_PREFIX.result", shuffledWord] + " ${Emotes.LORI_WOW}",
                            prefix = "✍"
                    ),
                    LorittaReply(
                            message = context.locale["$LOCALE_PREFIX.stats", currentWord, max],
                            prefix = "\uD83E\uDD13"
                    )
			)
		} else {
			this.explain(context)
		}
	}
}