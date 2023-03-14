package org.example;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface OnDataReceiveListener {
    void onDataReceived(String data) throws MqttException;
}
