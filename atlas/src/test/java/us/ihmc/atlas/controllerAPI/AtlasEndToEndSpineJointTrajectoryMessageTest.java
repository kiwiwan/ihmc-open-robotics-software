package us.ihmc.atlas.controllerAPI;

import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.controllerAPI.EndToEndSpineJointTrajectoryMessageTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasEndToEndSpineJointTrajectoryMessageTest extends EndToEndSpineJointTrajectoryMessageTest
{
   private final DRCRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false);

   @Override
   @Test // timeout = 110000
   public void testSingleWaypoint() throws SimulationExceededMaximumTimeException
   {
      super.testSingleWaypoint();
   }

   @Override
   @Test // timeout = 170000
   public void testSwitchingBetweenControlModes() throws SimulationExceededMaximumTimeException
   {
      super.testSwitchingBetweenControlModes();
   }

   @Override
   @Test // timeout = 330000
   public void testDesiredsAreContinuous() throws SimulationExceededMaximumTimeException
   {
      super.testDesiredsAreContinuous();
   }

   @Override
   @Test // timeout = 330000
   public void testMultipleWaypoints() throws SimulationExceededMaximumTimeException
   {
      super.testMultipleWaypoints();
   }

   @Override
   @Test // timeout = 340000
   public void testLongMessage() throws SimulationExceededMaximumTimeException
   {
      super.testLongMessage();
   }

   @Override
   @Test // timeout = 190000
   public void testMessageQueuing() throws SimulationExceededMaximumTimeException
   {
      super.testMessageQueuing();
   }

   @Override
   @Test // timeout = 250000
   public void testMessageWithDifferentTrajectoryLengthsPerJoint() throws SimulationExceededMaximumTimeException
   {
      super.testMessageWithDifferentTrajectoryLengthsPerJoint();
   }

   @Override
   public DRCRobotModel getRobotModel()
   {
      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return BambooTools.getSimpleRobotNameFor(BambooTools.SimpleRobotNameKeys.ATLAS);
   }

}
