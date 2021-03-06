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
package io.jrb.labs.irrigmgrms.command

import io.jrb.labs.irrigmgrms.model.Command
import io.jrb.labs.irrigmgrms.model.CommandResponse
import io.jrb.labs.irrigmgrms.model.MqttDevice
import io.jrb.labs.irrigmgrms.mqtt.MqttClient
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Component

@Component
class TurnOnCommand(private val mqttClient: MqttClient) : Command<MqttDevice> {

    private val log = KotlinLogging.logger {}

    override fun run(device: MqttDevice): CommandResponse {
        log.info("Turning device on - device=${device.name}, commandTopic=${device.commandTopic}")
        val message = MqttMessage(device.onMessage.toByteArray())
        mqttClient.publish(device.commandTopic, message)
        return CommandResponse()
    }

}
