package net.perfectdreams.loritta.discord.objects

import kotlinx.datetime.Instant

abstract class LorittaGuild(
    val id: Long,
    val name: String,
    val ownerId: Long,
    val region: String,
    val creation: Instant
) {
    abstract suspend fun retrieveMember(id: Long): LorittaMember?
    abstract suspend fun retrieveChannel(id: Long): LorittaDiscordChannel?

    abstract suspend fun retrieveChannels(): Collection<LorittaDiscordChannel>
}

interface LorittaMember {
    val roles: List<Long>
}