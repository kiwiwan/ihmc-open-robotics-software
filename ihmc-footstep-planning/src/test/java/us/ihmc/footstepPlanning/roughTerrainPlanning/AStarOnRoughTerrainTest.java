package us.ihmc.footstepPlanning.roughTerrainPlanning;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.euclid.geometry.ConvexPolygon2D;
import us.ihmc.footstepPlanning.FootstepPlanner;
import us.ihmc.footstepPlanning.graphSearch.aStar.FootstepNodeVisualization;
import us.ihmc.footstepPlanning.graphSearch.nodeExpansion.ParameterBasedNodeExpansion;
import us.ihmc.footstepPlanning.graphSearch.planners.AStarFootstepPlanner;
import us.ihmc.footstepPlanning.tools.PlannerTools;
import us.ihmc.robotics.robotSide.SideDependentList;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

@ContinuousIntegrationPlan(categories = IntegrationCategory.FAST)
public class AStarOnRoughTerrainTest extends FootstepPlannerOnRoughTerrainTest
{
   private AStarFootstepPlanner planner;
   private FootstepNodeVisualization visualization = null;

   private static boolean keepUp = false;



   @Override
   public boolean assertPlannerReturnedResult()
   {
      return true;
   }

   @Override
   @ContinuousIntegrationTest(estimatedDuration = 2.5)
   @Test
   public void testDownCorridor()
   {
      setCheckForBodyBoxCollision(true);
      super.testDownCorridor();
   }

   @Override
   @Disabled
   @Test
   public void testPartialGaps()
   {
      super.testPartialGaps();
   }

   @Override
   @Disabled
   @Test
   public void testSpiralStaircase()
   {
      super.testSpiralStaircase();
   }

   @AfterEach
   public void destroyPlanner()
   {
      planner = null;

      if (visualize)
         ThreadTools.sleepForever();

   }

   @Override
   public FootstepPlanner getPlanner()
   {
      return planner;
   }

   @Override
   public boolean visualize()
   {
      return visualize;
   }

   @Override
   public boolean keepUp()
   {
      return keepUp;
   }

   @Override
   public void setupInternal()
   {
      SideDependentList<ConvexPolygon2D> footPolygons = PlannerTools.createDefaultFootPolygons();
      ParameterBasedNodeExpansion expansion = new ParameterBasedNodeExpansion(getPlannerParameters());
      planner = AStarFootstepPlanner
            .createRoughTerrainPlanner(getPlannerParameters(), visualization, footPolygons, expansion, new YoVariableRegistry("TestRegistry"));
   }

   @Override
   public void destroyInternal()
   {
      planner = null;
   }
}
