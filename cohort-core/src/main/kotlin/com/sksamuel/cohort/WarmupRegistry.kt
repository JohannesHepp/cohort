package com.sksamuel.cohort

import io.github.oshai.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class WarmupRegistry(dispatcher: CoroutineDispatcher = Dispatchers.Default) {

   private val logger = KotlinLogging.logger { }
   private val scope = CoroutineScope(dispatcher)
   private val warmups = ConcurrentHashMap<String, WarmupState>()

   /**
    * Adds a [Warmup] to this registry using the default name.
    * This warmup is invoked until [duration] has expired, at which point the check is completed.
    *
    * Warmups are intended to be used to warm up the JVM for better performance once the system is ready
    * to start accepting requests.
    */
   fun register(warmup: Warmup, duration: Duration) {

      val existing = warmups.put(warmup.name, WarmupState.Running)
      if (existing != null)
         error("Warmup registry already contains a warmup with the name ${warmup.name}")

      println("Warmups=${warmups.keys}")
      println("Warmups=${System.identityHashCode(warmups)}")

      scope.launch {
         println("Starting warmup ${warmup.name} for $duration")
         var iterations = 0
         val end = System.currentTimeMillis() + duration.inWholeMilliseconds
         while (System.currentTimeMillis() < end) {
            warmup.warm(iterations++)
         }
         warmup.close()
         println("Warmup ${warmup.name} has completed")
         warmups[warmup.name] = WarmupState.Completed
      }
   }

   fun state(): WarmupState {
      println("Warmups=${warmups.keys}")
      println("Warmups=${System.identityHashCode(warmups)}")
      return if (warmups.all { it.value == WarmupState.Completed }) WarmupState.Completed else WarmupState.Running
   }
}

enum class WarmupState {
   Running, Completed
}