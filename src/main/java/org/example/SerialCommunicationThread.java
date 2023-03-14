package org.example;

import com.fazecast.jSerialComm.SerialPort;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class SerialCommunicationThread {
    SerialPort[] AvailablePorts = SerialPort.getCommPorts();
    SerialPort mySerialPort = AvailablePorts[0];
    private final OnDataReceiveListener onDataReceiveListener;

    MqttMessage mqttMessage ;
    public SerialCommunicationThread(MqttMessage mqttMessage, OnDataReceiveListener onDataReceiveListener) {
        this.mqttMessage = mqttMessage;
        this.onDataReceiveListener = onDataReceiveListener;
    }

    public void run()  {

        int BaudRate = 115200;
        int DataBits = 8;
        int StopBits = SerialPort.ONE_STOP_BIT;
        int Parity = SerialPort.NO_PARITY;
        mySerialPort.setComPortParameters(BaudRate,DataBits,StopBits,Parity);
        mySerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING,1000,0);
        mySerialPort.openPort();
        try{
            Thread.sleep(1500);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        byte[] writeByte = mqttMessage.toString().getBytes();
        mySerialPort.writeBytes(writeByte, 1);
        byte[] readBuffer = new byte[6];
        int i = 0;
        while (mySerialPort.readBytes(readBuffer, readBuffer.length) != 0) {
            String s = "";
            s = new String(readBuffer, StandardCharsets.UTF_8);
            s=s.trim();
            if (mySerialPort.getLastErrorLocation() == 1024 || mySerialPort.getLastErrorLocation() == 1029) {
                mySerialPort.closePort();
                System.out.println("Error occurred");
                return;
            }
            try {
                onDataReceiveListener.onDataReceived(s);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
                if(mqttMessage.toString().equals("c"))
                    break;
                i++;
                if(i==4999)
                    break;

        }
        mySerialPort.flushIOBuffers();
        mySerialPort.closePort();
        try {
            writeByte = mqttMessage.toString().getBytes();
            mySerialPort.writeBytes(writeByte,1);
            mySerialPort.closePort();
            Thread.sleep(1000);
            if(!mySerialPort.isOpen())
                System.out.println("Port Closed");
            else mySerialPort.closePort();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
