package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.utils.TimedDrive;

/**
 * AutoLeaveOnly
 *
 * Scores the 3-point LEAVE bonus by:
 *  1. Driving forward off the launch tile.
 *  2. Stopping and parking for the rest of autonomous.
 */
@Autonomous(name = "AutoLeaveOnly", group = "Competition")
public class AutoLeaveOnly extends LinearOpMode {

    private static final double DRIVE_POWER_FACTOR   = 0.5;
    private static final long   FORWARD_TIME_MS      = 600;  // tune on field
    private static final long   PAUSE_AFTER_MOVE_MS  = 100;

    @Override
    public void runOpMode() {

        telemetry.addData("Status", "Initializing robot...");
        telemetry.update();

        Robot robot = new Robot(hardwareMap, telemetry);
        Drivetrain drivetrain = robot.getDrivetrain();

        if (drivetrain == null) {
            telemetry.addData("Error", "Drivetrain is null. Check Robot initialization.");
            telemetry.update();
            return;
        }

        drivetrain.init();

        TimedDrive timedDrive = new TimedDrive(this, drivetrain, telemetry);

        telemetry.addData("Status", "Initialized - waiting for START");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // Step 1: drive forward off the launch tile
        timedDrive.forward(
                0.6,                  // forward powerY
                DRIVE_POWER_FACTOR,
                FORWARD_TIME_MS,
                "Leave launch zone"
        );

        timedDrive.pause(PAUSE_AFTER_MOVE_MS, "Pause after leave");

        telemetry.addData("Auto", "Complete – parked after leaving");
        telemetry.update();

        // Stay put until autonomous ends
        while (opModeIsActive()) {
            idle();
        }
    }
}
