package com.sksamuel.cohort.system

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [Check] for the maximum system cpu between 0 and 1.0
 *
 * The check is considered healthy if the system cpu load is < [maxLoad].
 */
class SystemCpuCheck(private val maxLoad: Double) : Check {

  private val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

  override suspend fun check(): CheckResult {
    val load = bean.systemCpuLoad
    return if (load < maxLoad) {
      CheckResult.Healthy("System CPU is below threshold [$load < $maxLoad]")
    } else {
      CheckResult.Unhealthy("System CPU is above threshold [$load >= $maxLoad]", null)
    }
  }

}