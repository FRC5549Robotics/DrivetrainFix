// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.DrivetrainSubsystem;
import com.pathplanner.lib.PathPlannerTrajectory;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Shintake;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ScoreBerry extends SequentialCommandGroup {
  /** Creates a new ScoreBerry. */
  DrivetrainSubsystem m_drivetrainSubsystem;
  Indexer m_indexer;
  Shintake m_shintake;
  PathPlannerTrajectory path;
  public ScoreBerry(DrivetrainSubsystem drivetrainSubsystem, Indexer indexer, Shintake shintake, PathPlannerTrajectory path) {
    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    m_drivetrainSubsystem = drivetrainSubsystem;
    m_indexer = indexer;
    m_shintake = shintake;
    this.path = path;
    addCommands(
      new InstantCommand(() -> {
            m_drivetrainSubsystem.resetOdometry(path.getInitialHolonomicPose());
         }),
      new InstantCommand(() -> {
        m_indexer.indexer_back();
      }),
      new ParallelCommandGroup(
        new InstantCommand(() -> {
        m_indexer.indexer_back();
      }),
        new InstantCommand(() -> {
          m_shintake.shintake_run();
        })
      ),
      new InstantCommand(() -> {
        m_shintake.shintake_run();
      }),
          m_drivetrainSubsystem.followTrajectoryCommand(path)
    );
  }
}
