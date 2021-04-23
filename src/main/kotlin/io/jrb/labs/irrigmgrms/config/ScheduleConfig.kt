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
package io.jrb.labs.irrigmgrms.config

import io.jrb.labs.irrigmgrms.command.MeasureCommand
import io.jrb.labs.irrigmgrms.command.TurnOffCommand
import io.jrb.labs.irrigmgrms.command.TurnOnCommand
import io.jrb.labs.irrigmgrms.device.Relay
import io.jrb.labs.irrigmgrms.device.Sensor
import io.jrb.labs.irrigmgrms.model.Schedule
import io.jrb.labs.irrigmgrms.model.ScheduleEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.LocalTime
import java.util.EnumSet

import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.WEDNESDAY

@Configuration
@EnableScheduling
class ScheduleConfig {

    @Bean
    fun relayA() = Relay("RelayA")

    @Bean
    fun sensor() = Sensor( "Sensor1")

    @Bean
    fun scheduleA(): Schedule = Schedule(
        name = "ScheduleA",
        events = listOf(
            ScheduleEvent(name = "ON", timestamp = timestamp(5), command = TurnOnCommand(relayA())),
            ScheduleEvent(name = "OFF", timestamp = timestamp(10), command = TurnOffCommand(relayA()))
        )
    )

    @Bean
    fun scheduleB(): Schedule = Schedule(
        name = "ScheduleB",
        events = listOf(
            ScheduleEvent(
                name = "MEASURE",
                timestamp = timestamp(5),
                command = MeasureCommand(sensor()),
                scheduledDays = EnumSet.of(MONDAY, WEDNESDAY, FRIDAY)
            )
        )
    )

    private fun timestamp(seconds: Long): LocalTime {
        return LocalTime.now().plusSeconds(seconds)
    }

}
