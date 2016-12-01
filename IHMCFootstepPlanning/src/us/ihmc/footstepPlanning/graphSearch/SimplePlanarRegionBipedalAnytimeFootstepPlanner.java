package us.ihmc.footstepPlanning.graphSearch;

import java.util.concurrent.atomic.AtomicReference;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import us.ihmc.footstepPlanning.AnytimeFootstepPlanner;
import us.ihmc.footstepPlanning.FootstepPlan;
import us.ihmc.footstepPlanning.FootstepPlanningResult;
import us.ihmc.footstepPlanning.SimpleFootstep;
import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.geometry.FramePose;
import us.ihmc.robotics.geometry.PlanarRegionsList;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.tools.thread.ThreadTools;

public class SimplePlanarRegionBipedalAnytimeFootstepPlanner extends PlanarRegionBipedalFootstepPlanner implements AnytimeFootstepPlanner, Runnable
{
   private BipedalFootstepPlannerNode closestNodeToGoal = null;
   private final AtomicReference<FootstepPlan> bestPlanYet = new AtomicReference<>(null);
   private boolean stopRequested = false;
   private boolean isBestPlanYetOptimal = false;
   private boolean alreadySetPlanarRegions = false;
   private final AtomicReference<PlanarRegionsList> planarRegionsListReference = new AtomicReference<>(null);
   private boolean requestInitialize = false;

   public SimplePlanarRegionBipedalAnytimeFootstepPlanner(YoVariableRegistry parentRegistry)
   {
      super(parentRegistry);
   }

   /**
    * @return The FootstepPlan that ends the closest to the goal of all explored yet
    */
   @Override
   public FootstepPlan getBestPlanYet()
   {
      return bestPlanYet.get();
   }

   @Override
   public void setPlanarRegions(PlanarRegionsList planarRegionsList)
   {
//      if(alreadySetPlanarRegions)
//      {
//         return;
//      }
//      else
//      {
         planarRegionsListReference.set(planarRegionsList);
//         alreadySetPlanarRegions = true;
//      }
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      stack.clear();
      
      if (initialSide == null)
      {
         throw new RuntimeException("initialSide == null");
      }

      startNode = new BipedalFootstepPlannerNode(initialSide, initialFootPose);
      stack.push(startNode);
      closestNodeToGoal = null;
      mapToAllExploredNodes.clear();
   }

   @Override
   public void executingFootstep(SimpleFootstep footstep)
   {
      FramePose newStartPose = new FramePose();
      footstep.getSoleFramePose(newStartPose);
      RobotSide newInitialSide = footstep.getRobotSide();

      super.setInitialStanceFoot(newStartPose, newInitialSide);
      requestInitialize = true;
   }

   @Override
   public boolean isBestPlanYetOptimal()
   {
      return isBestPlanYetOptimal;
   }

   @Override
   public FootstepPlanningResult plan()
   {
      return plan(true);
   }
   
   public FootstepPlanningResult plan(boolean stopAndReturnWhenGoalIsFound)
   {
//      initialize();
      goalNode = null;
      footstepPlan = null;

      planarRegionPotentialNextStepCalculator.setStartNode(startNode);

//      while(planarRegionsListReference.get() == null)
//      {
//         ThreadTools.sleep(100);
//      }

      checkForNewPlanarRegionsList();

      while (!stopRequested)
      {
         if(requestInitialize)
         {
            initialize();
            requestInitialize = false;
         }

         checkForNewPlanarRegionsList();

         if(stack.isEmpty())
         {
            if (stopAndReturnWhenGoalIsFound)
            {
               notifyListenerSolutionWasNotFound();
               return FootstepPlanningResult.NO_PATH_EXISTS;
            }

            ThreadTools.sleep(100);
            continue;
         }
         
         BipedalFootstepPlannerNode nodeToExpand = stack.pop();

         boolean nearbyNodeAlreadyExists = checkIfNearbyNodeAlreadyExistsAndStoreIfNot(nodeToExpand);
         if (nearbyNodeAlreadyExists)
            continue;
         
         numberOfNodesExpanded.increment();
         notifyListenerNodeForExpansionWasAccepted(nodeToExpand);

         setNodesCostsAndRememberIfClosestYet(nodeToExpand);

         if (nodeToExpand.isAtGoal())
         {
            if ((nodeToExpand.getParentNode() != null) && (nodeToExpand.getParentNode().isAtGoal()))
            {
               goalNode = nodeToExpand;
               closestNodeToGoal = goalNode;

               notifyListenerSolutionWasFound(getPlan());

               if (stopAndReturnWhenGoalIsFound)
               {
                  return FootstepPlanningResult.OPTIMAL_SOLUTION;
               }
            }
         }

         expandChildrenAndAddNodes(stack, nodeToExpand);
      }

      notifyListenerSolutionWasNotFound();
      return FootstepPlanningResult.NO_PATH_EXISTS;
   }

   private void checkForNewPlanarRegionsList()
   {
      PlanarRegionsList planarRegionsList = planarRegionsListReference.getAndSet(null);
      if (planarRegionsList != null)
      {
         super.setPlanarRegions(planarRegionsList);
         initialize();
      }
   }

   private void setNodesCostsAndRememberIfClosestYet(BipedalFootstepPlannerNode nodeToSetCostOf)
   {
      Point3d currentPosition = nodeToSetCostOf.getSolePosition();
      Point3d goalPosition = planarRegionPotentialNextStepCalculator.getGoalPosition(nodeToSetCostOf.getRobotSide());
      Vector3d currentToGoalVector = new Vector3d();
      currentToGoalVector.sub(goalPosition, currentPosition);

      double costFromParent = 1.0;
      double costToHereFromStart;
      if (nodeToSetCostOf.getParentNode() == null)
      {
         costToHereFromStart = 0.0;
      }
      else
      {
         costToHereFromStart = nodeToSetCostOf.getParentNode().getCostToHereFromStart() + costFromParent;
      }
      nodeToSetCostOf.setCostFromParent(costFromParent);
      nodeToSetCostOf.setCostToHereFromStart(costToHereFromStart);

      double euclideanDistanceToGoal = currentToGoalVector.length();
      nodeToSetCostOf.setEstimatedCostToGoal(euclideanDistanceToGoal);

      if (closestNodeToGoal == null || euclideanDistanceToGoal < closestNodeToGoal.getEstimatedCostToGoal())
      {
         System.out.println(euclideanDistanceToGoal);
         System.out.println(euclideanDistanceToGoal);
         System.out.println(euclideanDistanceToGoal);
         closestNodeToGoal = nodeToSetCostOf;
         FootstepPlan newBestPlan = new FootstepPlan(closestNodeToGoal);
         bestPlanYet.set(newBestPlan);
      }
   }

   public void requestStop()
   {
      stopRequested = true;
   }

   @Override
   public void run()
   {
      plan(false);
   }
}
