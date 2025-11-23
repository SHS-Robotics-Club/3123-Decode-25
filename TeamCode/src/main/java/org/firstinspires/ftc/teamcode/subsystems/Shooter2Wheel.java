package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * The Shooter class defines all the shooter components for a robot.
 *
 * PUBLIC METHODS:
 *     Shooter(hardwareMap) - constructor for instantiating a shooter
 *     void init() - initializes the components of the shooter
 *     void setPower(power) - sets power to the shooter motors
 *     void reportTelemetry() - reports shooter telemetry information
 *
 * VERSION   DATE     WHO  DETAIL
 * 00.01.00  22nOV25  SEB  Initial release
 *
 */
public class Shooter2Wheel {

    //    Constants    //
    public static final double MOTOR_POWER_ZERO = 0.0;  // Power to motors before START
    /** Maximum usable shooter RPM at the flywheel after gearing. */
    private static final double MAX_RPM = 2500.0;
    /** Minimum usable RPM (for sanity clamp). */
    private static final double MIN_RPM = 0.0;
    /** Encoder ticks per mechanical revolution at the motor output (28 PPR * 4x quadrature). */
    private static final int TICKS_PER_REV = 28 * 4;    // 112
    /**
     * RPM per second ramp rate. A value of 1200 means it takes
     * a bit over 2 seconds to go from 0 to 2500 RPM.
     */
    private static final double RAMP_RATE_RPM_PER_SEC = 1200.0;
    // How close (in RPM) we want to be to target to consider the wheel "ready"
    private static final double READY_TOLERANCE_RPM = 50.0;
    // Minimum RPM that counts as "spun up" (avoid READY at low speeds)
    private static final double READY_MIN_RPM = 500.0;

    //    Properties    //
    // Declare shooter components (null) - do not instantiate here
    private DcMotorEx leftShooter;
    private DcMotorEx rightShooter;
    private Telemetry telemetry;
    // Internal state: what we are currently commanding (after ramping).
    private double currentLeftRPM = 0.0;
    private double currentRightRPM = 0.0;
    // Cached readiness flag
    private boolean readyToFire = false;
    // Time tracking for ramp computation.
    private final ElapsedTime rampTimer = new ElapsedTime();


    /**
     * - Shooter Constructor -
     * Instantiates all the shooter components
     * @param hardwareMap the central store for hardware configuration
     * @param telemetry the telemetry object for reporting data
     */
    public Shooter2Wheel(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        try {
            // Instantiate both motors as DcMotor class and use the configuration
            leftShooter = hardwareMap.get(DcMotorEx.class, "leftShooter");
            rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");
            // Define motor directions
            leftShooter.setDirection(DcMotor.Direction.FORWARD);
            rightShooter.setDirection(DcMotor.Direction.REVERSE);
        } catch (Exception e) {
            telemetry.addData("Error", "Shooter initialization failed: " + e.getMessage());
            telemetry.update();
        }
    }

    /**
     * Initializes all shooter subsystems.
     * Motors are set to zero power and power is based directly from setPower (no PID controller).
     */
    public void init() {
        if (leftShooter != null && rightShooter != null) {
            leftShooter.setPower(MOTOR_POWER_ZERO);
            leftShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            leftShooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            rightShooter.setPower(MOTOR_POWER_ZERO);
            rightShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            rightShooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        } else {
            telemetry.addData("Error", "Shooter motors are not initialized.");
        }
    }

