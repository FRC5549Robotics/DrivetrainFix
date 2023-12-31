package frc.robot.subsystems;

import  com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.*;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import com.pathplanner.lib.commands.PPSwerveControllerCommand;
import com.pathplanner.lib.PathPlannerTrajectory;
import edu.wpi.first.wpilibj2.command.Command;


@SuppressWarnings("PMD.ExcessiveImports")
public class DrivetrainSubsystem extends SubsystemBase {

  // Robot swerve modules
  private final SwerveModule m_frontLeft =
      new SwerveModule(
          Constants.FRONT_LEFT_MODULE_DRIVE_MOTOR,
          Constants.FRONT_LEFT_MODULE_STEER_MOTOR,
          Constants.FRONT_LEFT_MODULE_STEER_ENCODER,
          Constants.CANCoder.kFrontLefTurningEncoderOffset
          );

  private final SwerveModule m_frontRight =
      new SwerveModule(
          Constants.FRONT_RIGHT_MODULE_DRIVE_MOTOR,
          Constants.FRONT_RIGHT_MODULE_STEER_MOTOR,
          Constants.FRONT_RIGHT_MODULE_STEER_ENCODER,
          Constants.CANCoder.kFrontRightTurningEncoderOffset
          );

  private final SwerveModule m_rearLeft =
      new SwerveModule(
        Constants.BACK_LEFT_MODULE_DRIVE_MOTOR,
        Constants.BACK_LEFT_MODULE_STEER_MOTOR,
        Constants.BACK_LEFT_MODULE_STEER_ENCODER,
          Constants.CANCoder.kRearLeftTurningEncoderOffset
          );

  private final SwerveModule m_rearRight =
      new SwerveModule(
        Constants.BACK_RIGHT_MODULE_DRIVE_MOTOR,
        Constants.BACK_RIGHT_MODULE_STEER_MOTOR,
        Constants.BACK_RIGHT_MODULE_STEER_ENCODER,
          Constants.CANCoder.kRearRightTurningEncoderOffset
          );

  private SwerveModule[] modules = {m_frontLeft, m_frontRight, m_rearLeft, m_rearRight};
  private double[] lastDistances;
  private double lastTime;
  private double offset = 0.0;
  private final Timer timer;

  public ChassisSpeeds m_chassisSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);
  private SwerveModuleState[] states = Constants.kDriveKinematics.toSwerveModuleStates(m_chassisSpeeds);


  // The gyro sensor
  private final AHRS m_ahrs = new AHRS();
