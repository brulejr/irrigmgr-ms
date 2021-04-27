package io.jrb.labs.irrigmgrms.model

interface MqttDevice : Device {
    val commandTopic: String
    val onMessage: String
    val offMessage: String
}
