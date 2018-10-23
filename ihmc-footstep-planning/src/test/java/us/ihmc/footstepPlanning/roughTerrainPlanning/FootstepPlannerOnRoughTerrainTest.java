package us.ihmc.footstepPlanning.roughTerrainPlanning;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.thread.ThreadTools;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.continuousIntegration.ContinuousIntegrationTools;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.footstepPlanning.FootstepPlan;
import us.ihmc.footstepPlanning.communication.FootstepPlannerMessagerAPI;
import us.ihmc.footstepPlanning.graphSearch.parameters.DefaultFootstepPlanningParameters;
import us.ihmc.footstepPlanning.graphSearch.parameters.FootstepPlannerParameters;
import us.ihmc.footstepPlanning.testTools.PlannerTestEnvironments;
import us.ihmc.footstepPlanning.testTools.PlanningTest;
import us.ihmc.footstepPlanning.tools.PlannerTools;
import us.ihmc.footstepPlanning.ui.ApplicationRunner;
import us.ihmc.footstepPlanning.ui.FootstepPlannerUI;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.javaFXToolkit.messager.SharedMemoryJavaFXMessager;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static us.ihmc.footstepPlanning.communication.FootstepPlannerMessagerAPI.ComputePathTopic;
import static us.ihmc.footstepPlanning.communication.FootstepPlannerMessagerAPI.PlannerParametersTopic;
import static us.ihmc.footstepPlanning.testTools.PlannerTestEnvironments.*;

public abstract class FootstepPlannerOnRoughTerrainTest implements PlanningTest
{
   protected static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();
   private FootstepPlannerUI ui;

   protected static boolean visualize = false;
   protected JavaFXMessager messager;

   private boolean checkForBodyBoxCollision = false;

   @BeforeEach
   public void setup()
   {
      visualize = visualize && !ContinuousIntegrationTools.isRunningOnContinuousIntegrationServer();
      checkForBodyBoxCollision = false;

      if (visualize)
      {
         ApplicationRunner.runApplication(new Application()
         {
            @Override
            public void start(Stage stage) throws Exception
            {
               messager = new SharedMemoryJavaFXMessager(FootstepPlannerMessagerAPI.API);
               messager.startMessager();

               ui = FootstepPlannerUI.createMessagerUI(stage, messager);
               ui.show();
            }

            @Override
            public void stop()
            {
               ui.stop();
               Platform.exit();
            }
         });

         double maxStartUpTime = 5.0;
         double currentTime = 0.0;
         long sleepDuration = 100;
         while (ui == null)
         {
            if (currentTime > maxStartUpTime)
               throw new RuntimeException("Failed to start UI");

            currentTime += Conversions.millisecondsToSeconds(sleepDuration);
            ThreadTools.sleep(sleepDuration);
         }
      }

      setupInternal();
   }

   @AfterEach
   public void tearDown()
   {
      ReferenceFrameTools.clearWorldFrameTree();

      if (ui != null)
      {
         ui.stop();
         Platform.exit();
      }

      destroyInternal();
   }

   public void setCheckForBodyBoxCollision(boolean checkForBodyBoxCollision)
   {
      this.checkForBodyBoxCollision = checkForBodyBoxCollision;
   }

   protected AtomicReference<FootstepPlannerParameters> parametersReference;

   protected abstract void setupInternal();

   protected abstract void destroyInternal();

   public abstract boolean assertPlannerReturnedResult();

   @Test // timeout = 50000
   public void testOnStaircase()
   {
      // run the test
      runTestAndAssert(getTestData(staircase));
   }

   @Test // timeout = 30000
   public void testWithWall()
   {
      // run the test
      runTestAndAssert(getTestData(wall));
   }

   public void testDownCorridor()
   {
      runTestAndAssert(getTestData(corridor));
   }

   @Test // timeout = 30000
   public void testOverCinderBlockField()
   {
      // run the test
      runTestAndAssert(getTestData(overCinderBlockField));
   }

   @Test // timeout = 3000000
   public void testSteppingStones()
   {
      // run the test
      runTestAndAssert(getTestData(steppingStones));
   }

   @Test // timeout = 30000
   public void testStepUpsAndDownsScoringDifficult()
   {
      runTestAndAssert(getTestData(stepUpsAndDownsScoringDifficult));
   }

