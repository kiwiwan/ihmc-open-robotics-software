package us.ihmc.footstepPlanning.remoteStandaloneDataSet;

import org.junit.Test;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import org.junit.jupiter.api.Tag;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.footstepPlanning.FootstepPlannerType;
import us.ihmc.pubsub.DomainFactory;

@Tag("fast")
@ContinuousIntegrationPlan(categories = IntegrationCategory.FAST)
public class RemoteStandaloneVisGraphAStarDataSetTest extends RemoteStandalonePlannerDataSetTest
{
   @Override
   public FootstepPlannerType getPlannerType()
   {
      return FootstepPlannerType.VIS_GRAPH_WITH_A_STAR;
   }

   @Override
   @Test(timeout = 500000)
   @ContinuousIntegrationAnnotations.ContinuousIntegrationTest(estimatedDuration = 125.0)
   public void testDatasetsWithoutOcclusion()
   {
      pubSubImplementation = DomainFactory.PubSubImplementation.INTRAPROCESS;
      setup();
      runAssertionsOnAllDatasetsWithoutOcclusions(dataset -> runAssertions(dataset));
   }

   @Override
   @Test(timeout = 500000)
   @Tag("in-development")
   @ContinuousIntegrationAnnotations.ContinuousIntegrationTest(estimatedDuration = 125.0, categoriesOverride = IntegrationCategory.IN_DEVELOPMENT)
   public void testDatasetsWithoutOcclusionInDevelopment()
   {
      pubSubImplementation = DomainFactory.PubSubImplementation.INTRAPROCESS;
      setup();
      runAssertionsOnAllDatasetsWithoutOcclusionsInDevelopment(dataset -> runAssertions(dataset));
   }

   @Test(timeout = 500000)
   @Tag("in-development")
   @ContinuousIntegrationAnnotations.ContinuousIntegrationTest(estimatedDuration = 13.0, categoriesOverride = IntegrationCategory.IN_DEVELOPMENT)
   public void testDatasetsWithoutOcclusionRTPS()
   {
      pubSubImplementation = DomainFactory.PubSubImplementation.FAST_RTPS;
      setup();
      runAssertionsOnAllDatasetsWithoutOcclusions(dataset -> runAssertions(dataset));
   }

   public static void main(String[] args) throws Exception
   {
      RemoteStandaloneVisGraphAStarDataSetTest test = new RemoteStandaloneVisGraphAStarDataSetTest();
      String prefix = "unitTestDataSets/test/";
      test.pubSubImplementation = DomainFactory.PubSubImplementation.INTRAPROCESS;
      test.setup();
      test.runAssertionsOnDataset(dataset -> test.runAssertions(dataset), prefix + "20171215_214730_CinderBlockField");
      test.tearDown();

   }
}
