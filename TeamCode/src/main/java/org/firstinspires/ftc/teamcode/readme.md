# TeamCode README

This README provides an overview of the TeamCode module structure for the FTC robot codebase. It explains how subsystems, utilities, TeleOps, and Autonomous programs are organized so new programmers can quickly understand how to extend or modify the code.

---

## 1. Folder Structure

```
TeamCode/
│
├── Robot.java
│
├── autonomous/
│   ├── AutoLeaveOnly.java
│   ├── AutoParkOppGoal.java
│   ├── AutoWallLane.java
│
├── subsystems/
│   ├── Drivetrain.java
│   ├── Shooter2Wheel.java
│   ├── LedIndicator.java  (optional; if hardware missing, code handles gracefully)
│   └── ... (future subsystems)
│
├── utils/
│   ├── TelemetryManager.java
│   ├── TimedDrive.java
│   └── ... (shared helpers)
│
├── teleop/
│   ├── MecDrive.java
│   ├── TestShooter.java
│   └── ... (future teleops)
│
└── ...
```

---

## 2. Central Robot Class

### `Robot.java`
This class constructs and stores shared singletons for all subsystems and utilities.

**Responsibilities**
- Construct and initialize subsystems
- Provide getters for TeleOps and Autos
- Provide safety checks so missing hardware does not crash the OpMode

---

## 3. Subsystems

Subsystems encapsulate hardware behavior. Each one typically has:

- A constructor using `HardwareMap`
- An `init()` method
- An `operate()` or equivalent action method
- A `reportTelemetry()` method

### Example subsystem: `Drivetrain.java`
Capabilities:
- Mecanum drive
- Field-agnostic coordinate system
- Power factor scaling
- Used by both TeleOp and Autonomous

### `LedIndicator.java`
- Supports optional LED hardware
- Constructor catches missing hardware and continues safely

---

## 4. Utility Classes

Utilities provide reusable features for all TeleOps and Autos.

### `TelemetryManager.java`
- Centralized telemetry formatting
- Adds subsystem-specific data consistently
- Called in TeleOps and Autos

### `TimedDrive.java`
Provides *time-based* autonomous motion helpers.

Example:
```java
TimedDrive.forward(drivetrain, 0.5, 800);
TimedDrive.strafeLeft(drivetrain, 0.5, 600);
TimedDrive.stop(drivetrain);
```

Benefits:
- Cleaner autonomous code
- Eliminates repeated boilerplate
- Makes tuning easier

---

## 5. TeleOp Programs

### `teleop/MecDrive.java`
Primary driver-controlled OpMode.

Features:
- Left stick X/Y for translation
- Right stick X for rotation
- Adjustable power factor using gamepad A
- Long-press turbo mode
- Uses:
  - `Robot`
  - `Drivetrain`
  - `TelemetryManager`

Add additional TeleOps by copying this structure and adding new subsystems.

---

## 6. Autonomous Programs

### `autonomous/AutoLeaveOnly.java`
Purpose:
- Score **3 Auto points** by leaving the starting tile

Movement:
- Timed forward drive using `TimedDrive`

### `autonomous/AutoParkOppGoal.java`
Purpose:
- Leave starting tile (3 pts)
- Drive to a fixed defensive parking location in front of opponent's goal (legal and aligned with strategy)

### `autonomous/AutoWallLane.java`
Purpose:
- Perform a **13‑point** route:
  - Leave tile (3 pts)
  - Enter wall lane
  - Drive forward into PARKING ZONE (10 pts)
- Uses `TimedDrive` for clean readable code

---

## 7. Adding New Code

### To add a new TeleOp:
1. Create a class under `teleop/`
2. Extend `OpMode` or `LinearOpMode`
3. Create a `Robot robot = new Robot(hardwareMap, telemetry);`
4. Call subsystem `.init()`
5. Use subsystem methods in `loop()`

### To add a new Autonomous:
1. Create a class under `autonomous/`
2. Extend `LinearOpMode`
3. Use:
   - `Robot`
   - `Drivetrain`
   - `TimedDrive`
4. Combine timed movements to build routines

---

## 8. Helpful Notes for New Students

- **All real robot behavior runs through subsystems**, not through TeleOps directly.
- **TimedDrive** is the recommended way to write Autonomous early in the season.
- **TelemetryManager** keeps output clean and consistent.
- The robot structure is intentionally simple so new students can start modifying code safely.
- TeleOps are for humans. Autonomous is for repeatable paths. Subsystems are for hardware.

---

## 9. Future Expansion Points

- Add IMU + encoder fusion (optional)
- Add an intake subsystem
- Add an outtake/shooter subsystem
- Add AprilTag-based localization
- Add a separate `FieldConstants` class
- Add path-following once robot matures

---

If you want, I can also:
- add diagrams,
- annotate classes,
- generate JavaDocs,
- or create a GitHub‑ready repo structure.

## TeamCode Module

Welcome!

