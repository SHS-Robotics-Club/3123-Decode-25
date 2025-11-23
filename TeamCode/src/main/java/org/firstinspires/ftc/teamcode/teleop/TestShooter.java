package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.utils.TelemetryManager;

//@Disabled
// Possible Groups: Competition, Development, Test, Training
@TeleOp(name = "MecSh-Test", group = "Test")


/*
 * MecSh-Test
 * Test program for 2-wheel shooter subsystem.
 * Uses gamepad buttons B and Y to select between two RPM sets (low and high).
 * Uses dpad directions to select between four preset RPMs within the selected set.
 * Uses left bumper to set shooter to IDLE RPM, right bumper to turn OFF shooter.
 *
 * VERSION   DATE     WHO  DETAIL
 * 00.01.00  25Jun24  SEB  Initial release
 *
 */
public class TestShooter extends OpMode {

    //  Drivetrain parameters
    public static final double DRIVETRAIN_X_POWER_CORRECTION = 1.1;
    public static final double DRIVETRAIN_LOW_POWER_FACTOR = 0.6;
    public static final double DRIVETRAIN_HIGH_POWER_FACTOR = 0.8;
    public static final double DRIVETRAIN_TURBO_POWER_FACTOR = 1.0;
    public static final long LONG_PRESS_THRESHOLD_MS = 500L;

    // Shooter parameters
    private enum RpmSet {
        LOW_SET,
        HIGH_SET
    }
    // Preset RPM values for each set
    private static final double[] LOW_SET_RPMS  = {1200, 1300, 1400, 1500};
    private static final double[] HIGH_SET_RPMS = {1600, 1700, 1800, 1900};
    // Idle RPM value
    private static final double IDLE_RPM = 400.0;

    // Define as instance of a Robot class as null
    private Robot robot;
    // Define TelemetryManager
    private TelemetryManager telemetryManager;

    // Define Drivetrain local parameters
    private double drivetrainPowerDirX;
    private double drivetrainPowerDirY;
    private double drivetrainPowerRotate;
    private double drivetrainPowerFactor = DRIVETRAIN_LOW_POWER_FACTOR;
    private long buttonAPressStartTime = 0;
    private boolean buttonAPreviouslyPressed = false;

    // Shooter Shooter2Wheel local parameters
    // Current selected RPM set
    private RpmSet currentSet = RpmSet.LOW_SET;
    // Current selected preset index (0-3)
    private int presetIndex = 0;
    // Previous button states for edge detection
    private boolean prevB, prevY;
    private boolean prevDown, prevLeft, prevUp, prevRight;

    /**
     * Instantiates and initializes the subsystems and utilities
     * Runs once
     */
    @Override
    public void init() {
        // Instantiate a robot using the hardwareMap constructor
        robot = new Robot(hardwareMap, telemetry);
        // Make sure all subsystems are properly instantiated
        if (robot.getDrivetrain() == null || robot.getShooter() == null) {
            telemetry.addData("Error", "Subsystem initialization failed. Check hardware configuration.");
            telemetry.update();
            requestOpModeStop();
            return;
        }
        // Initialize robot subsystems
        robot.getDrivetrain().init();
        robot.getShooter().init();
        robot.getLedIndicator().init();  // Not critical if LED fails
        telemetryManager = robot.getTelemetryManager();

        // Set default telemetry
        telemetry.addData("Status", "Initialized");
        robot.getDrivetrain().reportTelemetry();
        telemetry.addData("Power Factor", drivetrainPowerFactor);
        telemetry.update(); // Send "Initialized" and powerFactor to the Driver Station
    }


    /**
     * Initialization complete. Wait here for PLAY.
     */
    @Override
    public void init_loop() {
        // Report drivetrain telemetry
        telemetry.addData("Status", "Initialized");
        robot.getDrivetrain().reportTelemetry();

        // Report current power factor
        telemetry.addData("Power Factor", drivetrainPowerFactor);
        // Conditionally report LED status if present
        if (robot.getLedIndicator() != null) {
            telemetry.addData("LED", "Ready indicator available");
        } else {
            telemetry.addData("LED", "Ready indicator not present");
        }
        // Update telemetry once after all data is added
        telemetry.update();
    }


