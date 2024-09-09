package net.perfectdreams.loritta.morenitta.website

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.CurrentSong
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.SongPlaylist
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.findCurrentSong
import org.jetbrains.exposed.sql.select
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class Lorifetch(private val loritta: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Produces and emits lorifetch stats
     */
    val statsFlow = flow {
        while (true) {
            val t = measureTime {
                try {
                    // How much time it took to query and stuffz
                    val playlistInfo = Yaml.default.decodeFromStream<SongPlaylist>(
                        File(
                            loritta.config.loritta.folders.content,
                            "playlist.yml"
                        ).inputStream()
                    )
                    val shuffledPlaylistSongs = playlistInfo.songs.shuffled(Random(0))

                    val guildCount = loritta.lorittaShards.queryGuildCount()
                    val since = Instant.now()
                        .minusSeconds(86400)
                        .toKotlinInstant()

                    val (executedCommands, uniqueUsersExecutedCommands) = loritta.transaction {
                        val appCommands = ExecutedApplicationCommandsLog.select {
                            ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                        }.count()
                        val legacyCommands = ExecutedCommandsLog.select {
                            ExecutedCommandsLog.sentAt greaterEq since.toEpochMilliseconds()
                        }.count()

                        val uniqueAppCommands = ExecutedApplicationCommandsLog.slice(ExecutedApplicationCommandsLog.userId).select {
                            ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                        }.groupBy(ExecutedApplicationCommandsLog.userId).toList()
                            .map { it[ExecutedApplicationCommandsLog.userId] }
                        val uniqueLegacyCommands = ExecutedCommandsLog.slice(ExecutedCommandsLog.userId).select {
                            ExecutedCommandsLog.sentAt greaterEq since.toEpochMilliseconds()
                        }.groupBy(ExecutedCommandsLog.userId).toList()
                            .map { it[ExecutedCommandsLog.userId] }

                        return@transaction Pair(appCommands + legacyCommands, (uniqueAppCommands + uniqueLegacyCommands).distinct().size)
                    }

                    val nowAsZST = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)

                    val startTime = playlistInfo.startedPlayingAt.epochSeconds // When the playlist started playing
                    val timestamp = nowAsZST.toEpochSecond() // The timestamp we want to check

                    val currentSong = findCurrentSong(shuffledPlaylistSongs, startTime, timestamp)

                    emit(
                        LorifetchStats(
                            guildCount,
                            executedCommands.toInt(),
                            uniqueUsersExecutedCommands,
                            currentSong
                        )
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while processing lorifetch SSE flow!" }
                }
            }

            val timeToWait = 1.seconds - t
            logger.info { "Took $t to query stats for Lorifetch, waiting ${timeToWait}..." }
            delay(timeToWait)
        }
    }

    data class LorifetchStats(
        val guildCount: Int,
        val executedCommands: Int,
        val uniqueUsersExecutedCommands: Int,
        val currentSong: CurrentSong
    )
}