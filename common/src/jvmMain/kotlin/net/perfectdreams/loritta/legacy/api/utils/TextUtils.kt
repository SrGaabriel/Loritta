package net.perfectdreams.loritta.legacy.api.utils

import java.text.MessageFormat

actual fun String.format(vararg arguments: Any?): String {
	return MessageFormat.format(this, *arguments)
}