    /**
     * Initialization complete. Wait here for PLAY.
     */
    @Override
    public void loop() {

        long currentTime = System.currentTimeMillis();

        //********** DRIVETRAIN **********

        // Get drivetrain power inputs from gamepad1 joysticks
        boolean buttonAPressed = gamepad1.a;

        if (buttonAPressed && !buttonAPreviouslyPressed) {  // Respond to drivetrain power factor change request
            // Button just pressed, record the time
            buttonAPressStartTime = currentTime;
        } else if (!buttonAPressed && buttonAPreviouslyPressed) {
            // Button just released, calculate the press duration
            double pressDuration = currentTime - buttonAPressStartTime;

            if (pressDuration >= LONG_PRESS_THRESHOLD_MS) {
                // Long press: set powerFactor to TURBO_POWER_FACTOR
                drivetrainPowerFactor = DRIVETRAIN_TURBO_POWER_FACTOR;
            } else {
                // Brief press: toggle between LOW_POWER_FACTOR and HIGH_POWER_FACTOR
                if (drivetrainPowerFactor == DRIVETRAIN_LOW_POWER_FACTOR) {
                    drivetrainPowerFactor = DRIVETRAIN_HIGH_POWER_FACTOR;
                } else if (drivetrainPowerFactor == DRIVETRAIN_HIGH_POWER_FACTOR ||
                        drivetrainPowerFactor == DRIVETRAIN_TURBO_POWER_FACTOR) {
                    drivetrainPowerFactor = DRIVETRAIN_LOW_POWER_FACTOR;
                }
            }
        }

        // Update button state
        buttonAPreviouslyPressed = buttonAPressed;

        // Calculate drivetrain power components
        drivetrainPowerDirX = gamepad1.left_stick_x * DRIVETRAIN_X_POWER_CORRECTION;
        drivetrainPowerDirY = -gamepad1.left_stick_y;
        drivetrainPowerRotate = gamepad1.right_stick_x;
        // Request power application to drivetrain
        robot.getDrivetrain().operate(drivetrainPowerDirX, drivetrainPowerDirY, drivetrainPowerRotate, drivetrainPowerFactor);

        //********** SHOOTER **********
        // Update shooter set and preset selections based on gamepad input
        updateSetSelection();
        updatePresetSelection();
        // Get the selected preset RPM based on current set and preset index
        double presetRPM = getSelectedPresetRPM();
        double commandRPM = applyBumperOverrides(presetRPM);
        // Command the shooter to the determined RPM
        robot.getShooter().setRPM(commandRPM, commandRPM);

        // Read ready flag from shooter
        boolean ready = robot.getShooter().isReadyToFire();
        // Drive the REV Digital LED Indicator
        if (robot.getLedIndicator() != null) {
            robot.getLedIndicator().setReady(ready);
        }

        // Drivetrain telemetry
        telemetry.addLine("=== Drivetrain ===");
        telemetry.addData("Power Factor", drivetrainPowerFactor);
        telemetry.addData("LX/LY/RX", "%.2f / %.2f / %.2f",
                drivetrainPowerDirX, drivetrainPowerDirY, drivetrainPowerRotate);
        // Shooter telemetry
        telemetry.addLine("=== Shooter ===");
        telemetry.addData("Set", currentSet == RpmSet.LOW_SET ? "LOW SET (B)" : "HIGH SET (Y)");
        telemetry.addData("Preset", getPresetLabel());
        telemetry.addData("Target RPM", commandRPM);
        telemetry.addData("READY", robot.getShooter().isReadyToFire() ? ">>> READY TO FIRE <<<" : "spinning up...");

        // Update telemetry
        telemetry.update();

    }

    /**
     * Optional: turn off subsystems on stop.
     */
    @Override
    public void stop() {
        if (robot != null && robot.getLedIndicator() != null) {
            robot.getLedIndicator().off();
        }
        if (robot != null && robot.getShooter() != null) {
            robot.getShooter().stop();
        }
    }

    private void updateSetSelection() {
        boolean b = gamepad1.b;
        boolean y = gamepad1.y;

        if (b && !prevB) currentSet = RpmSet.LOW_SET;
        if (y && !prevY) currentSet = RpmSet.HIGH_SET;

        prevB = b;
        prevY = y;
    }

    private void updatePresetSelection() {
        boolean d = gamepad1.dpad_down;
        boolean l = gamepad1.dpad_left;
        boolean u = gamepad1.dpad_up;
        boolean r = gamepad1.dpad_right;

        if (d && !prevDown) presetIndex = 0;
        if (l && !prevLeft) presetIndex = 1;
        if (u && !prevUp)   presetIndex = 2;
        if (r && !prevRight)presetIndex = 3;

        prevDown = d;
        prevLeft = l;
        prevUp   = u;
        prevRight= r;
    }

    private double getSelectedPresetRPM() {
        return (currentSet == RpmSet.LOW_SET)
                ? LOW_SET_RPMS[presetIndex]
                : HIGH_SET_RPMS[presetIndex];
    }

    private double applyBumperOverrides(double baseRpm) {
        if (gamepad1.right_bumper) return 0.0;       // OFF
        if (gamepad1.left_bumper)  return IDLE_RPM;  // IDLE
        return baseRpm;                              // preset RPM
    }

    private String getPresetLabel() {
        String d;
        switch (presetIndex) {
            case 0: d = "DOWN";  break;
            case 1: d = "LEFT";  break;
            case 2: d = "UP";    break;
            case 3: d = "RIGHT"; break;
            default: d = "?";    break;
        }
        return d + " (" + getSelectedPresetRPM() + " RPM)";
    }
}