   @Test // timeout = 50000
   public void testStepAfterPitchedUp()
   {
      runTestAndAssert(getTestData(stepAfterPitchUp));
   }

   @Test // timeout = 50000
   public void testStepAfterPitchedDown()
   {
      runTestAndAssert(getTestData(stepAfterPitchDown));
   }

   @Test // timeout = 50000
   public void testCompareStepBeforeGap()
   {
      runTestAndAssert(getTestData(compareStepBeforeGap));
   }

   @Test // timeout = 50000
   public void testSimpleStepOnBox()
   {
      runTestAndAssert(getTestData(simpleStepOnBox));
   }

   @Test // timeout = 50000
   public void testSimpleStepOnBoxTwo()
   {
      runTestAndAssert(getTestData(simpleStepOnBoxTwo));
   }

   @Test // timeout = 30000
   public void testRandomEnvironment()
   {
      runTestAndAssert(getTestData(random));
   }

   @Test // timeout = 30000
   public void testSimpleGaps()
   {
      runTestAndAssert(getTestData(simpleGaps));
   }

   @Test // timeout = 30000
   public void testPartialGaps()
   {
      runTestAndAssert(getTestData(partialGaps));
   }

   @Test // timeout = 30000
   public void testWalkingAroundBox()
   {
      // run the test
      runTestAndAssert(getTestData(box));
   }

   @Test // timeout = 30000
   public void testSpiralStaircase()
   {
      // run the test
      runTestAndAssert(getTestData(spiralStaircase));
   }

   @Test // timeout = 30000
   public void testWalkingAroundHole()
   {
      // run the test
      runTestAndAssert(getTestData(hole));
   }

   protected FootstepPlannerParameters getDefaultPlannerParameters()
   {
      return new DefaultFootstepPlanningParameters()

      {
         @Override
         public boolean checkForBodyBoxCollisions()
         {
            return checkForBodyBoxCollision;
         }
      };
   }

   protected FootstepPlannerParameters getPlannerParameters()
   {
      if (parametersReference == null)
         return getDefaultPlannerParameters();

      return parametersReference.get();
   }

   private void runTestAndAssert(PlannerTestEnvironments.PlannerTestData testData)
   {
      if (messager != null && visualize())
         submitInfoToUI(testData);

      FootstepPlan footstepPlan = PlannerTools
            .runPlanner(getPlanner(), testData.getStartPose(), testData.getStartSide(), testData.getGoalPose(), testData.getPlanarRegionsList(),
                        assertPlannerReturnedResult());

      if (assertPlannerReturnedResult())
         assertTrue(PlannerTools.isGoalNextToLastStep(testData.getGoalPose(), footstepPlan));

      if (messager != null && visualize())
      {
         parametersReference = messager.createInput(PlannerParametersTopic, getDefaultPlannerParameters());

         messager.submitMessage(FootstepPlannerMessagerAPI.FootstepPlanTopic, footstepPlan);

         ThreadTools.sleep(10);

         messager.registerTopicListener(ComputePathTopic, request -> iterateOnPlan(testData));

         if (keepUp())
            ThreadTools.sleepForever();
      }
   }

   private void iterateOnPlan(PlannerTestEnvironments.PlannerTestData testData)
   {
      PrintTools.info("Iterating");
      FootstepPlan footstepPlan = PlannerTools
            .runPlanner(getPlanner(), testData.getStartPose(), testData.getStartSide(), testData.getGoalPose(), testData.getPlanarRegionsList(),
                        assertPlannerReturnedResult());

      messager.submitMessage(FootstepPlannerMessagerAPI.FootstepPlanTopic, footstepPlan);
   }

   private void submitInfoToUI(PlannerTestEnvironments.PlannerTestData testData)
   {
      messager.submitMessage(FootstepPlannerMessagerAPI.PlanarRegionDataTopic, testData.getPlanarRegionsList());
      messager.submitMessage(FootstepPlannerMessagerAPI.GoalPositionTopic, testData.getGoalPosition());
      messager.submitMessage(FootstepPlannerMessagerAPI.GoalOrientationTopic, testData.getGoalOrientation());
      messager.submitMessage(FootstepPlannerMessagerAPI.StartPositionTopic, testData.getStartPosition());
      messager.submitMessage(FootstepPlannerMessagerAPI.StartOrientationTopic, testData.getStartOrientation());
   }
}
