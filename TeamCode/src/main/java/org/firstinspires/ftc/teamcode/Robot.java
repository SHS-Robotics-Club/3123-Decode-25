package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * This class consists of all the subsystems and utilities used to form a robot.
 *
 * PUBLIC METHODS:
 *     Robot(hardwareMap) - constructor for instantiating a robot

 *
 * VERSION   DATE     WHO  DETAIL
 * 00.01.00  30Oct25  SEB  Initial release
 *
 */
public class Robot {

    // Declare robot subsystems as null instance
    private Drivetrain drivetrain;

    /**
     * - Robot Constructor -
     * Uses HardwareMap to import the robot subsystems
     */
    public Robot(HardwareMap hardwareMap, Telemetry telemetry) {

        try {
            // Instantiate robot subsystems
            drivetrain = new Drivetrain(hardwareMap, telemetry);  // Drivetrain is four motors

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

}