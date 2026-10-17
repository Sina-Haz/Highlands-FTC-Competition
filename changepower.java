package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name = "Highland Titans v1")
public class changepower extends LinearOpMode {

    private static final double DEADZONE = 0.1;

    private static final double INTAKE_POWER = 1.0;
    private static final double LEFT_SERVO_POWER = 1.0;
    private static final double RIGHT_SERVO_POWER = -1.0;
    private static final double FEEDER_POWER = 1.0;

    private DcMotor leftdrive;
    private DcMotor rightdrive;
    private DcMotor intake;
    private DcMotor shooterwheel;

    private CRServo servoleftintake;
    private CRServo servorightintake;
    private CRServo flywheel;

    private double deadzone(double value) {
        return Math.abs(value) < DEADZONE ? 0.0 : value;
    }

    private void stopAll() {
        leftdrive.setPower(0);
        rightdrive.setPower(0);
        intake.setPower(0);
        shooterwheel.setPower(0);

        servoleftintake.setPower(0);
        servorightintake.setPower(0);
        flywheel.setPower(0);
    }

    @Override
    public void runOpMode() {

        // Hardware configuration names must match exactly.
        leftdrive = hardwareMap.get(DcMotor.class, "leftdrive");
        rightdrive = hardwareMap.get(DcMotor.class, "rightdrive");
        intake = hardwareMap.get(DcMotor.class, "intake");
        shooterwheel = hardwareMap.get(DcMotor.class, "shooterwheel");

        servoleftintake =
                hardwareMap.get(CRServo.class, "servoleftintake");
        servorightintake =
                hardwareMap.get(CRServo.class, "servorightintake");
        flywheel =
                hardwareMap.get(CRServo.class, "flywheel");

        stopAll();

        // Motor and servo directions.
        leftdrive.setDirection(DcMotor.Direction.REVERSE);
        rightdrive.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        shooterwheel.setDirection(DcMotor.Direction.FORWARD);

        servoleftintake.setDirection(CRServo.Direction.FORWARD);
        servorightintake.setDirection(CRServo.Direction.FORWARD);
        flywheel.setDirection(CRServo.Direction.FORWARD);

        // Direct power control.
        for (DcMotor motor : new DcMotor[]{
                leftdrive, rightdrive, intake, shooterwheel}) {
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        leftdrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightdrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooterwheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        boolean shooterOn = false;
        boolean intakeOn = false;
        boolean sideServosOn = false;
        boolean feederOn = false;

        // Integer percentage avoids decimal rounding accumulation.
        int shooterPercent = 80;

        telemetry.addLine("Ready - all mechanisms OFF");
        telemetry.addLine("Square: shooter | Triangle: intake");
        telemetry.addLine("L1: side servos | R1: feeder");
        telemetry.addLine("D-pad Up/Down: shooter power +/-5%");
        telemetry.update();

        try {
            waitForStart();

            if (isStopRequested()) {
                return;
            }

            // Track each button independently.
            boolean lastX = gamepad1.x;
            boolean lastY = gamepad1.y;
            boolean lastL1 = gamepad1.left_bumper;
            boolean lastR1 = gamepad1.right_bumper;
            boolean lastUp = gamepad1.dpad_up;
            boolean lastDown = gamepad1.dpad_down;

            while (opModeIsActive()) {

                // Tank drive.
                leftdrive.setPower(
                        -deadzone(gamepad1.left_stick_y));
                rightdrive.setPower(
                        -deadzone(gamepad1.right_stick_y));

                // Read controller buttons.
                boolean x = gamepad1.x;
                boolean y = gamepad1.y;
                boolean l1 = gamepad1.left_bumper;
                boolean r1 = gamepad1.right_bumper;
                boolean up = gamepad1.dpad_up;
                boolean down = gamepad1.dpad_down;

                // Square: toggle shooter.
                if (x && !lastX) {
                    shooterOn = !shooterOn;
                }

                // Triangle: toggle intake motor.
                if (y && !lastY) {
                    intakeOn = !intakeOn;
                }

                // L1: toggle both side servos.
                if (l1 && !lastL1) {
                    sideServosOn = !sideServosOn;
                }

                // R1: toggle internal feeder.
                if (r1 && !lastR1) {
                    feederOn = !feederOn;
                }

                // Adjust shooter power once per press.
                // Ignore adjustment if Up and Down are both pressed.
                if (up && !lastUp && !down) {
                    shooterPercent = Math.min(100, shooterPercent + 5);
                }

                if (down && !lastDown && !up) {
                    shooterPercent = Math.max(0, shooterPercent - 5);
                }

                // Remember button states for the next loop.
                lastX = x;
                lastY = y;
                lastL1 = l1;
                lastR1 = r1;
                lastUp = up;
                lastDown = down;

                // Convert percentage to motor power: 80 -> 0.80.
                double shooterPower = shooterPercent / 100.0;

                // Apply mechanism commands.
                shooterwheel.setPower(
                        shooterOn ? shooterPower : 0.0);

                intake.setPower(
                        intakeOn ? INTAKE_POWER : 0.0);

                servoleftintake.setPower(
                        sideServosOn ? LEFT_SERVO_POWER : 0.0);

                servorightintake.setPower(
                        sideServosOn ? RIGHT_SERVO_POWER : 0.0);

                flywheel.setPower(
                        feederOn ? FEEDER_POWER : 0.0);

                // Driver Station display.
                telemetry.addData(
                        "Shooter", shooterOn ? "ON" : "OFF");
                telemetry.addData(
                        "Selected shooter power", "%d%%", shooterPercent);
                telemetry.addData(
                        "Intake", intakeOn ? "ON" : "OFF");
                telemetry.addData(
                        "Side servos", sideServosOn ? "ON" : "OFF");
                telemetry.addData(
                        "Feeder", feederOn ? "ON" : "OFF");
                telemetry.update();

                idle();
            }
        } finally {
            stopAll();
        }
    }
}
