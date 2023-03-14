package org.example;


import com.fazecast.jSerialComm.SerialPort;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MqttClientB {

    public static void main(String[] args) throws MqttException {
//        String broker = "tcp://localhost:1883";
        String broker = "ssl://5ff889680d284fb6adf092c802700e85.s2.eu.hivemq.cloud:8883";
        String homeTopic = "client_b";
        int qos = 0;
        String clientId = "ClientB";
        String sendTopic = "topic/client_a/" + homeTopic;
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName("harshul2023");
        connOpts.setPassword("7983145689aA".toCharArray());
        connOpts.setSocketFactory(SSLSocketFactory.getDefault()); // using the default socket factory
        client.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
        client.subscribe("topic/#", qos);
        System.out.println("Subscribed to topic: topic/#");
        client.publish(sendTopic, "Connected".getBytes(), qos, false);
        Scanner sc = new Scanner(System.in);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                if (!(topic.split("/")[2]).equals(homeTopic)) {
//                    System.out.println("Message received: " + mqttMessage.toString() + " from " + topic.split("/")[2]);
                    System.out.println(topic.split("/")[2] + " : " + mqttMessage.toString());
                    if (mqttMessage.toString().equals("c") || mqttMessage.toString().equals("1")) {
                        OnDataReceiveListener onDataReceiveListener = new OnDataReceiveListener() {
                            @Override
                            public void onDataReceived(String data) {
//                                    System.out.println(data);
                                Thread thread = new Thread(() -> {
                                    try {
                                        client.publish(sendTopic, data.getBytes(), qos, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                thread.start();
                            }
                        };
                        SerialCommunicationThread serialCommunicationThread = new SerialCommunicationThread(mqttMessage, onDataReceiveListener);
                        serialCommunicationThread.run();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
//                    System.out.println("Message delivered: ");
            }
        });

        while (true) { // runs forever, break with CTRL+C
            String msg = sc.nextLine();
            client.publish(sendTopic, msg.getBytes(), qos, false);
        }
    }
}
