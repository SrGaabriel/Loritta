package net.perfectdreams.loritta.morenitta.utils

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class LorittaDailyShopUpdateTask(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		// How many new items should be shown in the shop on every shop rotation?
		// This exists to avoid Lori always selecting previously sold items instead of selecting never seen before items
		private const val NEW_ITEMS_TARGET = 2
		private const val DAILY_PROFILE_DESIGNS_TARGET = 4
		private const val DAILY_BACKGROUNDS_TARGET = 10

		fun generate(loritta: LorittaBot) {
			logger.info { "Generating a new daily shop..." }

			runBlocking {
				val resultId = loritta.pudding.transaction {
					val newShop = DailyShops.insertAndGetId {
						it[generatedAt] = System.currentTimeMillis()
					}

					getAndAddRandomBackgroundsToShop(newShop)
					getAndAddRandomProfileDesignsToShop(newShop)

					newShop
				}

				// Notify that it was refreshed
				val shards = loritta.config.loritta.clusters.instances

				shards.map {
					GlobalScope.launch {
						try {
							logger.info { "Sending daily shop refresh request to other clusters..." }
							loritta.httpWithoutTimeout.post("${it.getUrl(loritta)}/daily-shop-refreshed?dailyShopId=${resultId}") {
								userAgent(loritta.lorittaCluster.getUserAgent(loritta))
							}
						} catch (e: Exception) {
							logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
						}
					}
				}
			}
		}

		private fun getAndAddRandomBackgroundsToShop(shopId: EntityID<Long>) {
			val allBackgrounds = Backgrounds.select {
				Backgrounds.enabled eq true and (Backgrounds.availableToBuyViaDreams eq true)
			}.toMutableList()

			val selectedBackgrounds = mutableListOf<ResultRow>()

			// We will try to at least have two new items every single day, to avoid showing already sold backgrounds every day
			val neverSoldBeforeBackgrounds = allBackgrounds.filter {
				DailyShopItems.select {
					DailyShopItems.item eq it[Backgrounds.id]
				}.count() == 0L
			}.toMutableList()

			repeat(Math.min(NEW_ITEMS_TARGET, neverSoldBeforeBackgrounds.size)) {
				if (neverSoldBeforeBackgrounds.isNotEmpty()) { // Because we repeat multiple times and remove the background from the list, we need to check if the list is empty inside the repeat
					val randomBackground = neverSoldBeforeBackgrounds.random()

					allBackgrounds.remove(randomBackground)
					neverSoldBeforeBackgrounds.remove(randomBackground)
					selectedBackgrounds.add(randomBackground)
				}
			}

			repeat(DAILY_BACKGROUNDS_TARGET - selectedBackgrounds.size) {
				val randomBackground = allBackgrounds.random()
				allBackgrounds.remove(randomBackground)
				selectedBackgrounds.add(randomBackground)
			}

			for (background in selectedBackgrounds) {
				DailyShopItems.insert {
					it[shop] = shopId
					it[item] = background[Backgrounds.id]
					it[tag] = if (DailyShopItems.select { item eq background[Backgrounds.id] }.count() == 0L) { "website.dailyShop.new" } else null
				}
			}
		}

		private fun getAndAddRandomProfileDesignsToShop(shopId: EntityID<Long>) {
			val allProfileDesigns = ProfileDesigns.select {
				ProfileDesigns.enabled eq true and (ProfileDesigns.availableToBuyViaDreams eq true)
			}.toMutableList()

			val selectedProfileDesigns = mutableListOf<ResultRow>()

			// We will try to at least have two new items every single day, to avoid showing already sold backgrounds every day
			val neverSoldBeforeProfileDesigns = allProfileDesigns.filter {
				DailyProfileShopItems.select {
					DailyProfileShopItems.item eq it[ProfileDesigns.id]
				}.count() == 0L
			}.toMutableList()

			repeat(Math.min(NEW_ITEMS_TARGET, neverSoldBeforeProfileDesigns.size)) {
				if (neverSoldBeforeProfileDesigns.isNotEmpty()) { // Because we repeat multiple times and remove the background from the list, we need to check if the list is empty inside the repeat
					val randomBackground = neverSoldBeforeProfileDesigns.random()

					allProfileDesigns.remove(randomBackground)
					neverSoldBeforeProfileDesigns.remove(randomBackground)
					selectedProfileDesigns.add(randomBackground)
				}
			}

			repeat(DAILY_PROFILE_DESIGNS_TARGET - selectedProfileDesigns.size) {
				val randomBackground = allProfileDesigns.random()
				allProfileDesigns.remove(randomBackground)
				selectedProfileDesigns.add(randomBackground)
			}

			for (background in selectedProfileDesigns) {
				DailyProfileShopItems.insert {
					it[shop] = shopId
					it[item] = background[ProfileDesigns.id]
					it[tag] = if (DailyProfileShopItems.select { item eq background[ProfileDesigns.id] }.count() == 0L) { "website.dailyShop.new" } else null
				}
			}
		}
	}

	override fun run() {
		logger.info { "Automatically updating the daily shop!" }
		try {
			generate(loritta)
			logger.info { "Successfully updated the daily shop!" }
		} catch (e: Exception) {
			logger.warn(e) { "Something went wrong while updating the daily shop..." }
		}
	}
}