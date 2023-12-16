// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.


package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Indexer extends SubsystemBase {
  /** Creates a new Indexer. */
  CANSparkMax motor_indexer;

  public Indexer() {
    motor_indexer = new CANSparkMax(Constants.INDEXER_MOTOR, MotorType.kBrushless);
  }

  public void indexer_run(){
    motor_indexer.set(Constants.INDEXER_SPEED);
  }

  public void indexer_back(){
    motor_indexer.set(-Constants.INDEXER_SPEED);
  }

  public void indexer_stop(){
    motor_indexer.set(0);
  }
  
  public void indexer_set_speed(double speed){
    motor_indexer.set(speed);
  }
  @Override
  public void periodic() {
    
  }
}
