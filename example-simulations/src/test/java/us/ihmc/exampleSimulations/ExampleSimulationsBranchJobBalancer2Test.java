package us.ihmc.exampleSimulations;

import org.junit.Test;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

import org.junit.jupiter.api.Tag;
public class ExampleSimulationsBranchJobBalancer2Test
{
   @ContinuousIntegrationTest(estimatedDuration = 1020.0)
   @Test(timeout = 30000)
   public void testExtraTimeToSyncJobs2()
   {

   }
}
