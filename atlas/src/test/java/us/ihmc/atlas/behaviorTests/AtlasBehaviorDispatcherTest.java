package us.ihmc.atlas.behaviorTests;

import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.behaviorTests.HumanoidBehaviorDispatcherTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasBehaviorDispatcherTest extends HumanoidBehaviorDispatcherTest
{
   private final AtlasRobotModel robotModel;

   public AtlasBehaviorDispatcherTest()
   {
      robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_DUAL_ROBOTIQ, RobotTarget.SCS, false);
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

   @Disabled("Hangs forever, with Exception in thread \"IntraProcessDomainImpl-thread-5938\" java.lang.NullPointerException")
   @Override
   @Test // timeout = 320000
   public void testDispatchPelvisPoseBehavior() throws SimulationExceededMaximumTimeException
   {
      super.testDispatchPelvisPoseBehavior();
   }

   @Override
   @Disabled
   @Test // timeout = 1200000
   public void testDispatchWalkToLocationBehavior() throws SimulationExceededMaximumTimeException
   {
      super.testDispatchWalkToLocationBehavior();
   }

   @Override
   @Test // timeout = 290000
   public void testDispatchWalkToLocationBehaviorAndStop() throws SimulationExceededMaximumTimeException
   {
      super.testDispatchWalkToLocationBehaviorAndStop();
   }

   @Override
   @Test // timeout = 510000
   public void testDispatchWalkToLocationBehaviorPauseAndResume() throws SimulationExceededMaximumTimeException
   {
      super.testDispatchWalkToLocationBehaviorPauseAndResume();
   }
}
