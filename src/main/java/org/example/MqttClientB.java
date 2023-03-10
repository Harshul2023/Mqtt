package org.example;


import com.fazecast.jSerialComm.SerialPort;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MqttClientB {


    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String homeTopic = "client_b";
        int qos = 0;
        String clientId = "ClientB";
        String sendTopic = "house/client_a/" + homeTopic;
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (!(topic.split("/")[2]).equals(homeTopic)) {
                        System.out.println(topic.split("/")[2] + " : " + message.toString());
                        if(message.toString().equals("c"))
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
                                        client.publish(sendTopic, s.getBytes(), qos, false);
                                    } catch (MqttException e) {
                                        throw new RuntimeException(e);
                                    }
//                                    mySerialPort.flushIOBuffers();
//                                    mySerialPort.closePort();
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                    break;
                                }
                            });
                            thread.start();
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
//                    System.out.println("Message delivered: ");
                }
            });

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            System.out.println("Connected to broker: " + broker);
//            logger.log(Level.INFO, "Connected to broker: " + broker)
            client.subscribe("house/#", qos);
            System.out.println("Subscribed to topic: house/#");
            Scanner sc = new Scanner(System.in);
            while (true) { // runs forever, break with CTRL+C
                String msg = sc.nextLine();
                client.publish(sendTopic, msg.getBytes(), qos, false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
