package frc.robot.subsystems;

import java.io.Console;

import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMax.ControlType;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class SwerveModule extends SubsystemBase {

    private final CANSparkMax m_driveMotor;
    private final CANSparkMax m_turningMotor;

    private final RelativeEncoder m_driveEncoder;
    private final RelativeEncoder m_turningEncoder;

    private final CANCoder m_turningCANCoder;

    // absolute offset for the CANCoder so that the wheels can be aligned when the
    // robot is turned on
//    private final Rotation2d m_CANCoderOffset;

    private final SparkMaxPIDController m_turningController;
    private final SparkMaxPIDController m_driveController;    

    /**
     * Constructs a SwerveModule.
     *
     * @param driveMotorChannel   ID for the drive motor.
     * @param turningMotorChannel ID for the turning motor.
     */
    public SwerveModule(
                        int driveMotorChannel,
                        int turningMotorChannel,
                        int turningCANCoderChannel,
                        double turningCANCoderOffsetDegrees) {

        m_driveMotor = new CANSparkMax(driveMotorChannel, MotorType.kBrushless);
        m_driveMotor.restoreFactoryDefaults();
        m_turningMotor = new CANSparkMax(turningMotorChannel, MotorType.kBrushless);
        m_turningMotor.restoreFactoryDefaults();
        m_driveMotor.burnFlash();
        m_turningMotor.burnFlash();
        Timer.delay(0.5);

        m_driveEncoder = m_driveMotor.getEncoder();
        Timer.delay(1);
        System.out.println("initialized");
        m_turningCANCoder = new CANCoder(turningCANCoderChannel);
        m_turningCANCoder.setPositionToAbsolute();
        m_turningCANCoder.configAbsoluteSensorRange(AbsoluteSensorRange.Unsigned_0_to_360);
        m_turningCANCoder.setPosition(0);        
       
        m_turningEncoder = m_turningMotor.getEncoder();
//        m_CANCoderOffset = Rotation2d.fromDegrees(turningCANCoderOffsetDegrees);

        // m_driveMotor.setIdleMode(IdleMode.kBrake);
        // m_turningMotor.setIdleMode(IdleMode.kCoast);

        m_driveMotor.setIdleMode(IdleMode.kBrake);
        m_turningMotor.setIdleMode(IdleMode.kBrake);
        m_driveMotor.setSmartCurrentLimit(40);
        m_turningMotor.setSmartCurrentLimit(40);

        // m_driveEncoder returns RPM by default. Use setVelocityConversionFactor() to
        // convert that to meters per second.
        m_driveEncoder.setVelocityConversionFactor(Constants.kDriveConversionFactor / 60.0);
        m_driveEncoder.setPositionConversionFactor(Constants.kDriveConversionFactor);

        m_turningEncoder.setPositionConversionFactor(360.0 / Constants.kTurnPositionConversionFactor);

        m_turningController = m_turningMotor.getPIDController();
        m_driveController = m_driveMotor.getPIDController();

        m_driveMotor.enableVoltageCompensation(12);

        m_turningController.setP(Constants.kTurningP);
        m_turningController.setI(Constants.kTurningI);
        m_turningController.setD(Constants.kTurningD);

        // 401 only sets P of the drive PID
        m_driveController.setP(Constants.kDriveP);
        m_driveController.setI(Constants.kDriveI);
        m_driveController.setD(Constants.kDriveD);
    }

    /**
     * Returns the current state of the module.
     *
     * @return The current state of the module.
     */
    public SwerveModuleState getState() {
        // getPosition() returns the number of cumulative rotations.
        // Convert that to 0.0 to 1.0
        // double m1 = m_turningEncoder.getPosition() % 360.0;
        // double m2 = (m1 < 0) ? m1 + 360 : m1;

        double m2 = (m_turningEncoder.getPosition() % 360 + 360) % 360;
        String x;
        String y;
        x= null;
        y="3";
        String a = x != null ? y : x;

        return new SwerveModuleState(m_driveEncoder.getVelocity(), new Rotation2d(m2 * Math.PI / 180));
    }

    public CANSparkMax getTurnMotor() {
        return m_turningMotor;
    }

    public RelativeEncoder getTurnEncoder() {
        return m_turningEncoder;
    }

    public CANCoder getTurnCANcoder() {
        return m_turningCANCoder;
    }

    public double getTurnCANcoderAngle() {
        return m_turningCANCoder.getAbsolutePosition();
    }

    public Rotation2d adjustedAngle = new Rotation2d();

    /**
     * Sets the desired state for the module.
     *
     * @param state Desired state with speed (in meters per second?) and angle (in
     *              degrees).
     */
    public void setDesiredState(SwerveModuleState state) {

        Rotation2d curAngle = Rotation2d.fromDegrees(m_turningEncoder.getPosition());

        double delta = deltaAdjustedAngle(state.angle.getDegrees(), curAngle.getDegrees());

        // Calculate the drive motor output from the drive PID controller.
        double driveOutput = state.speedMetersPerSecond;

        if (Math.abs(delta) > 90) {
            driveOutput *= -1;
            delta -= Math.signum(delta) * 180;
        }

        adjustedAngle = Rotation2d.fromDegrees(delta + curAngle.getDegrees());

        m_turningController.setReference(
            adjustedAngle.getDegrees(),
            ControlType.kPosition
        );        

        SmartDashboard.putNumber("Commanded Velocity", driveOutput);

        m_driveController.setReference(driveOutput, ControlType.kVelocity, 0, Constants.kDriveFF * driveOutput);
    }

    public void setOpenLoopState(SwerveModuleState state) {
        Rotation2d curAngle = Rotation2d.fromDegrees(m_turningEncoder.getPosition());

        double delta = deltaAdjustedAngle(state.angle.getDegrees(), curAngle.getDegrees());

        // Calculate the drive motor output from the drive PID controller.
        double driveOutput = state.speedMetersPerSecond;

        if (Math.abs(delta) > 90) {
            driveOutput *= -1;
            delta -= Math.signum(delta) * 180;
        }

        adjustedAngle = Rotation2d.fromDegrees(delta + curAngle.getDegrees());

        m_turningController.setReference(
            adjustedAngle.getDegrees(),
            ControlType.kPosition
        );        

        SmartDashboard.putNumber("Commanded Velocity", driveOutput);

        m_driveMotor.setVoltage(Constants.kDriveFF * driveOutput);
    }

    //calculate the angle motor setpoint based on the desired angle and the current angle measurement
    // Arguments are in radians.
    public double deltaAdjustedAngle(double targetAngle, double currentAngle) {

        return ((targetAngle - currentAngle + 180) % 360 + 360) % 360 - 180;
    }

    public double getDriveDistanceMeters() {
        return m_driveEncoder.getPosition();
    }

    public void resetDistance() {
        m_driveEncoder.setPosition(0.0);
    }

    public void syncTurningEncoders() {
        m_turningEncoder.setPosition(m_turningCANCoder.getAbsolutePosition());
    }

    /** Zeros all the SwerveModule encoders. */
    public void resetEncoders() {
        // Reset the cumulative rotation counts of the SparkMax motors
        m_turningEncoder.setPosition(0.0);

        m_turningCANCoder.setPosition(0.0);
        m_turningCANCoder.configMagnetOffset(m_turningCANCoder.configGetMagnetOffset() - m_turningCANCoder.getAbsolutePosition());
    }
}
