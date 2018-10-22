package us.ihmc.atlas.ObstacleCourseTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.avatar.obstacleCourseTests.DRCObstacleCoursePlatformTest;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.UnreasonableAccelerationException;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasObstacleCoursePlatformTest extends DRCObstacleCoursePlatformTest
{

   @Override
   public DRCRobotModel getRobotModel()
   {
      return new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false);
   }

   @Override
   public String getSimpleRobotName()
   {
      return BambooTools.getSimpleRobotNameFor(BambooTools.SimpleRobotNameKeys.ATLAS);
   }

   @Override
   // "This test is flaky. Sometimes it works, sometimes it doesn't due to threading of the various globalDataProducer and communicators. We need to be able to shut those off or make them not screw up the robot run.")
   @ContinuousIntegrationTest(estimatedDuration = 48.6, categoriesOverride = IntegrationCategory.FLAKY)
   @Test
   public void testRunsTheSameWayTwiceJustStanding() throws UnreasonableAccelerationException, SimulationExceededMaximumTimeException
   {
      super.testRunsTheSameWayTwiceJustStanding();
   }

   @Override
   @Test
   public void testSidestepOverSmallPlatform() throws SimulationExceededMaximumTimeException
   {
      super.testSidestepOverSmallPlatform();
   }

   @Override
   @Test
   public void testSidestepOverSmallWall() throws SimulationExceededMaximumTimeException
   {
      super.testSidestepOverSmallWall();
   }

   @Override
   @Test
   public void testWalkingOffOfMediumPlatform() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOffOfMediumPlatform();
   }

   @Override
   @Tag("slow")
   @Test
   public void testWalkingOffOfMediumPlatformSlowSteps() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOffOfMediumPlatformSlowSteps();
   }

   @Override
   @Tag("slow")
   @Test
   public void testWalkingOntoMediumPlatformToesTouching() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOntoMediumPlatformToesTouching();
   }

   @Override
   @Test
   public void testWalkingOverSmallPlatform() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOverSmallPlatform();
   }

   @Override
   @Tag("slow")
   @Test
   public void testWalkingOverSmallPlatformQuickly() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOverSmallPlatformQuickly();
   }

}
