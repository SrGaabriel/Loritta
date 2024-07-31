package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.*
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunkUtils
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo

class VerifyMessageCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Verifymessage
        private val prettyPrintJson = Json {
            prettyPrint = true
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Url.Label, I18N_PREFIX.Url.Description) {
            executor = VerifyMessageURLExecutor(m, this@VerifyMessageCommand)
        }

        subcommand(I18N_PREFIX.File.Label, I18N_PREFIX.File.Description) {
            executor = VerifyMessageFileExecutor(m, this@VerifyMessageCommand)
        }
    }

    suspend fun stuff(context: UnleashedContext, imageByteArray: ByteArray) {
        val chunks = PNGChunkUtils.readChunksFromPNG(imageByteArray)

        val textChunks = chunks.filter { it.type == "tEXt" }
        val textChunksAsStrings = textChunks.map { String(it.data, Charsets.US_ASCII) }
        val loriMessageDataAsString = textChunksAsStrings.firstOrNull { it.startsWith("LORIMESSAGEDATA:") }

        if (loriMessageDataAsString == null) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.ImageDoesNotContainMessageData),
                    Emotes.Error
                )
            }
            return
        }

        val result = LoriMessageDataUtils.parseFromPNGChunk(
            m,
            chunks,
            loriMessageDataAsString
        )

        val savedMessage = when (result) {
            LoriMessageDataUtils.LoriMessageDataParseResult.InvalidInput -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ImageHasMessageDataButCouldntBeValidated),
                        Emotes.Error
                    )
                }
                return
            }
            LoriMessageDataUtils.LoriMessageDataParseResult.InvalidSignature -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ImageDataHasBeenTampared),
                        Emotes.Error
                    )
                }
                return
            }
            is LoriMessageDataUtils.LoriMessageDataParseResult.Success -> result.savedMessage
        }

        val placeContext = savedMessage.placeContext

        context.reply(false) {
            val json = savedMessage

            styled(
                context.i18nContext.get(I18N_PREFIX.ValidMessage),
                Emotes.CheckMark
            )

            styled(
                context.i18nContext.get(I18N_PREFIX.PayAttentionToTheMessage),
                Emotes.LoriLurk
            )

            embed {
                title = "Informações sobre a Mensagem"

                field("${Emotes.LoriId} ID do Discord", "`${json.id}`", true)
                field("${Emotes.LoriCalendar} Quando a Mensagem foi Enviada", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(json.timeCreated), true)
                val timeEdited = json.timeEdited
                if (timeEdited != null) {
                    field("${Emotes.LoriCalendar} Quando a Mensagem foi Editada", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(timeEdited.toJavaInstant()), true)
                }
                if (placeContext is SavedGuild) {
                    field("${Emotes.LoriId} Onde a mensagem foi enviada", "#${placeContext.channelName} (`${placeContext.channelId}`)", true)
                }
                color = LorittaColors.LorittaAqua.rgb
            }

            embed {
                title = "Informações sobre o Usuário"

                field("${Emotes.LoriId} ID do Discord", "`${json.author.id}`", true)

                val userAvatarId = json.author.avatarId
                val avatarUrl = if (userAvatarId != null) {
                    val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "a_"
                        "gif"
                    } else { "png" }

                    "https://cdn.discordapp.com/avatars/${json.author.id}/${userAvatarId}.${extension}?size=256"
                } else {
                    val avatarId = (json.author.id shr 22) % 6

                    "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
                }

                field("${Emotes.LoriLabel} Tag do Discord", "`@${json.author.name}`", true)

                field("${Emotes.LoriCalendar} Quando a Conta foi Criada", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(json.author.timeCreated), true)

                thumbnail = avatarUrl

                color = LorittaColors.LorittaAqua.rgb
            }

            when (placeContext) {
                is SavedAttachedGuild -> {
                    embed {
                        title = "Informações sobre o Servidor"

                        field("${Emotes.LoriId} ID do Discord", "`${placeContext.id}`", true)

                        field("${Emotes.LoriLabel} Nome do Servidor", placeContext.name, true)
                        field("${Emotes.LoriCalendar} Quando o Servidor foi Criado", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(placeContext.timeCreated), true)

                        thumbnail = placeContext.getIconUrl(256, ImageFormat.PNG)

                        color = LorittaColors.LorittaAqua.rgb
                    }
                }
                is SavedDetachedGuild -> {
                    embed {
                        title = "Informações sobre o Servidor"

                        field("${Emotes.LoriId} ID do Discord", "`${placeContext.id}`", true)
                        field("${Emotes.LoriCalendar} Quando o Servidor foi Criado", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(placeContext.timeCreated), true)

                        color = LorittaColors.LorittaAqua.rgb
                    }
                }
                is SavedGroupChannel -> {
                    embed {
                        title = "Informações sobre o Grupo"

                        field("${Emotes.LoriId} ID do Discord", "`${placeContext.id}`", true)
                        field("${Emotes.LoriLabel} Nome do Grupo", placeContext.name, true)
                        field("${Emotes.LoriCalendar} Quando o Grupo foi Criado", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(placeContext.timeCreated), true)

                        color = LorittaColors.LorittaAqua.rgb

                        thumbnail = placeContext.getIconUrl(256)
                    }
                }
                is SavedPrivateChannel -> {
                    embed {
                        title = "Informações sobre o Canal Privado"

                        field("${Emotes.LoriId} ID do Discord", "`${placeContext.id}`", true)
                        field("${Emotes.LoriCalendar} Data de Criação do Canal Privado", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(placeContext.timeCreated), true)

                        color = LorittaColors.LorittaAqua.rgb
                    }
                }
            }

            actionRow(
                m.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(I18N_PREFIX.SendMessageCopy)
                ) { context ->
                    context.reply(true) {
                        this.content = json.content

                        for (embed in json.embeds) {
                            embed {
                                this.title = embed.title
                                this.url = embed.url
                                this.description = embed.description
                                this.color = embed.color
                                this.image = embed.image?.url
                                this.thumbnail = embed.thumbnail?.url

                                for (field in embed.fields) {
                                    field(field.name ?: "", field.value ?: "", field.isInline)
                                }

                                val embedAuthor = embed.author
                                if (embedAuthor != null) {
                                    author(embedAuthor.name, embedAuthor.url, embedAuthor.iconUrl)
                                }

                                val embedFooter = embed.footer
                                if (embedFooter != null) {
                                    this.footer(embedFooter.text ?: "", embedFooter.iconUrl)
                                }
                            }
                        }
                    }
                },

                m.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(I18N_PREFIX.SendMessageCopyJson)
                ) { context ->
                    context.deferChannelMessage(true)

                    context.reply(true) {
                        files += FileUpload.fromData(
                            prettyPrintJson.encodeToString(json).toByteArray(Charsets.UTF_8),
                            "message.json"
                        )
                    }
                }
            )
        }
    }

    class VerifyMessageURLExecutor(val m: LorittaBot, val verifyMessageCommand: VerifyMessageCommand) : LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val url = string("url", I18N_PREFIX.Url.Options.Url.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val url = args[options.url]

            val parsedUrl = Url(url)

            val urlToBeUsedToDownloadTheImage = when (parsedUrl.host) {
                "cdn.discordapp.com" -> {
                    val expiresAt = parsedUrl.parameters["ex"]
                    val issuedAt = parsedUrl.parameters["is"]
                    val uniqueSignature = parsedUrl.parameters["hm"]

                    // If any of the attributes are not present, bail out!
                    if (expiresAt == null || issuedAt == null || uniqueSignature == null)
                        return

                    URLBuilder().apply {
                        set(scheme = "https", host = "cdn.discordapp.com") {
                            pathSegments = parsedUrl.pathSegments
                            parameters.append("ex", expiresAt)
                            parameters.append("is", issuedAt)
                            parameters.append("hm", uniqueSignature)
                        }
                    }.buildString()
                }
                "media.discordapp.net" -> {
                    // This is interesting, we can get the original file by just "converting" the URL into "cdn.discordapp.com"!
                    // We need to do that because if someone copies a image when clicking on the image -> copy from the opened image, the URL is the media.discordapp.com one, and that always copies the image in webp format
                    // So let's convert them to cdn.discordapp.com!
                    val expiresAt = parsedUrl.parameters["ex"]
                    val issuedAt = parsedUrl.parameters["is"]
                    val uniqueSignature = parsedUrl.parameters["hm"]

                    // If any of the attributes are not present, bail out!
                    if (expiresAt == null || issuedAt == null || uniqueSignature == null)
                        return

                    URLBuilder().apply {
                        set(scheme = "https", host = "cdn.discordapp.com") {
                            pathSegments = parsedUrl.pathSegments
                            parameters.append("ex", expiresAt)
                            parameters.append("is", issuedAt)
                            parameters.append("hm", uniqueSignature)
                        }
                    }.buildString()
                }
                else -> {
                    // Not a valid URL!
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Url.InvalidUrl),
                            Emotes.LoriSob
                        )
                    }
                    return
                }
            }

            // We need to use "downloadFile" because we want to download the original raw image with our custom attributes within it
            val imageInputStream = LorittaUtils.downloadFile(m, urlToBeUsedToDownloadTheImage, 5_000)
            if (imageInputStream == null) {
                context.reply(false) {
                    styled(
                        "Algo deu errado ao tentar baixar a imagem"
                    )
                }
                return
            }

            val imageByteArray = imageInputStream.readAllBytes()

            // We technically don't need to check for the image size, because we don't read the image's contents as pixels
            // If some day we add QR Codes to the image, then we will need to implement it tho!
            val simpleImageInfo = SimpleImageInfo(imageByteArray)
            if (simpleImageInfo.mimeType != "image/png") {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UploadedImageIsNotInPngFormat),
                        Emotes.Error
                    )
                }
                return
            }

            verifyMessageCommand.stuff(context, imageByteArray)
        }
    }

    class VerifyMessageFileExecutor(val m: LorittaBot, val verifyMessageCommand: VerifyMessageCommand) : LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val file = attachment("file", I18N_PREFIX.File.Options.File.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val file = args[options.file]

            if (file.contentType != "image/png") {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UploadedImageIsNotInPngFormat),
                        Emotes.Error
                    )
                }
                return
            }

            val imageByteArray = m.http.get(file.url).readBytes()

            verifyMessageCommand.stuff(context, imageByteArray)
        }
    }
}