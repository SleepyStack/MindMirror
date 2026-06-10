package com.thedebugnaths.ai_mindmirror.service;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Slf4j
@Service
public class HardwareIntegrationService {

    private SerialPort arduinoPort;
    private OutputStream outputStream;
    private boolean isHardwareConnected = false;

    // Use application.properties to set this, e.g., arduino.port=COM3
    @Value("${arduino.port:COM3}")
    private String portName;

    @PostConstruct
    public void init() {
        log.info("Attempting to connect to MindMirror Hardware on port: {}", portName);
        try {
            arduinoPort = SerialPort.getCommPort(portName);
            arduinoPort.setComPortParameters(115200, 8, 1, 0);
            arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

            if (arduinoPort.openPort()) {
                outputStream = arduinoPort.getOutputStream();
                isHardwareConnected = true;
                log.info("Hardware successfully connected!");
                Thread.sleep(2000); // Allow Arduino to reset upon connection
            } else {
                log.warn("Hardware not found on {}. Running in Mock Mode.", portName);
            }
        } catch (Exception e) {
            log.warn("Serial port error. Running in Phantom/Mock Mode. Reason: {}", e.getMessage());
        }
    }

    public void triggerHardwareCommand(String command) {
        if (isHardwareConnected && arduinoPort.isOpen()) {
            try {
                // Ensure the exact newline format your C++ script expects
                outputStream.write((command.toLowerCase() + "\n").getBytes());
                outputStream.flush();
                log.info("⚡ Sent to hardware: {}", command);
            } catch (Exception e) {
                log.error("Hardware send failed: {}", e.getMessage());
            }
        } else {
            // MOCK MODE: Logs the tool execution so you know your webhooks are working
            log.info(" MOCK : Hardware command executed: {}", command);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (isHardwareConnected && arduinoPort != null && arduinoPort.isOpen()) {
            arduinoPort.closePort();
            log.info("Hardware connection closed safely.");
        }
    }
}