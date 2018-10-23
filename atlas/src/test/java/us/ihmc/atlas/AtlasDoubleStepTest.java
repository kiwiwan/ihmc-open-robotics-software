package us.ihmc.atlas;

import org.junit.Test;
import us.ihmc.avatar.AvatarDoubleStepTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import org.junit.jupiter.api.Tag;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class AtlasDoubleStepTest extends AvatarDoubleStepTest
{
   private final RobotTarget target = RobotTarget.SCS;

   private final AtlasRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, target, false);

   @Override
   public DRCRobotModel getRobotModel()
   {
      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return robotModel.getSimpleRobotName();
   }

   @ContinuousIntegrationTest(estimatedDuration = 76.4)
   @Test(timeout = 380000)
   @Override
   public void testTwoStepsInARowSameSide() throws SimulationExceededMaximumTimeException
   {
      super.testTwoStepsInARowSameSide();
   }

   @ContinuousIntegrationTest(estimatedDuration = 76.4, categoriesOverride = IntegrationCategory.SLOW)
   @Test(timeout = 380000)
   @Override
   public void testTwoStepsInARowSameSideAfterFirstSep() throws SimulationExceededMaximumTimeException
   {
      super.testTwoStepsInARowSameSideAfterFirstSep();
   }

   @ContinuousIntegrationTest(estimatedDuration = 76.4, categoriesOverride = IntegrationCategory.SLOW)
   @Test(timeout = 380000)
   @Override
   public void testTwoStepsInARowLongTransferSameSide() throws SimulationExceededMaximumTimeException
   {
      super.testTwoStepsInARowLongTransferSameSide();
   }

   @ContinuousIntegrationTest(estimatedDuration = 76.4, categoriesOverride = IntegrationCategory.SLOW)
   @Test(timeout = 380000)
   @Override
   public void testTwoStepsStandingInBetween() throws SimulationExceededMaximumTimeException
   {
      super.testTwoStepsStandingInBetween();
   }
}
