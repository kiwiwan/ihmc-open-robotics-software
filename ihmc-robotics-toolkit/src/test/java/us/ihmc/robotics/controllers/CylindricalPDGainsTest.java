package us.ihmc.robotics.controllers;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = {IntegrationCategory.FAST})
public class CylindricalPDGainsTest
{
   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test
   public void test()
   {
      Random random = new Random();
      for (int i = 0; i < 1000; i++)
      {
         double kpRadius = random.nextDouble() * 100;
         double kpAngle = random.nextDouble() * Math.PI * 2;
         double kpZ = random.nextDouble() * 100;
         double zeta = random.nextDouble() * 100;

         CylindricalPDGains cylindricalPDGains = new CylindricalPDGains(kpRadius, kpAngle, kpZ, zeta);
         
         assertEquals(kpRadius, cylindricalPDGains.getKpRadius(), 1e-6);
         assertEquals(kpAngle, cylindricalPDGains.getKpAngle(), 1e-6);
         assertEquals(kpZ, cylindricalPDGains.getKpZ(), 1e-6);
      }

   }

}
