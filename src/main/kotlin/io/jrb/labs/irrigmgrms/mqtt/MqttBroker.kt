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

import io.jrb.labs.irrigmgrms.datafill.MqttBrokerDatafill
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import mu.KotlinLogging
import org.springframework.context.SmartLifecycle
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

class MqttBroker(private val datafill: MqttBrokerDatafill) : SmartLifecycle {

    private val log = KotlinLogging.logger {}
    private val running: AtomicBoolean = AtomicBoolean(false)

    private var brokerService: Server? = null

    override fun getPhase(): Int {
        return 10;
    }

    override fun start() {
        log.info("Starting MqttBroker on {}", datafill)

        val config = mqttConfig()
        brokerService = Server()
        brokerService!!.startServer(config)

        log.info("MqttBroker STARTED")
        running.set(true)
    }

    override fun stop() {
        log.info("Stopping MqttBroker")

        brokerService!!.stopServer();

        log.info("MqttBroker STOPPED")
        running.set(false)
    }

    override fun isRunning(): Boolean {
        return running.get()
    }

    private fun mqttConfig(): MemoryConfig {
        val config = MemoryConfig(Properties())
        config.setProperty("port", datafill.port.toString())
        config.setProperty("websocket_port", datafill.websocketPort.toString())
        config.setProperty("host", datafill.host)
        return config
    }

}
