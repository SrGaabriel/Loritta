package net.perfectdreams.loritta.morenitta.website.routes.sponsors

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.SponsorRedirectView

class SponsorsRedirectRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/sponsor/{sponsorSlug}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		val sponsorSlug = call.parameters["sponsorSlug"]

		val sponsor = loritta.sponsors.firstOrNull { it.slug == sponsorSlug } ?: return

		call.respondHtml(
			SponsorRedirectView(
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				sponsor
			).generateHtml()
		)
	}
}