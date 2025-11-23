package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.LedIndicator;
import org.firstinspires.ftc.teamcode.subsystems.Shooter2Wheel;
import org.firstinspires.ftc.teamcode.utils.TelemetryManager;

/**
 * This class consists of all the subsystems and utilities used to form a robot.
 * It serves as a single point of reference for all robot capabilities.
 *
 * PUBLIC METHODS:
 *     Robot(hardwareMap) - constructor for instantiating a robot
 *     getDrivetrain() - returns the drivetrain subsystem
 *     getShooter() - returns the shooter subsystem
 *     getLedIndicator() - returns the LED indicator subsystem
 *
 * VERSION   DATE     WHO  DETAIL
 * 00.01.00  22Nov25  SEB  Initial release
 *
 */
public class Robot {

    // Declare robot subsystems as null instance
    private final Drivetrain drivetrain;
    private final Shooter2Wheel shooter;
    private final LedIndicator ledIndicator;
    private final TelemetryManager telemetryManager;


    /**
     * - Robot Constructor -
     * Uses HardwareMap to import the robot subsystems
     */
    public Robot(HardwareMap hardwareMap, Telemetry telemetry) {

        try {
            // Instantiate robot subsystems
            drivetrain = new Drivetrain(hardwareMap, telemetry);  // Drivetrain is four motors
            shooter = new Shooter2Wheel(hardwareMap, telemetry);        // Shooter is two motors
            ledIndicator = new LedIndicator(hardwareMap, telemetry, "shooterReadyLed");
            telemetryManager = new TelemetryManager(telemetry);

        } catch (Exception e) {
            telemetry.addData("Error", "Robot initialization failed: " + e.getMessage());
            telemetry.update();
            throw new RuntimeException("Robot initialization failed", e);
        }
    }

    /**
     * Allows public access to the drivetrain subsystem
     */
    public Drivetrain getDrivetrain() {
        return drivetrain;
    }

    /**
     * Allows public access to the shooter subsystem
     */
    public Shooter2Wheel getShooter() {
        return shooter;
    }

    /**
     * Allows public access to the LED indicator subsystem
     */
    public LedIndicator getLedIndicator() { return ledIndicator; }

    /**
     * Allows public access to the telemetry manager utility
     */
    public TelemetryManager getTelemetryManager() {
        return telemetryManager;
    }

}