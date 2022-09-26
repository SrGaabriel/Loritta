package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordRegexes
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.image.BufferedImage
import kotlin.streams.toList

abstract class ProfileCreator(val loritta: LorittaCinnamon, val internalName: String) {
    /**
     * Gets the user's global position in the economy ranking
     *
     * @param  userProfile the user's profile
     * @return the user's current global position in the economy ranking
     */
    suspend fun getGlobalEconomyPosition(userProfile: PuddingUserProfile) =
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any sonhos, we just return null! :3
        if (userProfile.money >= 100_000L)
            loritta.pudding.sonhos.getSonhosRankPositionBySonhos(userProfile.money)
        else null

    /**
     * Gets the user's local position in the experience ranking
     *
     * @param  localProfile the user's local profile
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalExperiencePosition(localProfile: ResultRow?) = if (localProfile != null && localProfile[GuildProfiles.xp] != 0L) {
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any local XP, we just return null! :3
        loritta.pudding.transaction {
            GuildProfiles.select { (GuildProfiles.guildId eq localProfile[GuildProfiles.guildId]) and (GuildProfiles.xp greaterEq localProfile[GuildProfiles.xp]) }.count()
        }
    } else {
        null
    }

    /**
     * Draws the user's about me
     *
     * @param graphics      the image graphics' instance
     * @param text          the user's about me text
     * @param startX        the X position of where the text should be drawn
     * @param startY        the Y position of where the text should be drawn
     * @param endX          where the X will overflow
     * @param endY          unused
     * @param allowedDiscordEmojis the allowed emojis in the about me, if null, then all emojis are allowed
     */
    suspend fun drawAboutMeWrapSpaces(
        graphics: Graphics,
        fontMetrics: FontMetrics,
        text: String,
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        allowedDiscordEmojis: List<Snowflake>?
    ): Int {
        val sections = ImageUtils.parseStringToDrawableSections(loritta.unicodeEmojiManager, text)

        val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte

        var currentX = startX // X atual
        var currentY = startY // Y atual

        val emojiWidth = fontMetrics.ascent
        val emojiYOffset = (fontMetrics.descent / 2)

        for (section in sections) {
            when (section) {
                is ImageUtils.DrawableText -> {
                    val split = section.text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Nós precisamos deixar os espaços entre os splits!
                    for (str in split) {
                        var width = fontMetrics.stringWidth(str) // Width do texto que nós queremos colocar
                        if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
                            currentX = startX // Nós iremos fazer wrapping do texto
                            currentY += lineHeight
                        }
                        for (c in str.toCharArray()) { // E agora nós iremos printar todos os chars
                            if (c == '\n') {
                                currentX = startX // Nós iremos fazer wrapping do texto
                                currentY += lineHeight
                                continue
                            }
                            width = fontMetrics.charWidth(c)
                            if (!graphics.font.canDisplay(c))
                                continue
                            graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
                            currentX += width // E adicione o width no nosso currentX
                        }
                    }
                }
                is ImageUtils.DrawableDiscordEmote -> {
                    if (allowedDiscordEmojis == null || section.emoteId in allowedDiscordEmojis) {
                        val emoteImage = loritta.emojiImageCache.getDiscordEmoji(section.emoteId, Image.Size.Size64)

                        if (emoteImage != null) {
                            if (currentX + emojiWidth > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
                                currentX = startX // Nós iremos fazer wrapping do texto
                                currentY += lineHeight
                            }

                            graphics.drawImage(
                                emoteImage.getResizedInstance(emojiWidth, emojiWidth, InterpolationType.BILINEAR),
                                currentX,
                                currentY - emojiWidth + emojiYOffset,
                                null
                            )

                            currentX += emojiWidth
                        }
                    }
                }
                is ImageUtils.DrawableUnicodeEmote -> {
                    val emoteImage = loritta.emojiImageCache.getTwitterEmoji(section.emoji.codePoints().toList())

                    if (emoteImage != null) {
                        if (currentX + emojiWidth > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
                            currentX = startX // Nós iremos fazer wrapping do texto
                            currentY += lineHeight
                        }

                        graphics.drawImage(
                            emoteImage.getResizedInstance(emojiWidth, emojiWidth, InterpolationType.BILINEAR),
                            currentX,
                            currentY - emojiWidth + emojiYOffset,
                            null
                        )

                        currentX += emojiWidth
                    }
                }
            }
        }

        return currentY
    }
}