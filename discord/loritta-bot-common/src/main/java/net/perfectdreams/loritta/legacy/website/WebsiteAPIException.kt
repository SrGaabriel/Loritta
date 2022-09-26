package net.perfectdreams.loritta.legacy.website

import com.google.gson.JsonElement
import io.ktor.http.HttpStatusCode

class WebsiteAPIException(val status: HttpStatusCode, val payload: JsonElement) : RuntimeException()