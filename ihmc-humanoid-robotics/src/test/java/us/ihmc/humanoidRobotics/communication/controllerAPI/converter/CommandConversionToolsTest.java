package us.ihmc.humanoidRobotics.communication.controllerAPI.converter;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import org.junit.jupiter.api.Tag;
import us.ihmc.humanoidRobotics.communication.controllerAPI.command.EuclideanTrajectoryControllerCommand;
import us.ihmc.humanoidRobotics.communication.controllerAPI.command.SE3TrajectoryControllerCommand;
import us.ihmc.humanoidRobotics.communication.controllerAPI.command.SO3TrajectoryControllerCommand;

public class CommandConversionToolsTest
{
   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testSO3()
   {
      Random random = new Random(4219L);
      for (int i = 0; i < 100; i++)
      {
         SO3TrajectoryControllerCommand expected = new SO3TrajectoryControllerCommand(random);
         SE3TrajectoryControllerCommand intermediate = new SE3TrajectoryControllerCommand();
         SO3TrajectoryControllerCommand actual = new SO3TrajectoryControllerCommand();

         CommandConversionTools.convertToSE3(expected, intermediate);
         CommandConversionTools.convertToSO3(intermediate, actual);

         Assert.assertEquals(0, intermediate.getSelectionMatrix().getLinearPart().getNumberOfSelectedAxes());
         Assert.assertTrue(expected.epsilonEquals(actual, Double.MIN_VALUE));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testEuclidean()
   {
      Random random = new Random(5204L);
      for (int i = 0; i < 100; i++)
      {
         EuclideanTrajectoryControllerCommand expected = new EuclideanTrajectoryControllerCommand(random);
         SE3TrajectoryControllerCommand intermediate = new SE3TrajectoryControllerCommand();
         EuclideanTrajectoryControllerCommand actual = new EuclideanTrajectoryControllerCommand();

         CommandConversionTools.convertToSE3(expected, intermediate);
         CommandConversionTools.convertToEuclidean(intermediate, actual);

         Assert.assertEquals(0, intermediate.getSelectionMatrix().getAngularPart().getNumberOfSelectedAxes());
         Assert.assertTrue(expected.epsilonEquals(actual, Double.MIN_VALUE));
      }
   }
}
