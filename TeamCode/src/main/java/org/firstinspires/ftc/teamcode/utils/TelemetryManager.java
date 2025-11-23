package org.firstinspires.ftc.teamcode.utils;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TelemetryManager {

    private final Telemetry telemetry;

    public TelemetryManager(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    public void addStatus(String status) {
        telemetry.addData("Status", status);
    }

    public void addError(String error) {
        telemetry.addData("Error", error);
    }

    public void addDrivetrainData(double powerFactor, double lx, double ly, double rx) {
        telemetry.addData("Drivetrain", "PF: %.2f | LX/LY/RX: %.2f/%.2f/%.2f", powerFactor, lx, ly, rx);
    }

    public void addShooterData(String set, String preset, double targetRPM, boolean ready) {
        telemetry.addData("Shooter", "Set: %s | Preset: %s | RPM: %.0f | Ready: %s",
                set, preset, targetRPM, ready ? "YES" : "NO");
    }

    public void update() {
        telemetry.update();
    }
}
