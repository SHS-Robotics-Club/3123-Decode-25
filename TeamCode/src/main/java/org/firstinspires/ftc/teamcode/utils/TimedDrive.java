package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * TimedDrive
 *
 * Utility for simple time-based drivetrain moves in autonomous.
 * Wraps the common pattern:
 *   1) Set drivetrain power
 *   2) Wait for a duration
 *   3) Stop
 *
 * This keeps autonomous OpModes readable and reduces copy/paste.
 */
public class TimedDrive {

    private final LinearOpMode opMode;
    private final Drivetrain drivetrain;
    private final Telemetry telemetry;

    public TimedDrive(LinearOpMode opMode, Drivetrain drivetrain, Telemetry telemetry) {
        this.opMode = opMode;
        this.drivetrain = drivetrain;
        this.telemetry = telemetry;
    }

    /**
     * Generic move helper: set power, wait, stop.
     */
    public void move(double powerX,
                     double powerY,
                     double powerRotate,
                     double powerFactor,
                     long durationMs,
                     String label) {

        if (!opMode.opModeIsActive()) return;

        if (label != null && !label.isEmpty()) {
            telemetry.addData("Auto", label);
            telemetry.update();
        }

        drivetrain.operate(powerX, powerY, powerRotate, powerFactor);

        long start = System.currentTimeMillis();
        while (opMode.opModeIsActive()
                && (System.currentTimeMillis() - start) < durationMs) {
            // Optionally, you could add live telemetry here
            opMode.idle();
        }

        drivetrain.operate(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Convenience: drive straight forward/backward for a time.
     */
    public void forward(double powerY, double powerFactor, long durationMs, String label) {
        move(0.0, powerY, 0.0, powerFactor, durationMs, label);
    }

    /**
     * Convenience: strafe left/right for a time.
     * Positive powerX = right, negative = left (matches your drivetrain).
     */
    public void strafe(double powerX, double powerFactor, long durationMs, String label) {
        move(powerX, 0.0, 0.0, powerFactor, durationMs, label);
    }

    /**
     * Convenience: rotate in place for a time.
     */
    public void rotate(double powerRotate, double powerFactor, long durationMs, String label) {
        move(0.0, 0.0, powerRotate, powerFactor, durationMs, label);
    }

    /**
     * Simple pause that respects opModeIsActive.
     */
    public void pause(long durationMs, String label) {
        if (!opMode.opModeIsActive()) return;

        if (label != null && !label.isEmpty()) {
            telemetry.addData("Auto", label);
            telemetry.update();
        }

        long start = System.currentTimeMillis();
        while (opMode.opModeIsActive()
                && (System.currentTimeMillis() - start) < durationMs) {
            opMode.idle();
        }
    }
}
