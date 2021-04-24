/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.irrigmgrms.service

import io.jrb.labs.irrigmgrms.datafill.IrrigationDatafill
import io.jrb.labs.irrigmgrms.model.Command
import io.jrb.labs.irrigmgrms.model.Device
import io.jrb.labs.irrigmgrms.model.Schedule
import io.jrb.labs.irrigmgrms.model.ScheduleEvent
import mu.KotlinLogging
import org.springframework.context.SmartLifecycle
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SchedulingService(
    private val datafill: IrrigationDatafill,
    private val scheduler: TaskScheduler
) : SmartLifecycle {

    private val log = KotlinLogging.logger {}

    private val running: AtomicBoolean = AtomicBoolean(false)
    private var jobsMap = mutableMapOf<String, ScheduledFuture<*>>()

    override fun start() {
        log.info("Starting SchedulingService")
        jobsMap = mutableMapOf()
        datafill.schedules.forEach { schedule ->
            schedule.events.forEach { event ->
                val taskTime = calculateTaskTime(event)
                event.devices.forEach { device ->
                    val taskName = calculateTaskName(schedule, event, device)
                    if (isEventEnabled(schedule, event, taskTime)) {
                        log.info("Scheduling Task - $taskName for $taskTime")
                        val scheduledTask = scheduler.schedule(
                            runCommand(event.command, device),
                            taskTime.toInstant()
                        )
                        jobsMap[taskName] = scheduledTask
                    } else {
                        log.info("Not Scheduling Task - $taskName")
                    }
                }
            }
        }
        running.set(true)
    }

    override fun stop() {
        log.info("Stopping SchedulingService")
        jobsMap.forEach { (taskName, scheduledTask) ->
            log.info("Cancelling Task - $taskName")
            scheduledTask.cancel(true)
        }
        jobsMap.clear()
        running.set(false)
    }

    override fun isRunning(): Boolean {
        return running.get()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun restart() {
        log.info("Restarting SchedulingService")
        stop()
        start()
    }

    private fun calculateTaskName(schedule: Schedule, event: ScheduleEvent, device: Device): String {
        return "${schedule.name}::${event.name}::${device.name}"
    }

    private fun calculateTaskTime(event: ScheduleEvent): ZonedDateTime {
        val timestamp: LocalTime = event.timestamp
        val now: LocalTime = LocalTime.now()
        val date: LocalDate = if (now.isBefore(timestamp)) LocalDate.now() else LocalDate.now().plusDays(1)
        return timestamp.atDate(date).atZone(ZoneId.systemDefault())
    }

    private fun isEventEnabled(schedule: Schedule, scheduleEvent: ScheduleEvent, eventTime: ZonedDateTime): Boolean {
        return if (schedule.enabled) {
            val scheduledDays = scheduleEvent.scheduledDays
            scheduledDays.isEmpty() || scheduledDays.contains(eventTime.dayOfWeek)
        } else {
            true
        }
    }

    private fun runCommand(command: Command, device: Device): Runnable {
        return Runnable {
            command.run(device)
        }
    }

}