//  private final Gyro m_gyro =  new ADIS16470_IMU(); // new ADXRS450_Gyro();
  // private final PigeonIMU m_pigeon = new PigeonIMU(DriveConstants.kPigeonPort);

  // Odometry class for tracking robot pose
  SwerveDriveOdometry m_odometry;

  //target pose and controller
  Pose2d m_targetPose;
  PIDController m_thetaController = new PIDController(1.0, 0.0, 0.05);

  private ChassisSpeeds speeds; 
    
  /** Creates a new DriveSubsystem. */
  public DrivetrainSubsystem() {
    fixBackRight();

    // Zero out the gyro.
    m_ahrs.calibrate();

    m_odometry = new SwerveDriveOdometry(Constants.kDriveKinematics, getHeading(), getModulePositions());

    for (SwerveModule module: modules) {
      module.resetDistance();
      module.syncTurningEncoders();
    }

    m_targetPose = m_odometry.getPoseMeters();
    m_thetaController.reset();
    m_thetaController.enableContinuousInput(-Math.PI, Math.PI);
    lastDistances = new double[]{
      m_frontLeft.getDriveDistanceMeters(),
      m_frontRight.getDriveDistanceMeters(),
      m_rearLeft.getDriveDistanceMeters(),
      m_rearRight.getDriveDistanceMeters(),
    };

    timer = new Timer();
    timer.reset();
    timer.start();
    lastTime = 0;
  }

  @Override
  public void periodic() {
    // Update the odometry in the periodic block
    updateOdometry(); 
    
    SmartDashboard.putNumber("Front Left CANCoder", m_frontLeft.getState().angle.getDegrees());
    SmartDashboard.putNumber("Front Right CANCoder", m_frontRight.getState().angle.getDegrees());
    SmartDashboard.putNumber("Back Left CANCoder", m_rearLeft.getState().angle.getDegrees());
    SmartDashboard.putNumber("Back Right CANCoder", m_rearRight.getState().angle.getDegrees());

    SmartDashboard.putNumber("Front Left CANCoder", m_frontLeft.getTurnCANcoder().getPosition());
    SmartDashboard.putNumber("Front Right CANCoder", m_frontRight.getTurnCANcoder().getPosition());
    SmartDashboard.putNumber("Back Left CANCoder", m_rearLeft.getTurnCANcoder().getPosition());
    SmartDashboard.putNumber("Back Right CANCoder", m_rearRight.getTurnCANcoder().getPosition());

    SmartDashboard.putNumber("Front Left Neo Encoder", m_frontLeft.getTurnEncoder().getPosition());
    SmartDashboard.putNumber("Front Right Neo Encoder", m_frontRight.getTurnEncoder().getPosition());
    SmartDashboard.putNumber("Back Left Neo Encoder", m_rearLeft.getTurnEncoder().getPosition());
    SmartDashboard.putNumber("Back Right Neo Encoder", m_rearRight.getTurnEncoder().getPosition());
    
    SmartDashboard.putNumber("Heading", getHeading().getDegrees());
    
    SmartDashboard.putNumber("currentX", getPose().getX());
    SmartDashboard.putNumber("currentY", getPose().getY());
    SmartDashboard.putNumber("currentAngle", getPose().getRotation().getRadians());
    SmartDashboard.putNumber("targetPoseAngle", m_targetPose.getRotation().getRadians());

    if(Math.abs(m_frontRight.getTurnEncoder().getPosition() - m_frontRight.getTurnCANcoderAngle()) > 2){
      m_frontRight.getTurnEncoder().setPosition(m_frontRight.getTurnCANcoderAngle());
    }
    // SmartDashboard.putNumber("Distance 0", modules[0].getDriveDistanceMeters());
    // SmartDashboard.putNumber("Distance 1", modules[1].getDriveDistanceMeters());
    // SmartDashboard.putNumber("Distance 2", modules[2].getDriveDistanceMeters());
    // SmartDashboard.putNumber("Distance 3", modules[3].getDriveDistanceMeters());

    // SmartDashboard.putNumber("Angle 0", modules[0].getTurnCANcoderAngle());
    // SmartDashboard.putNumber("Angle 1", modules[1].getTurnCANcoderAngle());
    // SmartDashboard.putNumber("Angle 2", modules[2].getTurnCANcoderAngle());
    // SmartDashboard.putNumber("Angle 3", modules[3].getTurnCANcoderAngle());

  }

  public void updateOdometry() {
    double[] distances = new double[]{
      m_frontLeft.getDriveDistanceMeters(),
      m_frontRight.getDriveDistanceMeters(),
      m_rearLeft.getDriveDistanceMeters(),
      m_rearRight.getDriveDistanceMeters(),
    };
    double time = timer.get();
    double dt = time - lastTime;
    lastTime = time;
    if (dt == 0) return;
    m_odometry.update(getHeading(), getModulePositions());
    lastDistances = distances;
  }


  public double getTranlationalVelocity() {
    return Math.hypot(this.speeds.vxMetersPerSecond, this.speeds.vyMetersPerSecond);
  }
  /**
   * Returns the currently-estimated pose of the robot.
   *
   * @return The pose.
   */
  public Pose2d getPose() {
    return m_odometry.getPoseMeters();
  }

  /**
   * Resets the odometry to the specified pose.
   *
   * @param pose The pose to which to set the odometry.
   */
  public void resetOdometry(Pose2d pose) {
    /* Don't reset all the motors' positions. Otherwise the robot thinks it has teleported!
    for (SwerveModule module: modules) {
      module.resetDistance();
    }
    */
    m_odometry.resetPosition(
      getHeading(), 
      getModulePositions(), 
      pose);
  }

  /**
   * Method to rotate the relative orientation of the target pose at a given rate.
   *
   * @param deltaTheta How much to rotate the target orientation per loop.
   */
  public void rotateRelative(Rotation2d deltaTheta) {
    Transform2d transform = new Transform2d(new Translation2d(), deltaTheta);
    m_targetPose = m_targetPose.transformBy(transform);
  }

  /**
   * Method to set the absolute orientation of the target pose.
   *
   * @param theta The target orientation.
   */
  public void rotateAbsolute(Rotation2d theta) {
    m_targetPose = new Pose2d(new Translation2d(), theta);
  }

  /**
   * Method to get the output of the chassis orientation PID controller.
   *
   */
  public double getThetaDot() {
    double setpoint = m_targetPose.getRotation().getRadians();
    double measurement = getPose().getRotation().getRadians();
    double output = m_thetaController.calculate(measurement, setpoint);
    SmartDashboard.putNumber("PID out", output);
    return output;
  }

  /**
   * Method to drive the robot with given velocities.
   *
   * @param speeds ChassisSpeeds object with the desired chassis speeds [m/s and rad/s].
   */
  @SuppressWarnings("ParameterName")
  public void drive(ChassisSpeeds speeds, boolean normalize) {

    this.speeds = speeds;

    if (speeds.vxMetersPerSecond == 0 && speeds.vyMetersPerSecond == 0 && speeds.omegaRadiansPerSecond == 0) {
      brake();
      return;
    }

    SwerveModuleState[] swerveModuleStates =
        Constants.kDriveKinematics.toSwerveModuleStates(speeds);
           
    if (normalize) normalizeDrive(swerveModuleStates, speeds);
    
    setModuleStates(swerveModuleStates);
  }

  public void openLoopDrive(ChassisSpeeds speeds) {
    this.speeds = speeds;
    if (speeds.vxMetersPerSecond == 0 && speeds.vyMetersPerSecond == 0 && speeds.omegaRadiansPerSecond == 0) {
      brake();
      return;
    }

    SwerveModuleState[] swerveModuleStates =
        Constants.kDriveKinematics.toSwerveModuleStates(speeds);
           
    normalizeDrive(swerveModuleStates, speeds);
    
    setModuleStates(swerveModuleStates);
  }

  public void normalizeDrive(SwerveModuleState[] desiredStates, ChassisSpeeds speeds) {
    double translationalK = Math.hypot(speeds.vxMetersPerSecond, speeds.vyMetersPerSecond) / Constants.kMaxTranslationalVelocity;
    double rotationalK = Math.abs(speeds.omegaRadiansPerSecond) / Constants.kMaxRotationalVelocity;
    double k = Math.max(translationalK, rotationalK);

    // Find the how fast the fastest spinning drive motor is spinning                                       
    double realMaxSpeed = 0.0;
    for (SwerveModuleState moduleState : desiredStates) {
      realMaxSpeed = Math.max(realMaxSpeed, Math.abs(moduleState.speedMetersPerSecond));
    }

    double scale = Math.min(k * Constants.kMaxSpeedMetersPerSecond / realMaxSpeed, 1);
    for (SwerveModuleState moduleState : desiredStates) {
      moduleState.speedMetersPerSecond *= scale;
    }
  }

  public void brake() {
    for (SwerveModule module : modules) {
      module.setDesiredState(new SwerveModuleState(0, module.getState().angle));
    }
  }

  /**
   * Sets the swerve ModuleStates.
   *
   * @param desiredStates The desired SwerveModule states.
   */
  public void setModuleStates(SwerveModuleState[] desiredStates) {

    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Preferences.getDouble("kMaxSpeedMetersPerSecond", Constants.kMaxSpeedMetersPerSecond));

        for (int i = 0; i <= 3; i++) {
          modules[i].setDesiredState(desiredStates[i]);
        }
  }

  public void setOpenLoopStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Preferences.getDouble("kMaxSpeedMetersPerSecond", Constants.kMaxSpeedMetersPerSecond));

    for (int i = 0; i <= 3; i++) {
      modules[i].setOpenLoopState(desiredStates[i]);
    }
  }

  public SwerveModuleState[] getModuleStates() {

    SwerveModuleState[] states = new SwerveModuleState[4];

    for (int i = 0; i <= 3; i++) {
      states[i++] = modules[i].getState();
      
    }

    return states;
  }

  /** Resets the drive encoders to currently read a position of 0. */
  public void resetEncoders() {

    for (SwerveModule module: modules) {
      module.resetEncoders();
    }
  }

  // /** Zeroes the heading of the robot. */
  // public void zeroHeading() {
  //   m_ahrs.zeroYaw();
  //   offset = 0;
  //   m_targetPose = new Pose2d(new Translation2d(), new Rotation2d());
  // }

  // public void resetOdometry(double heading, Pose2d pose) {
  //   zeroHeading();
  //   offset = heading;
  //   m_odometry.resetPosition(Rotation2d.fromDegrees(heading),
  //   getModulePositions(),
  //   pose);
  // }
  public void zeroGyroscope(){
    m_ahrs.reset();
  }

  /**
   * Returns the heading of the robot.
   *
   * @return the robot's heading as a Rotation2d
   */
  public Rotation2d getHeading() {
    float raw_yaw = m_ahrs.getYaw() - (float)offset; // Returns yaw as -180 to +180.
    // float raw_yaw = m_ahrs.getYaw(); // Returns yaw as -180 to +180.
    float calc_yaw = raw_yaw;

    if (0.0 > raw_yaw ) { // yaw is negative
      calc_yaw += 360.0;
    }
    return Rotation2d.fromDegrees(calc_yaw);
  }
  

  private SwerveModulePosition[] getModulePositions(){
    return new SwerveModulePosition[]{
      new SwerveModulePosition(m_frontLeft.getDriveDistanceMeters(), m_frontLeft.getState().angle),
      new SwerveModulePosition(m_frontRight.getDriveDistanceMeters(), m_frontRight.getState().angle),
      new SwerveModulePosition(m_rearLeft.getDriveDistanceMeters(), m_rearLeft.getState().angle),
      new SwerveModulePosition(m_rearRight.getDriveDistanceMeters(), m_rearRight.getState().angle)};
  }

  public ChassisSpeeds getChassisSpeeds(){
    return m_chassisSpeeds;
  }
  private void fixBackRight(){
    m_rearRight.getTurnMotor().setInverted(false);
  }

  public Command followTrajectoryCommand(PathPlannerTrajectory traj) {
    return new PPSwerveControllerCommand(
        traj, 
        this::getPose, // Pose supplier
        Constants.kDriveKinematics, // SwerveDriveKinematics
        new PIDController(1, 0, 0), // X controller. Tune these values for your robot. Leaving them 0 will only use feedforwards.
        new PIDController(3.5, 0, 0), // Y controller (usually the same values as X controller)
        new PIDController(1.7, 0, 0), // Rotation controller. Tune these values for your robot. Leaving them 0 will only use feedforwards.
        (SwerveModuleState[] states) -> {
               this.m_chassisSpeeds = Constants.kDriveKinematics.toChassisSpeeds(states);
       }, // Module states consumer
        true, // Should the path be automatically mirrored depending on alliance color. Optional, defaults to true
        this // Requires this drive subsystem
    );
  }
}