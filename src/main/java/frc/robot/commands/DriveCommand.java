package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.subsystems.DrivetrainSubsystem;
import java.lang.Math;

public class DriveCommand extends CommandBase {

    private double xDot;
    private double yDot;
    private double thetaDot;
    private boolean fieldRelative;
    private ChassisSpeeds chassisSpeeds, chassisPercent;
    private XboxController m_controller;

    // The subsystem the command runs on
    public final DrivetrainSubsystem drivetrain;

    public DriveCommand(DrivetrainSubsystem subsystem, XboxController controller){
        drivetrain = subsystem;
        m_controller = controller;
        addRequirements(drivetrain);
    }
 
    @Override
    public void initialize() {
    }
            
    @Override
    public void execute() {
        xDot = m_controller.getLeftY() * Constants.kMaxTranslationalVelocity;
        yDot = m_controller.getLeftX() * Constants.kMaxTranslationalVelocity;
        thetaDot = m_controller.getRightX() * Constants.kMaxRotationalVelocity;
        fieldRelative = true;
        if(Math.abs(xDot)<0.1*Constants.kMaxTranslationalVelocity){
          xDot = 0;
        }
        if(Math.abs(yDot)<0.1*Constants.kMaxTranslationalVelocity){
          yDot = 0;
        }
        if(Math.abs(thetaDot)<0.1*Constants.kMaxRotationalVelocity){
          thetaDot = 0;
        }

        chassisSpeeds = new ChassisSpeeds(xDot, yDot, thetaDot);
        
        drivetrain.drive(chassisSpeeds, true);
    }
}