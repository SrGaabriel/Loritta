package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File

class LavaCommand(loritta: LorittaBot) : AbstractCommand(loritta, "lava", category = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.lava.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.lava.examples")
	override fun getUsage() = arguments {
		argument(ArgumentType.IMAGE) {}
		argument(ArgumentType.TEXT) {}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
			val template = readImage(File(LorittaBot.ASSETS + "lava.png")) // Template

			context.rawArgs = context.rawArgs.sliceArray(1..context.rawArgs.size - 1)

			if (context.rawArgs.isEmpty()) {
				this.explain(context)
				return
			}

			var joined = context.rawArgs.joinToString(separator = " ") // Vamos juntar tudo em uma string
			var singular = true // E verificar se é singular ou não
			if (context.rawArgs[0].endsWith("s", true)) { // Se termina com s...
				singular = false // Então é plural!
			}
			var resized = contextImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)
			var small = contextImage.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH)
			var templateGraphics = template.graphics
			templateGraphics.drawImage(resized, 120, 0, null)
			templateGraphics.drawImage(small, 487, 0, null)
			var image = BufferedImage(700, 443, BufferedImage.TYPE_INT_ARGB)
			var graphics = image.graphics.enableFontAntiAliasing()
			graphics.color = Color.WHITE
			graphics.fillRect(0, 0, 700, 443)
			graphics.color = Color.BLACK
			graphics.drawImage(template, 0, 100, null)

			var font = Font.createFont(0, File(LorittaBot.ASSETS + "mavenpro-bold.ttf")).deriveFont(24F)
			graphics.font = font
			ImageUtils.drawCenteredString(graphics, "O chão " + (if (singular) "é" else "são") + " $joined", Rectangle(2, 2, 700, 100), font)

			context.sendFile(image, "lava.png", context.getAsMention(true))
		} else {
			this.explain(context)
		}
	}
}