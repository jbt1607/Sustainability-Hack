/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package v.smartagriculture;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class SmartAgriculture {
    private static final String connString = "[IoT Hub Connection String]";
    private static DeviceClient client;

    private static final String HUMIDITY_SENSOR_ID = "28-000008c3fa0c";
    private static final String TEMPERATURE_SENSOR_ID = "28-000008c3f42b";

    private static final GpioController gpio = GpioFactory.getInstance();
    private static final GpioPinDigitalOutput fan = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Fan", PinState.LOW);
    private static final GpioPinDigitalOutput light = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Light", PinState.LOW);

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        client = new DeviceClient(connString, IotHubClientProtocol.MQTT);
        client.open();

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    W1Master w1Master = new W1Master();
                    W1Device humiditySensor = w1Master.getDevice(HUMIDITY_SENSOR_ID);
                    W1Device temperatureSensor = w1Master.getDevice(TEMPERATURE_SENSOR_ID);
                    Random random = new Random();
                    while (true) {
                        double humidity = humiditySensor.getValue();
                        double temperature = temperatureSensor.getValue();
                        Property humidityProperty = new Property("humidity", Double.toString(humidity));
                        Property temperatureProperty = new Property("temperature", Double.toString(temperature));
                        client.sendEventAsync(humidityProperty, new EventCallback(), humidityProperty);
                        client.sendEventAsync(temperatureProperty, new EventCallback(), temperatureProperty);

                        if (humidity > 70) {
                            fan.high();
                        } else {
                            fan.low();
                        }

                        if (temperature < 15) {
                            light.high();
                        } else if (temperature > 25) {
                            light.low();
                        } else {
                            light.toggle();
                        }

                        Thread.sleep(5000 + random.nextInt(5000));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }


    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }
}
