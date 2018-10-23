package us.ihmc.atlas.behaviorTests;

import java.io.IOException;

import org.junit.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.behaviorTests.AvatarWalkOverTerrainBehaviorTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.ControllerFailureException;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner;

@Disabled
@ContinuousIntegrationAnnotations.ContinuousIntegrationPlan(categories = {IntegrationCategory.EXCLUDE})
public class AtlasWalkOverTerrainBehaviorTest extends AvatarWalkOverTerrainBehaviorTest
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
   @Disabled
   @ContinuousIntegrationTest(estimatedDuration = 63.6, categoriesOverride = IntegrationCategory.EXCLUDE)
   @Test(timeout = 400000)
   public void testWalkOverCinderBlocks() throws IOException, BlockingSimulationRunner.SimulationExceededMaximumTimeException, ControllerFailureException
   {
      super.testWalkOverCinderBlocks();
   }
}
