package net.perfectdreams.loritta.commands.minecraft

import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI

class McAvatarExecutor(emotes: Emotes, mojang: MinecraftMojangAPI) : CrafatarExecutorBase(
    "avatars",
    emotes,
    mojang
) {
    companion object : CommandExecutorDeclaration(McAvatarExecutor::class) {
        override val options = CrafatarExecutorBase.options
    }
}