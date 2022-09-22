package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Warmup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RedisConnectionWarmup(
   private val jedis: Jedis,
   override val iterations: Int = 1000,
   override val interval: Duration = 10.milliseconds,
   private val command: (Jedis) -> Unit = { it.get(Random.nextInt().toString()) }
) : Warmup() {

   override val name: String = "redis_warmup"

   override suspend fun warmup() {
      withContext(Dispatchers.IO) { command(jedis) }
   }
}
