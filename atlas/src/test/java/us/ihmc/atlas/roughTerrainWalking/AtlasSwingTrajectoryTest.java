package us.ihmc.atlas.roughTerrainWalking;

import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.avatar.roughTerrainWalking.DRCSwingTrajectoryTest;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasSwingTrajectoryTest extends DRCSwingTrajectoryTest
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
   @ContinuousIntegrationTest(estimatedDuration = 130.4)
   @Test
   public void testMultipleHeightFootsteps() throws SimulationExceededMaximumTimeException
   {
      super.testMultipleHeightFootsteps();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 47.8)
   @Test
   public void testNegativeSwingHeight() throws SimulationExceededMaximumTimeException
   {
      super.testNegativeSwingHeight();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 55.8, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testReallyHighFootstep() throws SimulationExceededMaximumTimeException
   {
      super.testReallyHighFootstep();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 126.9, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testSelfCollisionAvoidance() throws SimulationExceededMaximumTimeException
   {
      super.testSelfCollisionAvoidance();
   }
}
