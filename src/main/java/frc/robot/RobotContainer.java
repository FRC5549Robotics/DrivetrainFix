// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants;
import frc.robot.commands.DriveCommand;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj.XboxController;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;
import frc.robot.subsystems.DrivetrainSubsystem;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Shintake;


/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  private final XboxController m_controller = new XboxController(0);
  private final XboxController m_controller2 = new XboxController(1);
  private final DrivetrainSubsystem m_drive = new DrivetrainSubsystem();

  public Indexer m_indexer = new Indexer(); 
  public Shintake m_shintake = new Shintake(); 

  JoystickButton indexInButton = new JoystickButton(m_controller2, Constants.INDEXER_BUTTON);
  JoystickButton resetNavXButton = new JoystickButton(m_controller, 4);
  Trigger shootTrigger = new JoystickButton(m_controller2, Constants.SHOOT_TRIGGER);
  JoystickButton intakeButton = new JoystickButton(m_controller2, Constants.INTAKE_BUTTON);
  JoystickButton pnuematics = new JoystickButton(m_controller2, Constants.PNEUMATIC_BUTTON);
  JoystickButton shootButton = new JoystickButton(m_controller2, Constants.SHOOT_BUTTON);
  JoystickButton indexOutButton = new JoystickButton(m_controller2, Constants.INDEX_OUT);


  public RobotContainer() {

    m_drive.setDefaultCommand(new DriveCommand(m_drive, m_controller));
    // Configure the trigger bindings
    configureBindings();
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    indexInButton.onTrue(new InstantCommand(m_indexer::indexer_run));
    indexInButton.onFalse(new InstantCommand(m_indexer::indexer_stop));
    resetNavXButton.onTrue(new InstantCommand(m_drive::zeroGyroscope));
    intakeButton.onTrue(new InstantCommand(m_shintake::shintake_back));
    intakeButton.onFalse(new InstantCommand(m_shintake::shintake_stop));
    pnuematics.onFalse(new InstantCommand(m_shintake::retract));
    pnuematics.onTrue(new InstantCommand(m_shintake::extend));
    shootButton.onTrue(new InstantCommand(m_shintake::shintake_run));
    shootButton.onFalse(new InstantCommand(m_shintake::shintake_stop));
    indexOutButton.onTrue(new InstantCommand(m_indexer::indexer_back));
    indexOutButton.onFalse(new InstantCommand(m_indexer::indexer_stop));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return null;
  }
}
