package us.ihmc.atlas.controllerAPI;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.controllerAPI.EndToEndChestTrajectoryMessageTest;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;

public class AtlasEndToEndChestTrajectoryMessageTest extends EndToEndChestTrajectoryMessageTest
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
   @Test
   public void testLookingLeftAndRight() throws Exception
   {
      super.testLookingLeftAndRight();
   }

   @Override
   @Tag("slow")
   @Test
   public void testLookingLeftAndRightInVariousTrajectoryFrames() throws Exception
   {
      super.testLookingLeftAndRightInVariousTrajectoryFrames();
   }

   @Override
   @Tag("slow")
   @Test
   public void testMessageWithALotOfTrajectoryPoints() throws Exception
   {
      super.testMessageWithALotOfTrajectoryPoints();
   }

   @Override
   @Tag("slow")
   @Test
   public void testMessageWithALotOfTrajectoryPointsExpressedInPelvisZUp() throws Exception
   {
      super.testMessageWithALotOfTrajectoryPointsExpressedInPelvisZUp();
   }

   @Override
   @Test
   public void testMultipleTrajectoryPoints() throws Exception
   {
      super.testMultipleTrajectoryPoints();
   }

   @Override
   @Test
   public void testQueuedMessages() throws Exception
   {
      super.testQueuedMessages();
   }

   @Override
   @Tag("slow")
   @Test
   public void testQueueStoppedWithOverrideMessage() throws Exception
   {
      super.testQueueStoppedWithOverrideMessage();
   }

   @Override
   @Tag("slow")
   @Test
   public void testQueueWithUsingDifferentTrajectoryFrameWithoutOverride() throws Exception
   {
      super.testQueueWithUsingDifferentTrajectoryFrameWithoutOverride();
   }

   @Override
   @Tag("slow")
   @Test
   public void testQueueWithWrongPreviousId() throws Exception
   {
      super.testQueueWithWrongPreviousId();
   }

   @Override
   @Tag("slow")
   @Test
   public void testSelectionMatrixDisableRandomAxisWithSingleTrajectoryPoint() throws Exception
   {
      super.testSelectionMatrixDisableRandomAxisWithSingleTrajectoryPoint();
   }

   @Override
   @Tag("slow")
   @Test
   public void testSelectionMatrixWithAllAxisOffUsingSingleTrajectoryPoint() throws Exception
   {
      super.testSelectionMatrixWithAllAxisOffUsingSingleTrajectoryPoint();
   }

   @Override
   @Tag("slow")
   @Test
   public void testSettingWeightMatrixUsingSingleTrajectoryPoint() throws Exception
   {
      super.testSettingWeightMatrixUsingSingleTrajectoryPoint();
   }

   @Override
   @Test
   public void testSingleTrajectoryPoint() throws Exception
   {
      super.testSingleTrajectoryPoint();
   }

   @Override
   @Test
   public void testStopAllTrajectory() throws Exception
   {
      super.testStopAllTrajectory();
   }
}
