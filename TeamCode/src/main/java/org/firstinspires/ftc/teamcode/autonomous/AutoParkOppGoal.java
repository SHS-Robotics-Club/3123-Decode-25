package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.utils.TimedDrive;

/**
 * AutoParkOppGoal
 *
 * Time-based autonomous:
 *  1. Leave the launch zone (3 pts).
 *  2. Drive downfield toward midfield.
 *  3. Strafe across the field into a lane in front of the opponent's goal.
 *  4. Optionally adjust forward/back to be ~3 ft from the goal.
 *
 * This is a defensive positioning auto and must be tuned to:
 *  - Stay out of your own alliance's goal lane and gate.
 *  - Not contact an opponent robot.
 */
@Autonomous(name = "AutoParkOppGoal", group = "Competition")
public class AutoParkOppGoal extends LinearOpMode {

    private static final double DRIVE_POWER_FACTOR      = 0.5;

    // Step 1: forward just enough to clear the launch zone
    private static final long STEP1_FORWARD_TIME_MS     = 600;
    // Step 2: continue forward downfield (toward midfield / opponent half)
    private static final long STEP2_FORWARD_TIME_MS     = 1200;
    // Step 3: strafe across field into lane in front of opponent goal
    private static final long STEP3_STRAFE_TIME_MS      = 1500;
    // Step 4: small forward/back tweak to land ~3 ft in front
    private static final long STEP4_ADJUST_TIME_MS      = 300;

    private static final long PAUSE_BETWEEN_STEPS_MS    = 150;

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

        // Step 1: leave launch zone
        timedDrive.forward(
                0.6,
                DRIVE_POWER_FACTOR,
                STEP1_FORWARD_TIME_MS,
                "Step 1: Leave launch zone"
        );

        timedDrive.pause(PAUSE_BETWEEN_STEPS_MS, "Pause after Step 1");

        // Step 2: drive further downfield
        timedDrive.forward(
                0.6,
                DRIVE_POWER_FACTOR,
                STEP2_FORWARD_TIME_MS,
                "Step 2: Drive toward midfield/opponent side"
        );

        timedDrive.pause(PAUSE_BETWEEN_STEPS_MS, "Pause after Step 2");

        // Step 3: strafe across field toward opponent goal lane
        // Choose sign of strafe based on which alliance & side you start on.
        // Start with +0.6; if it goes the wrong way, flip to -0.6.
        double strafeDirection = 0.6;

        timedDrive.strafe(
                strafeDirection,
                DRIVE_POWER_FACTOR,
                STEP3_STRAFE_TIME_MS,
                "Step 3: Strafe into opponent goal lane"
        );

        timedDrive.pause(PAUSE_BETWEEN_STEPS_MS, "Pause after Step 3");

        // Step 4: small adjust forward/back to reach ~3 ft from goal
        // Use positive power to move closer, negative to back away.
        timedDrive.forward(
                0.4,   // slightly softer
                DRIVE_POWER_FACTOR,
                STEP4_ADJUST_TIME_MS,
                "Step 4: Adjust distance from opponent goal"
        );

        telemetry.addData("Auto", "Complete – parked in opponent goal lane");
        telemetry.update();

        while (opModeIsActive()) {
            idle();
        }
    }
}
