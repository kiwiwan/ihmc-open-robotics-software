package us.ihmc.atlas.controllerAPI;

import org.junit.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.controllerAPI.EndToEndWholeBodyTrajectoryMessageTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
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
   @Test(timeout = 84000)
   public void testIssue47BadChestTrajectoryMessage() throws Exception
   {
      super.testIssue47BadChestTrajectoryMessage();
   }

   @Override
   @Test(timeout = 79000)
   public void testIssue47BadPelvisTrajectoryMessage() throws Exception
   {
      super.testIssue47BadPelvisTrajectoryMessage();
   }

   @Override
   @Test(timeout = 180000)
   public void testSingleWaypoint() throws Exception
   {
      super.testSingleWaypoint();
   }

   @Override
   @Test(timeout = 180000)
   public void testSingleWaypointUsingMessageOfMessages() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessages();
   }

   @Override
   @Test(timeout = 320000)
   public void testSingleWaypointUsingMessageOfMessagesWithDelays() throws Exception
   {
      super.testSingleWaypointUsingMessageOfMessagesWithDelays();
   }
}
