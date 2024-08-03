package net.perfectdreams.loritta.discordchatmessagerenderer

import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitUntilState
import mu.KotlinLogging
import java.io.Closeable
import java.util.*
import kotlin.time.measureTimedValue

class DiscordMessageRendererManager : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val playwright = Playwright.create()
    // Firefox has an issue in headless more where there is a white space at the bottom of the screenshot...
    // Chromium has an issue where screenshots >16384 are "corrupted"
    private val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
    private val deviceScale = 2.0
    private val maxDimensionsOfImages = (16_384 / deviceScale).toInt()
    private val browserContext = browser.newContext(Browser.NewContextOptions().setDeviceScaleFactor(deviceScale))
    private var page = createPage()

    private fun createPage(): Page {
        val newPage = browserContext.newPage()
        newPage.onCrash {
            // The reason we don't attempt to withPage lock it, is because this seems to create a deadlock because the onCrash handler is triggered within the rendering call
            // Failsafe if a page crashes
            logger.error { "Page $it crashed! - Closing page..." }
            page.close()
            page = createPage()
        }
        return newPage
    }

    // The caller must make sure that this is called on only a single thread!
    fun renderMessage(
        messageId: Long,
        savedMessageUniqueId: UUID,
        qrCodeImageAsByteArray: ByteArray?
    ): ByteArray {
        logger.info { "Starting to render message $messageId! - Open pages: ${browserContext.pages().size} (render request: $savedMessageUniqueId)" }

        val timedValueLoadPage = measureTimedValue {
            logger.info { "Loading message preview page for $messageId! (render request: $savedMessageUniqueId)" }

            page.navigate(
                // We navigate to an URL to avoid any memory leaks that may cause by reusing the same page
                "http://127.0.0.1:8080/internal/message-preview?message=$savedMessageUniqueId",
                // By using LOAD, we force the browser to actually wait the entire page to be loaded
                Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD),
            )

            // We don't need to wait anything to load, because Playwright makes sure that everything is loaded when using "WaitUntilState.LOAD" :3
        }

        logger.info { "Took ${timedValueLoadPage.duration} to load the message preview page for $messageId! (render request: $savedMessageUniqueId)" }

        val timedValueTakingScreenshot = measureTimedValue {
            logger.info { "Taking screenshot of message $messageId! (render request: $savedMessageUniqueId)" }

            page.querySelector("#wrapper").screenshot(ElementHandle.ScreenshotOptions())
        }

        logger.info { "Took ${timedValueTakingScreenshot.duration} to generate a message screenshot for $messageId! (render request: $savedMessageUniqueId)" }

        return timedValueTakingScreenshot.value
    }

    override fun close() {
        playwright.close()
    }
}