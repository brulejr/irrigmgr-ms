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
package io.jrb.labs.irrigmgrms.mqtt

import io.jrb.labs.irrigmgrms.datafill.MqttClientDatafill
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.context.SmartLifecycle
import java.lang.String.format
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class MqttClient(private val datafill: MqttClientDatafill) : SmartLifecycle {

    private val urlPattern = "tcp://%s:%d"

    private val log = KotlinLogging.logger {}
    private val running: AtomicBoolean = AtomicBoolean(false)
    private val persistence: MemoryPersistence = MemoryPersistence()

    private var mqttClient: IMqttClient? = null;

    override fun getPhase(): Int {
        return 20;
    }

    override fun start() {
        log.info("Starting MqttConsumer on {}", datafill)

        val url = format(urlPattern, datafill.host, datafill.port)
        val publisherId = UUID.randomUUID().toString()
        mqttClient = MqttClient(url, publisherId, persistence)

        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = true
        options.connectionTimeout = datafill.connectionTimeout
        mqttClient!!.connect(options)

        log.info("MqttConsumer STARTED")

        running.set(true)
    }

    override fun stop() {
        log.info("Stopping MqttConsumer")

        mqttClient!!.disconnect()
        mqttClient = null

        log.info("MqttConsumer STOPPED")

        running.set(false)
    }

    override fun isRunning(): Boolean {
        return running.get()
    }

    fun publish(topic: String, message: MqttMessage) {
        mqttClient!!.publish(topic, message)
    }

    fun subscribe(topic: String, listener: IMqttMessageListener) {
        mqttClient!!.subscribe(topic, listener)
    }

}
