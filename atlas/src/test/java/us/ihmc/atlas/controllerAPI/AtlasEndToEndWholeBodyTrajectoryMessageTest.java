package us.ihmc.atlas.controllerAPI;

import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.controllerAPI.EndToEndWholeBodyTrajectoryMessageTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;

public class AtlasEndToEndWholeBodyTrajectoryMessageTest extends EndToEndWholeBodyTrajectoryMessageTest
{

   private final DRCRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false);

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

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 16.8, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testIssue47BadChestTrajectoryMessage() throws Exception
   {
      super.testIssue47BadChestTrajectoryMessage();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 15.8, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testIssue47BadPelvisTrajectoryMessage() throws Exception
   {
      super.testIssue47BadPelvisTrajectoryMessage();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 36.1)
   @Test
   public void testSingleWaypoint() throws Exception
   {
      super.testSingleWaypoint();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 36.4)
   @Test
   public void testSingleWaypointUsingMessageOfMessages() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessages();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 63.1, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testSingleWaypointUsingMessageOfMessagesWithDelays() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessagesWithDelays();
   }
}
