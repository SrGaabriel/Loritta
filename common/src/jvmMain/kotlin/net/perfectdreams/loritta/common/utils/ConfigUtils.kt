package net.perfectdreams.loritta.common.utils

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

@OptIn(ExperimentalSerializationApi::class)
object ConfigUtils {
    fun parseConfig(reference: KClass<*>): LorittaConfig {
        val file = File("config.conf")

        if (file.exists().not()) {
            file.createNewFile()
            file.writeBytes(reference.java.getResourceAsStream("/config.conf").readAllBytes())
            println("I just created a config file, since you don't have one, customize your preferences there and then try restarting me!")
            exitProcess(-1)
        }
        return Hocon {}.decodeFromConfig(ConfigFactory.parseFile(file))
    }
}