package us.ihmc.atlas.roughTerrainWalking;

import org.junit.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.avatar.roughTerrainWalking.AvatarSwingWithWaypointsTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

@Tag("fast")
@Tag("video")
public class AtlasSwingWithWaypointsTest extends AvatarSwingWithWaypointsTest
{
   @Override
   @Test(timeout = 690000)
   public void testRegularSwingWithWaypoints() throws SimulationExceededMaximumTimeException
   {
      super.testRegularSwingWithWaypoints();
   }

   @Override
   @Test(timeout = 170000)
   public void testSwingWithWaypointsRotated() throws SimulationExceededMaximumTimeException
   {
      super.testSwingWithWaypointsRotated();
   }

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

}
