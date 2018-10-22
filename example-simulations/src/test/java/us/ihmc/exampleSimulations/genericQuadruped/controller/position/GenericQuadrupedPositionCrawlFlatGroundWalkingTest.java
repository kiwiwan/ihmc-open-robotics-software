package us.ihmc.exampleSimulations.genericQuadruped.controller.position;

import org.junit.jupiter.api.Test;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.exampleSimulations.genericQuadruped.GenericQuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.QuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.controller.position.QuadrupedPositionCrawlFlatGroundWalkingTest;
import us.ihmc.simulationconstructionset.util.ControllerFailureException;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

import java.io.IOException;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class GenericQuadrupedPositionCrawlFlatGroundWalkingTest extends QuadrupedPositionCrawlFlatGroundWalkingTest
{
   @Override
   public QuadrupedTestFactory createQuadrupedTestFactory()
   {
      return new GenericQuadrupedTestFactory();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 33.0)
   @Test
   public void testWalkingForwardFast() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingForwardFast();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 33.0)
   @Test
   public void testWalkingForwardSlow() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingForwardSlow();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 33.0)
   @Test
   public void testWalkingBackwardsFast() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingBackwardsFast();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 33.0)
   @Test
   public void testWalkingBackwardsSlow() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingBackwardsSlow();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 70.0)
   @Test
   public void testWalkingInAForwardLeftCircle() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingInAForwardLeftCircle();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 70.0)
   @Test
   public void testWalkingInAForwardRightCircle() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingInAForwardRightCircle();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 70.0)
   @Test
   public void testWalkingInABackwardLeftCircle() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingInABackwardLeftCircle();
   }
   
   @Override
   @ContinuousIntegrationTest(estimatedDuration = 70.0)
   @Test
   public void testWalkingInABackwardRightCircle() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.testWalkingInABackwardRightCircle();
   }
}
