package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Renders a Navbar view with [html] as its' contents
 *
 * This is used for legacy Pebble views!
 */
class LegacyPebbleRawHtmlView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    private val _title: String,
    private val html: String
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    override val hasFooter = false
    override val useOldStyleCss = true

    override fun getTitle() = _title

    override fun DIV.generateContent() {
        unsafe {
            raw(html)
        }
    }
}