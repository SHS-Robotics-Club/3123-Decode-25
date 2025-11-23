package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Simple wrapper for a REV Digital LED Indicator (or basic LED)
 * connected to a digital port on the Control Hub/Expansion Hub.
 */
public class LedIndicator {

    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    private final String ledName;

    private DigitalChannel readyLed;

    /**
     * Constructor: only store references.
     */
    public LedIndicator(HardwareMap hardwareMap, Telemetry telemetry, String ledName) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
        this.ledName = ledName;
    }

    /**
     * Initialize the LED hardware.
     * Should be called from Robot.init() with the other subsystems.
     */
    public void init() {
        try {
            readyLed = hardwareMap.get(DigitalChannel.class, ledName);
            readyLed.setMode(DigitalChannel.Mode.OUTPUT);
            readyLed.setState(false);   // LED off at init
            telemetry.addData("LED", "Initialized: %s", ledName);
        } catch (Exception e) {
            telemetry.addData("Error", "LED init failed for '%s': %s", ledName, e.getMessage());
        }
    }

    /**
     * Turn LED on when shooter is in ready-to-fire RPM tolerance.
     */
    public void setReady(boolean on) {
        if (readyLed != null) {
            readyLed.setState(on);
            telemetry.addData("LED-State", on ? "ON (ready)" : "OFF");
        }
    }

    /**
     * Optional: turn LED off on stop.
     */
    public void off() {
        setReady(false);
    }
}
