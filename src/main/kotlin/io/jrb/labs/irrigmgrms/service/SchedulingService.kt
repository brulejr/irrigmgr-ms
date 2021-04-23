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

import io.jrb.labs.irrigmgrms.model.Schedule
import io.jrb.labs.irrigmgrms.model.ScheduleEvent
import mu.KotlinLogging
import org.springframework.context.SmartLifecycle
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SchedulingService(
    private val schedules: List<Schedule>,
    private val scheduler: TaskScheduler
) : SmartLifecycle {

    private val log = KotlinLogging.logger {}

    private val running: AtomicBoolean = AtomicBoolean(false)
    private var jobsMap = mutableMapOf<String, ScheduledFuture<*>>()

    override fun start() {
        log.info("Starting SchedulingService")
        jobsMap = mutableMapOf()
        schedules.forEach { schedule ->
            schedule.events.forEach { event ->
                val eventName = "${schedule.name}::${event.name}"
                val eventTime = calculateEventTime(event)
                if (eventEnabled(schedule, event, eventTime)) {
                    log.info("Scheduling Event - $eventName for $eventTime")
                    val scheduledTask = scheduler.schedule(event.command::run, eventTime.toInstant())
                    jobsMap[eventName] = scheduledTask
                } else {
                    log.info("Not Scheduling Event - $eventName")
                }
            }
        }
        running.set(true)
    }

    override fun stop() {
        log.info("Stopping SchedulingService")
        jobsMap.forEach { (eventName, scheduledTask) ->
            log.info("Cancelling - $eventName")
            scheduledTask.cancel(true)
        }
        jobsMap.clear()
        running.set(false)
    }

    override fun isRunning(): Boolean {
        return running.get()
    }

    fun restart() {
        stop()
        start()
    }

    private fun calculateEventTime(event: ScheduleEvent): ZonedDateTime {
        return event.timestamp.atDate(LocalDate.now()).atZone(ZoneId.systemDefault())
    }

    private fun eventEnabled(schedule: Schedule, scheduleEvent: ScheduleEvent, eventTime: ZonedDateTime): Boolean {
        return if (schedule.enabled) {
            val scheduledDays = scheduleEvent.scheduledDays
            scheduledDays.isEmpty() || scheduledDays.contains(eventTime.dayOfWeek)
        } else {
            true
        }
    }

}
