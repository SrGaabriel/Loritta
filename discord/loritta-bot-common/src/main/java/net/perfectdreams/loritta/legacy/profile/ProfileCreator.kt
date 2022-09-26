package net.perfectdreams.loritta.legacy.profile

import net.perfectdreams.loritta.legacy.dao.Profile
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.Guild
import java.awt.image.BufferedImage

abstract class ProfileCreator(
		val internalName: String
) {
	abstract suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage

	open suspend fun createGif(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): List<BufferedImage> {
		return listOf(create(sender, user, userProfile, guild, badges, locale, background, aboutMe))
	}
}