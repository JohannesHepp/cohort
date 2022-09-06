package com.sksamuel.cohort.pulsar

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.pulsar.client.admin.PulsarAdmin

class PulsarHealthCheck(private val client: PulsarAdmin) : HealthCheck {

   companion object {
      operator fun invoke(serviceHttpUrl: String): PulsarHealthCheck {
         return PulsarHealthCheck(
            PulsarAdmin.builder().serviceHttpUrl(serviceHttpUrl).build()
         )
      }
   }

   override val name: String = "pulsar_cluster"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         client.bookies()
         HealthCheckResult.Healthy("Connected to Pulsar")
      }.getOrElse { HealthCheckResult.Unhealthy("Could not connect to Pulsar", it) }
   }
}