    /**
     * Command the shooter to reach the requested RPMs with a controlled ramp.
     * TeleOp should call this once per loop.
     *
     * @param leftTargetRPM  Desired left shooter RPM [0, 2500].
     * @param rightTargetRPM Desired right shooter RPM [0, 2500].
     */
    public void setRPM(double leftTargetRPM,
                       double rightTargetRPM) {

        // Compute elapsed time since last call for ramping.
        double dt = rampTimer.seconds();
        rampTimer.reset();

        // Guard against extreme dt (for example if OpMode was paused).
        if (dt <= 0.0 || dt > 0.5) {
            // If dt is weird, just assume a nominal loop time (e.g., 20 ms) to avoid overshooting the ramp.
            dt = 0.02;
        }

        // 1) Clamp requested targets to safe range.
        leftTargetRPM  = clamp(leftTargetRPM,  MIN_RPM, MAX_RPM);
        rightTargetRPM = clamp(rightTargetRPM, MIN_RPM, MAX_RPM);

        // 2) Ramp current commands toward targets.
        double maxDelta = RAMP_RATE_RPM_PER_SEC * dt;

        currentLeftRPM  = rampToward(currentLeftRPM,  leftTargetRPM,  maxDelta);
        currentRightRPM = rampToward(currentRightRPM, rightTargetRPM, maxDelta);

        // 3) Convert current command RPM -> ticks/second for DcMotorEx.setVelocity().
        double leftVelocityTicksPerSec  = rpmToTicksPerSecond(currentLeftRPM);
        double rightVelocityTicksPerSec = rpmToTicksPerSecond(currentRightRPM);

        // 4) Apply velocities to motors using built-in velocity PIDF control.
        leftShooter.setVelocity(leftVelocityTicksPerSec);
        rightShooter.setVelocity(rightVelocityTicksPerSec);

        // 5) Read back actual velocity and compute actual RPM for telemetry & health monitoring.
        double leftActualTicksPerSec  = leftShooter.getVelocity();
        double rightActualTicksPerSec = rightShooter.getVelocity();
        // Convert to RPM
        double leftActualRPM  = ticksPerSecondToRpm(leftActualTicksPerSec);
        double rightActualRPM = ticksPerSecondToRpm(rightActualTicksPerSec);
        // Compute errors
        double leftErrorRPM  = leftActualRPM  - currentLeftRPM;
        double rightErrorRPM = rightActualRPM - currentRightRPM;

        // ----- READY-TO-FIRE LOGIC -----
        boolean leftWithinTolerance  = Math.abs(leftErrorRPM)  <= READY_TOLERANCE_RPM;
        boolean rightWithinTolerance = Math.abs(rightErrorRPM) <= READY_TOLERANCE_RPM;
        // Use the average actual RPM to check that we're above a minimum "spun up" threshold
        double avgActualRPM = 0.5 * (Math.abs(leftActualRPM) + Math.abs(rightActualRPM));
        boolean aboveMin = avgActualRPM >= READY_MIN_RPM;
        // Final readiness: both within tolerance AND above minimum RPM
        readyToFire = leftWithinTolerance && rightWithinTolerance && aboveMin;
        // ----- END READY-TO-FIRE LOGIC -----

        // 6) Basic error detection:
        //    If we are commanding high RPM but seeing almost zero actual RPM for a while,
        //    that might indicate a stall, unplugged motor, or broken encoder.
        boolean leftSuspect  = (currentLeftRPM  > 500.0 && Math.abs(leftActualRPM)  < 150.0);
        boolean rightSuspect = (currentRightRPM > 500.0 && Math.abs(rightActualRPM) < 150.0);

        // Serious error policy: for now, just report via telemetry.
        // If you want to actively shut down, you could set power to zero on severe error.
        if (leftSuspect) {
            telemetry.addData("Shooter Warning", "Left motor suspected stall or encoder issue");
        }
        if (rightSuspect) {
            telemetry.addData("Shooter Warning", "Right motor suspected stall or encoder issue");
        }

        // 7) Telemetry output for tuning and driver feedback.
        telemetry.addData("Shooter L Target RPM", "%.1f", currentLeftRPM);
        telemetry.addData("Shooter L Actual RPM", "%.1f (err=%.1f)", leftActualRPM, leftErrorRPM);

        telemetry.addData("Shooter R Target RPM", "%.1f", currentRightRPM);
        telemetry.addData("Shooter R Actual RPM", "%.1f (err=%.1f)", rightActualRPM, rightErrorRPM);

        // Optionally add raw velocity in ticks for debugging:
        // telemetry.addData("Shooter L vel (ticks/s)", "%.1f", leftActualTicksPerSec);
        // telemetry.addData("Shooter R vel (ticks/s)", "%.1f", rightActualTicksPerSec);
    }

    /**
     * Stop both shooter motors and reset internal RPM commands.
     */
    public void stop() {
        currentLeftRPM = 0.0;
        currentRightRPM = 0.0;
        leftShooter.setVelocity(0.0);
        rightShooter.setVelocity(0.0);
    }

    // -----------------------------
    // Helper methods
    // -----------------------------

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Move current toward target by at most maxDelta (symmetric).
     */
    private static double rampToward(double current, double target, double maxDelta) {
        double delta = target - current;
        if (Math.abs(delta) <= maxDelta) {
            return target;
        }
        return current + Math.signum(delta) * maxDelta;
    }

    private static double rpmToTicksPerSecond(double rpm) {
        // RPM -> RPS -> ticks/s
        return (rpm / 60.0) * TICKS_PER_REV;
    }

    private static double ticksPerSecondToRpm(double ticksPerSec) {
        // ticks/s -> RPS -> RPM
        return (ticksPerSec / TICKS_PER_REV) * 60.0;
    }

    /**
     * @return true if both shooter wheels are within tolerance of their commanded RPM
     *         AND above the minimum RPM threshold.
     */
    public boolean isReadyToFire() {
        return readyToFire;
    }

    /**
     * Reports the current encoder position and power level for each motor.
     */
    public void reportTelemetry() {
        if (leftShooter != null && rightShooter != null) {
            telemetry.addData("-----  SHOOTER", "  -----");
            telemetry.addData("LeftShooter", "Encoder: %2d, Power: %.2f",
                    leftShooter.getCurrentPosition(), leftShooter.getPower());
            telemetry.addData("RightShooter", "Encoder: %2d, Power: %.2f",
                    rightShooter.getCurrentPosition(), rightShooter.getPower());
        } else {
            telemetry.addData("Error", "Shooter motors are not initialized.");
        }
    }
}