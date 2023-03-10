package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLSocketFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Example {
    public static void main(String[] args) throws MqttException {
        MqttClient client = new MqttClient(
                "ssl://5ff889680d284fb6adf092c802700e85.s2.eu.hivemq.cloud:8883", // serverURI in format: "protocol://name:port"
                MqttClient.generateClientId(), // ClientId
                new MemoryPersistence()); // Persistence
        System.out.println(MqttClient.generateClientId());
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("harshul2023");
        mqttConnectOptions.setPassword("7983145689aA".toCharArray());
        mqttConnectOptions.setSocketFactory(SSLSocketFactory.getDefault()); // using the default socket factory
        client.connect(mqttConnectOptions);

        client.setCallback(new MqttCallback() {
            @Override
            // Called when the client lost the connection to the broker
            public void connectionLost(Throwable cause) {
                System.out.println("client lost connection " + cause);
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) {
//                System.out.println(topic + ": " + Arrays.toString(message.getPayload()));
                System.out.println(message.toString());
            }
            @Override
            // Called when an outgoing publish is complete
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("delivery complete " + token);
            }
        });
        client.subscribe("#", 1); // subscribe to everything with QoS =
        client.publish(
                "topic","Hey Harshul".getBytes(UTF_8),2,false);

//        client.disconnect();
    }
}