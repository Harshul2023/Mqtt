package org.example;


import com.fazecast.jSerialComm.SerialPort;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MqttClientA {
    public static void main(String[] args) throws MqttException, InterruptedException {

        String broker = "tcp://localhost:1883";
        String homeTopic = "client_a";
        int qos = 0;
        boolean cleanSession = true;
        String sendTopic = "house/client_b/" + homeTopic;
        MqttClient mqttClient = new MqttClient(broker, "ClientA", new MemoryPersistence());
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection lost: " + throwable.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if (!(topic.split("/")[2]).equals(homeTopic)) {
//                    System.out.println("Message received: " + mqttMessage.toString() + " from " + topic.split("/")[2]);
                    System.out.println(topic.split("/")[2] + " : " + mqttMessage.toString());
                    if(mqttMessage.toString().equals("c"))
                    {
                        Thread thread = new Thread(() -> {
                            SerialPort[] AvailablePorts = SerialPort.getCommPorts();
                            SerialPort mySerialPort = AvailablePorts[0];
                            int BaudRate = 115200;
                            int DataBits = 8;
                            int StopBits = SerialPort.ONE_STOP_BIT;
                            int Parity = SerialPort.NO_PARITY;
                            mySerialPort.setComPortParameters(BaudRate,DataBits,StopBits,Parity);
                            mySerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING,1000,0);
                            mySerialPort.openPort();
                            byte[] WriteByte = "c".getBytes();
                            mySerialPort.writeBytes(WriteByte, 1);
                            byte[] readBuffer = new byte[6];
                            while (mySerialPort.readBytes(readBuffer, readBuffer.length) != 0) {
                                String s = "";
                                s = new String(readBuffer, StandardCharsets.UTF_8);
                                s=s.trim();
                                try {
                                    mqttClient.publish(sendTopic, s.getBytes(), qos, false);
                                } catch (MqttException e) {
                                    throw new RuntimeException(e);
                                }
                                mySerialPort.flushIOBuffers();
                                mySerialPort.closePort();
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
//
                            }
                        });
                        thread.start();
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//                System.out.println("Message delivered: ");
            }
        });

        mqttClient.connect(new MqttConnectOptions() {{
            setCleanSession(cleanSession);
        }});
        System.out.println("Connected to broker: " + broker);
//            logger.log(Level.INFO, "Connected to broker: " + broker)
        mqttClient.subscribe("house/#", qos);
        System.out.println("Subscribed to topic: house/#");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String message = sc.nextLine();
            mqttClient.publish(sendTopic, message.getBytes(), qos, false);
//            System.out.println("Published message " + count + " to " + sendTopic);
        }
    }
}

