package us.ihmc.valkyrie.obstacleCourse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.avatar.obstacleCourseTests.DRCObstacleCourseTrialsTerrainTest;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.valkyrie.ValkyrieRobotModel;
import us.ihmc.wholeBodyController.AdditionalSimulationContactPoints;
import us.ihmc.wholeBodyController.FootContactPoints;

@ContinuousIntegrationPlan(categories = {IntegrationCategory.FAST, IntegrationCategory.VIDEO})
public class ValkyrieObstacleCourseTrialsTerrainTest extends DRCObstacleCourseTrialsTerrainTest
{
   private final AdditionalSimulationContactPoints<RobotSide> footContactPoints = new AdditionalSimulationContactPoints<RobotSide>(RobotSide.values, 3, 4, true,
                                                                                                                                   true);
   private final ValkyrieRobotModel robotModel = new ValkyrieRobotModel(RobotTarget.SCS, false, footContactPoints);

   @Override
   public DRCRobotModel getRobotModel()
   {
      return robotModel;
   }

   @Override
   protected DRCRobotModel getRobotModelWithAdditionalFootContactPoints()
   {
      FootContactPoints<RobotSide> simulationContactPoints = new AdditionalSimulationContactPoints<RobotSide>(RobotSide.values, 5, 4, true, true);
      ValkyrieRobotModel robotModel = new ValkyrieRobotModel(RobotTarget.SCS, false, simulationContactPoints);
      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return BambooTools.getSimpleRobotNameFor(BambooTools.SimpleRobotNameKeys.VALKYRIE);
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 81.4, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testTrialsTerrainZigzagHurdlesScript() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainZigzagHurdlesScript();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 125.6)
   @Test
   public void testWalkingOntoAndOverSlopesSideways() throws SimulationExceededMaximumTimeException
   {
      super.testWalkingOntoAndOverSlopesSideways();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 176.1)
   @Test
   public void testTrialsTerrainSlopeScriptRandomFootSlip() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainSlopeScriptRandomFootSlip();
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 177.6, categoriesOverride = IntegrationCategory.SLOW)
   @Test
   public void testTrialsTerrainSlopeScript() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainSlopeScript();
   }

   /**
    * This test doesn't make any sense. We have plenty of tests evaluating robustness to foot slip,
    * and that one adds random foot slip while stepping narrow cinder blocks. The test will often
    * fail because the foot contact points are on the edge of a cinder block.
    */
   @Override
   @Disabled
   @Test
   public void testTrialsTerrainZigzagHurdlesScriptRandomFootSlip() throws SimulationExceededMaximumTimeException
   {
      super.testTrialsTerrainZigzagHurdlesScriptRandomFootSlip();
   }
}
