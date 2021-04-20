package net.perfectdreams.loritta.platform.interaktions

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.discord.AvatarExecutor
import net.perfectdreams.loritta.commands.discord.declarations.AvatarCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.commands.images.declarations.*
import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.declarations.MinecraftPlayerCommand
import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.KkEaeMenCommand
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.commands.utils.ECBManager
import net.perfectdreams.loritta.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.platform.interaktions.commands.CommandManager
import net.perfectdreams.loritta.platform.interaktions.emotes.DiscordEmoteManager
import net.perfectdreams.loritta.platform.interaktions.utils.metrics.Prometheus
import net.perfectdreams.loritta.platform.interaktions.webserver.InteractionsServer

class LorittaInteraKTions(config: LorittaConfig, discordConfig: LorittaDiscordConfig): LorittaDiscord(config, discordConfig) {
    val interactions = InteractionsServer(
        applicationId = discordConfig.applicationId,
        publicKey = discordConfig.publicKey,
        token = discordConfig.token
    )

    val commandManager = CommandManager(this, interactions.commandManager)

    override val emotes = Emotes(
        DiscordEmoteManager(
            mapOf("chino_ayaya" to "discord:a:chino_AYAYA:696984642594537503")
        )
    )

    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    val mojangApi = MinecraftMojangAPI()

    val http = HttpClient {
        expectSuccess = false
    }

    fun start() {
        // Register Prometheus Metrics
        Prometheus.register()

        localeManager.loadLocales()

        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor(emotes)
        )

        // ===[ DISCORD ]===
        commandManager.register(
            AvatarCommand,
            AvatarExecutor()
        )

        // ===[ FUN ]===
        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(emotes, random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor(emotes)
        )

        commandManager.register(
            CancelledCommand,
            CancelledExecutor(emotes)
        )

        commandManager.register(
            CalculatorCommand,
            CalculatorExecutor(emotes)
        )

        commandManager.register(
            AnagramCommand,
            AnagramExecutor(emotes)
        )

        commandManager.register(
            MoneyCommand,
            MoneyExecutor(emotes, ECBManager())
        )

        commandManager.register(
            ChooseCommand,
            ChooseExecutor(emotes)
        )

        commandManager.register(
            MinecraftPlayerCommand,
            McSkinExecutor(emotes, mojangApi),
            McAvatarExecutor(emotes, mojangApi),
            McHeadExecutor(emotes, mojangApi),
            McBodyExecutor(emotes, mojangApi)
        )

        commandManager.register(
            TextTransformDeclaration,
            TextVaporwaveExecutor(emotes),
            TextQualityExecutor(emotes),
            TextVaporQualityExecutor(emotes),
            TextVemDeZapExecutor(emotes, random)
        )

        commandManager.register(
            KkEaeMenCommand,
            KkEaeMenExecutor(emotes)
        )

        commandManager.register(
            AvatarTestCommand,
            AvatarTestExecutor(http)
        )

        commandManager.register(
            JankenponCommand, JankenponExecutor(random, emotes)
        )

        // ===[ IMAGES ]===
        commandManager.register(AtaCommand, MonicaAtaExecutor(http), ChicoAtaExecutor(http), LoriAtaExecutor(http), GessyAtaExecutor(http))
        commandManager.register(DrakeCommand, DrakeExecutor(http), BolsoDrakeExecutor(http), LoriDrakeExecutor(http))
        commandManager.register(ManiaTitleCardCommand, ManiaTitleCardExecutor(http))

        commandManager.register(ArtCommand, ArtExecutor(http))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(http))
        commandManager.register(BolsonaroCommand, BolsonaroExecutor(http), Bolsonaro2Executor(http), BolsoFrameExecutor(http))
        commandManager.register(BriggsCoverCommand, BriggsCoverExecutor(http))
        commandManager.register(BuckShirtCommand, BuckShirtExecutor(http))
        commandManager.register(CanellaDvdCommand, CanellaDvdExecutor(http))
        commandManager.register(EdnaldoCommand, EdnaldoBandeiraExecutor(http), EdnaldoTvExecutor(http))
        commandManager.register(LoriSignCommand, LoriSignExecutor(http))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(http))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(http))
        commandManager.register(PetPetCommand, PetPetExecutor(http))
        commandManager.register(QuadroCommand, QuadroExecutor(http))
        commandManager.register(RipTvCommand, RipTvExecutor(http))
        commandManager.register(RomeroBrittoCommand, RomeroBrittoExecutor(http))
        commandManager.register(StudiopolisTvCommand, StudiopolisTvExecutor(http))
        commandManager.register(SustoCommand, SustoExecutor(http))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(http))
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(http))
        commandManager.register(CortesFlowCommand, CortesFlowExecutor(http))
        commandManager.register(KnuxThrowCommand, KnuxThrowExecutor(http))
        commandManager.register(CepoDeMadeiraCommand, CepoDeMadeiraExecutor(http))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(http))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(http))
        commandManager.register(TrumpCommand, TrumpExecutor(http))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(http))
        commandManager.register(SAMCommand, SAMExecutor(http))
        commandManager.register(ToBeContinuedCommand, ToBeContinuedExecutor(http))
        commandManager.register(InvertColorsCommand, InvertColorsExecutor(http))

        // ===[ UTILS ]===
        commandManager.register(MorseCommand, MorseFromExecutor(emotes), MorseToExecutor(emotes))

        runBlocking {
            commandManager.convertToInteraKTions(
                localeManager.getLocaleById("default")
            )
        }

        interactions.start()
    }
}