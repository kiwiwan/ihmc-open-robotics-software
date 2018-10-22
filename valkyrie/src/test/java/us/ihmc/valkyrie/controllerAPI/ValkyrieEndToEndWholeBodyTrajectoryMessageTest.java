package us.ihmc.valkyrie.controllerAPI;

import org.junit.jupiter.api.Test;

import us.ihmc.avatar.controllerAPI.EndToEndWholeBodyTrajectoryMessageTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.valkyrie.ValkyrieRobotModel;

public class ValkyrieEndToEndWholeBodyTrajectoryMessageTest extends EndToEndWholeBodyTrajectoryMessageTest
{
   private final ValkyrieRobotModel robotModel = new ValkyrieRobotModel(RobotTarget.SCS, false);

   @Override
   public DRCRobotModel getRobotModel()
   {
      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return BambooTools.getSimpleRobotNameFor(BambooTools.SimpleRobotNameKeys.VALKYRIE);
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 22.1, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testIssue47BadChestTrajectoryMessage() throws Exception
   {
      super.testIssue47BadChestTrajectoryMessage();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 17.1, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testIssue47BadPelvisTrajectoryMessage() throws Exception
   {
      super.testIssue47BadPelvisTrajectoryMessage();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 38.2)
   @Test
   public void testSingleWaypoint() throws Exception
   {
      super.testSingleWaypoint();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 38.1)
   @Test
   public void testSingleWaypointUsingMessageOfMessages() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessages();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 69.3, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testSingleWaypointUsingMessageOfMessagesWithDelays() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessagesWithDelays();
   }
}
