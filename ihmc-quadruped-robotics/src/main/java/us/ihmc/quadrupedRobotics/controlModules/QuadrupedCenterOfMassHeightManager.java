package us.ihmc.quadrupedRobotics.controlModules;

import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.quadrupedRobotics.controller.force.QuadrupedForceControllerToolbox;
import us.ihmc.quadrupedRobotics.estimator.referenceFrames.QuadrupedReferenceFrames;
import us.ihmc.robotics.controllers.PDController;
import us.ihmc.robotics.controllers.PIDController;
import us.ihmc.robotics.controllers.pidGains.GainCoupling;
import us.ihmc.robotics.controllers.pidGains.implementations.DefaultPID3DGains;
import us.ihmc.robotics.controllers.pidGains.implementations.PIDGains;
import us.ihmc.robotics.controllers.pidGains.implementations.ParameterizedPID3DGains;
import us.ihmc.robotics.controllers.pidGains.implementations.ParameterizedPIDGains;
import us.ihmc.robotics.math.trajectories.waypoints.MultipleWaypointsPositionTrajectoryGenerator;
import us.ihmc.robotics.screwTheory.CenterOfMassJacobian;
import us.ihmc.robotics.screwTheory.MovingReferenceFrame;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;

public class QuadrupedCenterOfMassHeightManager
{
   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();

   private final YoVariableRegistry registry = new YoVariableRegistry(getClass().getSimpleName());

   private final YoDouble robotTimestamp;

   private final YoBoolean controlBodyHeight = new YoBoolean("controlBodyHeight", registry);

   private final MovingReferenceFrame bodyFrame;
   private final ReferenceFrame centerOfMassFrame;

   private final CenterOfMassJacobian centerOfMassJacobian;

   private final ReferenceFrame supportFrame;
   private final MultipleWaypointsPositionTrajectoryGenerator centerOfMassHeightTrajectory;

   private final PIDController linearMomentumZPDController;

   private final FramePoint3D currentPosition = new FramePoint3D();
   private final FrameVector3D currentVelocity = new FrameVector3D();

   private final FramePoint3D desiredPosition = new FramePoint3D();
   private final FrameVector3D desiredVelocity = new FrameVector3D();
   private final FrameVector3D desiredAcceleration = new FrameVector3D();

   private final ParameterizedPIDGains comPositionGainsParameter;

   private final YoDouble currentHeightInWorld;
   private final YoDouble desiredHeightInWorld;
   private final YoDouble desiredVelocityInWorld;
   private final YoDouble currentVelocityInWorld;

   private final double controlDT;

   public QuadrupedCenterOfMassHeightManager(QuadrupedForceControllerToolbox controllerToolbox, YoVariableRegistry parentRegistry)
   {
      this.robotTimestamp = controllerToolbox.getRuntimeEnvironment().getRobotTimestamp();
      this.controlDT = controllerToolbox.getRuntimeEnvironment().getControlDT();

      QuadrupedReferenceFrames referenceFrames = controllerToolbox.getReferenceFrames();
      supportFrame = referenceFrames.getCenterOfFeetZUpFrameAveragingLowestZHeightsAcrossEnds();
      bodyFrame = referenceFrames.getBodyFrame();
      centerOfMassFrame = referenceFrames.getCenterOfMassFrame();

      centerOfMassJacobian = controllerToolbox.getCenterOfMassJacobian();

      PIDGains defaultComPositionGains = new PIDGains();
      defaultComPositionGains.setKp(5000.0);
      defaultComPositionGains.setKd(750.0);
      comPositionGainsParameter = new ParameterizedPIDGains("_comHeight", defaultComPositionGains, registry);

      linearMomentumZPDController = new PIDController("linearMomentumZPDController", registry);

      centerOfMassHeightTrajectory = new MultipleWaypointsPositionTrajectoryGenerator("centerOfMassHeight", supportFrame, registry);

      currentHeightInWorld = new YoDouble("currentHeightInWorld", registry);
      currentVelocityInWorld = new YoDouble("currentVelocityInWorld", registry);
      desiredHeightInWorld = new YoDouble("desiredHeightInWorld", registry);
      desiredVelocityInWorld = new YoDouble("desiredVelocityInWorld", registry);

      controlBodyHeight.set(true);

      parentRegistry.addChild(registry);
   }

   public void handleCenterOfMassHeightCommand()
   {

   }

   public double computeDesiredCenterOfMassHeightAcceleration()
   {
      centerOfMassHeightTrajectory.compute(robotTimestamp.getDoubleValue());
      centerOfMassHeightTrajectory.getLinearData(desiredPosition, desiredVelocity, desiredAcceleration);

      desiredPosition.changeFrame(worldFrame);
      desiredVelocity.changeFrame(worldFrame);

      if (controlBodyHeight.getBooleanValue())
      {
         currentPosition.setToZero(bodyFrame);
         bodyFrame.getTwistOfFrame().getLinearVelocityOfPointFixedInBodyFrame(currentVelocity, currentPosition);
      }
      else
      {
         currentPosition.setToZero(centerOfMassFrame);
         centerOfMassJacobian.getCenterOfMassVelocity(currentVelocity);
      }

      currentPosition.changeFrame(worldFrame);
      currentVelocity.changeFrame(worldFrame);

      currentHeightInWorld.set(currentPosition.getZ());
      currentVelocityInWorld.set(currentVelocity.getZ());
      desiredHeightInWorld.set(desiredPosition.getZ());
      desiredVelocityInWorld.set(desiredVelocity.getZ());

      linearMomentumZPDController.setGains(comPositionGainsParameter);
      return linearMomentumZPDController.compute(currentPosition.getZ(), desiredPosition.getZ(), currentVelocity.getZ(), desiredVelocity.getZ(), controlDT);
   }

}
