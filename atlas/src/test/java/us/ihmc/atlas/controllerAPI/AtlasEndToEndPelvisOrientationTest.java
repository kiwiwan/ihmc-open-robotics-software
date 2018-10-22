package us.ihmc.atlas.controllerAPI;

import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.controllerAPI.EndToEndPelvisOrientationTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasEndToEndPelvisOrientationTest extends EndToEndPelvisOrientationTest
{
   private DRCRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false);

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 26.9)
   @Test
   public void testGoHome() throws SimulationExceededMaximumTimeException
   {
      super.testGoHome();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 25.5)
   @Test
   public void testSingleTrajectoryPoint() throws SimulationExceededMaximumTimeException
   {
      super.testSingleTrajectoryPoint();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 42.5)
   @Test
   public void testWalking() throws SimulationExceededMaximumTimeException
   {
      super.testWalking();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 40.8, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testWalkingAfterTrajectory() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingAfterTrajectory();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 33.4, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testMultipleTrajectoryPoints() throws SimulationExceededMaximumTimeException
   {
      super.testMultipleTrajectoryPoints();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 34.9, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testWalkingWithUserControl() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingWithUserControl();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 29.3, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testCustomControlFrame() throws SimulationExceededMaximumTimeException
   {
      super.testCustomControlFrame();
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
