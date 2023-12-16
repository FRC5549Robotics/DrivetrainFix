// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants; 
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Ultrasonic;

public class Shintake extends SubsystemBase {
  /** Creates a new Shintake. */
  CANSparkMax motor_shintake;
  PIDController pid = new PIDController(Constants.kP, Constants.kI, Constants.kD);
  SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(Constants.kS, Constants.kV, Constants.kA);
  double targetRPM;
  Compressor pcmCompressor;
  DoubleSolenoid shooterSolenoid;
  Ultrasonic m_rangeFinder = new Ultrasonic(Constants.ULTRASONIC_PING, Constants.ULTRASONIC_ECHO);


  public Shintake() {
    motor_shintake = new CANSparkMax(Constants.SHINTAKE_MOTOR , MotorType.kBrushless);
    targetRPM = 111000;
    pcmCompressor = new Compressor(PneumaticsModuleType.CTREPCM);    
    pcmCompressor.enableDigital();
    pcmCompressor.disable(); //disables pneumadics
    shooterSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, Constants.SHOOTER_SOLENOID_FORWARD, Constants.SHOOTER_SOLENOID_REVERSE);
    boolean enabled = pcmCompressor.isEnabled();
    System.out.println(pcmCompressor.isEnabled());
    boolean pressureSwitch = pcmCompressor.getPressureSwitchValue();
    double current = pcmCompressor.getCurrent();
    shooterSolenoid.set(Value.kForward);
  }
  public void extend(){
    shooterSolenoid.set(Value.kForward);
  }
  public void retract(){
    shooterSolenoid.set(Value.kReverse);
  }
  public void shintake_run(){
    motor_shintake.set(-Constants.SHINTAKE_SPEED);
  }

  public void shintake_back(){
    motor_shintake.set(Constants.SHINTAKE_SPEED);
  }

  public void shintake_stop(){
    motor_shintake.set(0);
  }
  
  public void shintake_set_speed(double speed){
    motor_shintake.set(speed);
  }
  public void on(double setPoint) {
    
    targetRPM = setPoint;
    motor_shintake.setVoltage(pid.calculate((motor_shintake.getEncoder().getVelocity() * (1 / (1.43 * 60))), setPoint) + feedforward.calculate(setPoint));
  }
  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Ultrasonic", m_rangeFinder.getRangeInches());
  }
}

