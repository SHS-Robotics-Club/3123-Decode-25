package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.utils.TimedDrive;

/**
 * AutoWallLane
 *
 * Time-based autonomous:
 *  1. Drive forward off the starting tile (3 pts).
 *  2. Strafe sideways into the wall lane.
 *  3. Drive forward along the wall lane into the PARKING ZONE (10 pts).
 */
@Autonomous(name = "AutoWallLane", group = "Competition")
public class AutoWallLane extends LinearOpMode {

    private static final double DRIVE_POWER_FACTOR = 0.5;

    private static final long STEP1_FORWARD_TIME_MS = 600;
    private static final long STEP2_STRAFE_TIME_MS  = 700;
    private static final long STEP3_FORWARD_TIME_MS = 1500;
    private static final long PAUSE_BETWEEN_STEPS_MS = 150;

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

        // Step 1: forward to cross AUTO line
        timedDrive.forward(
                0.6,
                DRIVE_POWER_FACTOR,
                STEP1_FORWARD_TIME_MS,
                "Step 1: Forward to cross AUTO line"
        );

        timedDrive.pause(PAUSE_BETWEEN_STEPS_MS, "Pause after Step 1");

        // Step 2: strafe into wall lane
        double strafeDirection = 0.6;  // flip sign if needed for your side
        timedDrive.strafe(
                strafeDirection,
                DRIVE_POWER_FACTOR,
                STEP2_STRAFE_TIME_MS,
                "Step 2: Strafe into wall lane"
        );

        timedDrive.pause(PAUSE_BETWEEN_STEPS_MS, "Pause after Step 2");

        // Step 3: forward along wall to PARKING ZONE
        timedDrive.forward(
                0.6,
                DRIVE_POWER_FACTOR,
                STEP3_FORWARD_TIME_MS,
                "Step 3: Forward along wall into PARK"
        );

        telemetry.addData("Auto", "Complete – parked in wall lane");
        telemetry.update();

        while (opModeIsActive()) {
            idle();
        }
    }
}