This module, TeamCode, is the place where you will write/paste the code for your team's
robot controller App. This module is currently empty (a clean slate) but the
process for adding OpModes is straightforward.

## Creating your own OpModes

The easiest way to create your own OpMode is to copy a Sample OpMode and make it your own.

Sample opmodes exist in the FtcRobotController module.
To locate these samples, find the FtcRobotController module in the "Project/Android" tab.

Expand the following tree elements:
 FtcRobotController/java/org.firstinspires.ftc.robotcontroller/external/samples

### Naming of Samples

To gain a better understanding of how the samples are organized, and how to interpret the
naming system, it will help to understand the conventions that were used during their creation.

These conventions are described (in detail) in the sample_conventions.md file in this folder.

To summarize: A range of different samples classes will reside in the java/external/samples.
The class names will follow a naming convention which indicates the purpose of each class.
The prefix of the name will be one of the following:

Basic:  	This is a minimally functional OpMode used to illustrate the skeleton/structure
            of a particular style of OpMode.  These are bare bones examples.

Sensor:    	This is a Sample OpMode that shows how to use a specific sensor.
            It is not intended to drive a functioning robot, it is simply showing the minimal code
            required to read and display the sensor values.

Robot:	    This is a Sample OpMode that assumes a simple two-motor (differential) drive base.
            It may be used to provide a common baseline driving OpMode, or
            to demonstrate how a particular sensor or concept can be used to navigate.

Concept:	This is a sample OpMode that illustrates performing a specific function or concept.
            These may be complex, but their operation should be explained clearly in the comments,
            or the comments should reference an external doc, guide or tutorial.
            Each OpMode should try to only demonstrate a single concept so they are easy to
            locate based on their name.  These OpModes may not produce a drivable robot.

After the prefix, other conventions will apply:

* Sensor class names are constructed as:    Sensor - Company - Type
* Robot class names are constructed as:     Robot - Mode - Action - OpModetype
* Concept class names are constructed as:   Concept - Topic - OpModetype

Once you are familiar with the range of samples available, you can choose one to be the
basis for your own robot.  In all cases, the desired sample(s) needs to be copied into
your TeamCode module to be used.

This is done inside Android Studio directly, using the following steps:

 1) Locate the desired sample class in the Project/Android tree.

 2) Right click on the sample class and select "Copy"

 3) Expand the  TeamCode/java folder

 4) Right click on the org.firstinspires.ftc.teamcode folder and select "Paste"

 5) You will be prompted for a class name for the copy.
    Choose something meaningful based on the purpose of this class.
    Start with a capital letter, and remember that there may be more similar classes later.

Once your copy has been created, you should prepare it for use on your robot.
This is done by adjusting the OpMode's name, and enabling it to be displayed on the
Driver Station's OpMode list.

Each OpMode sample class begins with several lines of code like the ones shown below:

```
 @TeleOp(name="Template: Linear OpMode", group="Linear Opmode")
 @Disabled
```

The name that will appear on the driver station's "opmode list" is defined by the code:
 ``name="Template: Linear OpMode"``
You can change what appears between the quotes to better describe your opmode.
The "group=" portion of the code can be used to help organize your list of OpModes.

As shown, the current OpMode will NOT appear on the driver station's OpMode list because of the
  ``@Disabled`` annotation which has been included.
This line can simply be deleted , or commented out, to make the OpMode visible.



## ADVANCED Multi-Team App management:  Cloning the TeamCode Module

In some situations, you have multiple teams in your club and you want them to all share
a common code organization, with each being able to *see* the others code but each having
their own team module with their own code that they maintain themselves.

In this situation, you might wish to clone the TeamCode module, once for each of these teams.
Each of the clones would then appear along side each other in the Android Studio module list,
together with the FtcRobotController module (and the original TeamCode module).

Selective Team phones can then be programmed by selecting the desired Module from the pulldown list
prior to clicking to the green Run arrow.

Warning:  This is not for the inexperienced Software developer.
You will need to be comfortable with File manipulations and managing Android Studio Modules.
These changes are performed OUTSIDE of Android Studios, so close Android Studios before you do this.
 
Also.. Make a full project backup before you start this :)

To clone TeamCode, do the following:

Note: Some names start with "Team" and others start with "team".  This is intentional.

1)  Using your operating system file management tools, copy the whole "TeamCode"
    folder to a sibling folder with a corresponding new name, eg: "Team0417".

2)  In the new Team0417 folder, delete the TeamCode.iml file.

3)  the new Team0417 folder, rename the "src/main/java/org/firstinspires/ftc/teamcode" folder
    to a matching name with a lowercase 'team' eg:  "team0417".

4)  In the new Team0417/src/main folder, edit the "AndroidManifest.xml" file, change the line that contains
         package="org.firstinspires.ftc.teamcode"
    to be
         package="org.firstinspires.ftc.team0417"

5)  Add:    include ':Team0417' to the "/settings.gradle" file.
    
6)  Open up Android Studios and clean out any old files by using the menu to "Build/Clean Project""
