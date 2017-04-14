package us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.icpOptimization.multipliers.recursion;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.dataStructures.variable.DoubleYoVariable;

public class ExitCMPRecursionMultipliersTest
{
   private static final double epsilon = 0.0001;

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderOneStepOneRegisteredTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 1;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = true;

         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
         double currentTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(0).getDoubleValue()) * singleSupportDurations.get(0).getDoubleValue() +
               transferSplitFractions.get(1).getDoubleValue() * doubleSupportDurations.get(1).getDoubleValue();
         double upcomingTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(1).getDoubleValue()) * doubleSupportDurations.get(1).getDoubleValue() +
               swingSplitFractions.get(1).getDoubleValue() * singleSupportDurations.get(1).getDoubleValue();
         double upcomingTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(1).getDoubleValue()) * singleSupportDurations.get(1).getDoubleValue() +
               transferSplitFractions.get(2).getDoubleValue() * doubleSupportDurations.get(2).getDoubleValue();

         double exitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + upcomingTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * upcomingTimeSpentOnExitCMP));
         Assert.assertEquals(exitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(exitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         exitCMPMultiplier = Math.exp(-omega * (currentStepDuration + upcomingTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * upcomingTimeSpentOnExitCMP));
         Assert.assertEquals(exitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(exitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderOneStepTwoRegisteredTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 2;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = true;

         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
         double currentTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(0).getDoubleValue()) * singleSupportDurations.get(0).getDoubleValue() +
               transferSplitFractions.get(1).getDoubleValue() * doubleSupportDurations.get(1).getDoubleValue();
         double upcomingTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(1).getDoubleValue()) * doubleSupportDurations.get(1).getDoubleValue() +
               swingSplitFractions.get(1).getDoubleValue() * singleSupportDurations.get(1).getDoubleValue();
         double upcomingTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(1).getDoubleValue()) * singleSupportDurations.get(1).getDoubleValue() +
               transferSplitFractions.get(2).getDoubleValue() * doubleSupportDurations.get(2).getDoubleValue();

         double exitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + upcomingTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * upcomingTimeSpentOnExitCMP));
         Assert.assertEquals(exitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(exitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         exitCMPMultiplier = Math.exp(-omega * (currentStepDuration + upcomingTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * upcomingTimeSpentOnExitCMP));
         Assert.assertEquals(exitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(exitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderOneStepOneRegisteredOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 1;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = false;

         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderOneStepTwoRegisteredOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 2;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = false;

         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(1, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepsOneRegisteredTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 1;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = true;

         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();

         double currentTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(0).getDoubleValue()) * singleSupportDurations.get(0).getDoubleValue() +
               transferSplitFractions.get(1).getDoubleValue() * doubleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(1).getDoubleValue()) * doubleSupportDurations.get(1).getDoubleValue() +
               swingSplitFractions.get(1).getDoubleValue() * singleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(1).getDoubleValue()) * singleSupportDurations.get(1).getDoubleValue() +
               transferSplitFractions.get(2).getDoubleValue() * doubleSupportDurations.get(2).getDoubleValue();

         double firstExitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         firstExitCMPMultiplier = Math.exp(-omega * (currentStepDuration + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepTwoRegisteredTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 2;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = true;

         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
         double nextStepDuration = doubleSupportDurations.get(1).getDoubleValue() + singleSupportDurations.get(1).getDoubleValue();

         double currentTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(0).getDoubleValue()) * singleSupportDurations.get(0).getDoubleValue() +
               transferSplitFractions.get(1).getDoubleValue() * doubleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(1).getDoubleValue()) * doubleSupportDurations.get(1).getDoubleValue() +
               swingSplitFractions.get(1).getDoubleValue() * singleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(1).getDoubleValue()) * singleSupportDurations.get(1).getDoubleValue() +
               transferSplitFractions.get(2).getDoubleValue() * doubleSupportDurations.get(2).getDoubleValue();
         double nextNextTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(2).getDoubleValue()) * doubleSupportDurations.get(2).getDoubleValue() +
               swingSplitFractions.get(2).getDoubleValue() * singleSupportDurations.get(2).getDoubleValue();
         double nextNextTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(2).getDoubleValue()) * singleSupportDurations.get(2).getDoubleValue() +
               transferSplitFractions.get(3).getDoubleValue() * doubleSupportDurations.get(3).getDoubleValue();

         double firstExitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));
         double secondExitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + nextStepDuration + nextNextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextNextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(secondExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         Assert.assertFalse(Double.isNaN(secondExitCMPMultiplier));
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         firstExitCMPMultiplier = Math.exp(-omega * (currentStepDuration + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));
         secondExitCMPMultiplier = Math.exp(-omega * (currentStepDuration + nextStepDuration + nextNextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextNextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(secondExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         Assert.assertFalse(Double.isNaN(secondExitCMPMultiplier));
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepThreeRegisteredTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 3;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = true;

         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
         double nextStepDuration = doubleSupportDurations.get(1).getDoubleValue() + singleSupportDurations.get(1).getDoubleValue();

         double currentTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(0).getDoubleValue()) * singleSupportDurations.get(0).getDoubleValue() +
               transferSplitFractions.get(1).getDoubleValue() * doubleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(1).getDoubleValue()) * doubleSupportDurations.get(1).getDoubleValue() +
               swingSplitFractions.get(1).getDoubleValue() * singleSupportDurations.get(1).getDoubleValue();
         double nextTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(1).getDoubleValue()) * singleSupportDurations.get(1).getDoubleValue() +
               transferSplitFractions.get(2).getDoubleValue() * doubleSupportDurations.get(2).getDoubleValue();
         double nextNextTimeSpentOnEntryCMP = (1.0 - doubleSupportDurations.get(2).getDoubleValue()) * doubleSupportDurations.get(2).getDoubleValue() +
               swingSplitFractions.get(2).getDoubleValue() * singleSupportDurations.get(2).getDoubleValue();
         double nextNextTimeSpentOnExitCMP = (1.0 - swingSplitFractions.get(2).getDoubleValue()) * singleSupportDurations.get(2).getDoubleValue() +
               transferSplitFractions.get(3).getDoubleValue() * doubleSupportDurations.get(3).getDoubleValue();

         double firstExitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));
         double secondExitCMPMultiplier = Math.exp(-omega * (currentTimeSpentOnExitCMP + nextStepDuration + nextNextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextNextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(secondExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         Assert.assertFalse(Double.isNaN(secondExitCMPMultiplier));
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         firstExitCMPMultiplier = Math.exp(-omega * (currentStepDuration + nextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextTimeSpentOnExitCMP));
         secondExitCMPMultiplier = Math.exp(-omega * (currentStepDuration + nextStepDuration + nextNextTimeSpentOnEntryCMP)) * (1.0 - Math.exp(-omega * nextNextTimeSpentOnExitCMP));

         Assert.assertEquals(firstExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(secondExitCMPMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         Assert.assertFalse(Double.isNaN(firstExitCMPMultiplier));
         Assert.assertFalse(Double.isNaN(secondExitCMPMultiplier));
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepsOneRegisteredOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 1;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {

         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = false;

         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));


         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         for (int i = 1; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepsTwoRegisteredOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 2;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = false;

         exitCMPRecursionMultipliers.compute(2, 2, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, 2, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testConsiderTwoStepsThreeRegisteredOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int stepsRegistered = 3;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int iter = 0; iter < iters; iter++)
      {
         for (int i = 0; i < stepsRegistered; i++)
         {
            doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
            singleSupportDurations.get(i).set(5.0 * random.nextDouble());
            transferSplitFractions.get(i).set(0.8 * random.nextDouble());
            swingSplitFractions.get(i).set(0.8 * random.nextDouble());
         }
         doubleSupportDurations.get(stepsRegistered).set(2.0 * random.nextDouble());
         transferSplitFractions.get(stepsRegistered).set(0.8 * random.nextDouble());


         // setup for in swing
         boolean isInTransfer = false;
         boolean useTwoCMPs = false;

         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));

         // setup for in transfer
         isInTransfer = true;
         exitCMPRecursionMultipliers.compute(2, stepsRegistered, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(0), epsilon);
         Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(1), epsilon);
         for (int i = 2; i < maxSteps; i++)
            Assert.assertTrue(Double.isNaN(exitCMPRecursionMultipliers.getExitMultiplier(i)));
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testNStepTwoCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int j = 1; j < maxSteps; j++)
      {
         for (int iter = 0; iter < iters; iter++)
         {
            for (int i = 0; i < maxSteps; i++)
            {
               doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
               singleSupportDurations.get(i).set(5.0 * random.nextDouble());
               transferSplitFractions.get(i).set(0.8 * random.nextDouble());
               swingSplitFractions.get(i).set(0.8 * random.nextDouble());
            }
            doubleSupportDurations.get(maxSteps).set(2.0 * random.nextDouble());
            transferSplitFractions.get(maxSteps).set(0.8 * random.nextDouble());

            // setup for in swing
            boolean isInTransfer = false;
            boolean useTwoCMPs = true;

            exitCMPRecursionMultipliers.compute(j, j + 1, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
            double recursionTime = swingSplitFractions.get(0).getDoubleValue() * currentStepDuration;

            for (int i = 0; i < j; i++)
            {
               double stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue() + singleSupportDurations.get(i + 1).getDoubleValue();

               double timeSpentOnEntryCMP = (1.0 - transferSplitFractions.get(i).getDoubleValue()) * doubleSupportDurations.get(i).getDoubleValue() +
                     swingSplitFractions.get(i).getDoubleValue() * singleSupportDurations.get(i).getDoubleValue();
               double timeSpentOnExitCMP = (1.0 - swingSplitFractions.get(i).getDoubleValue()) * singleSupportDurations.get(i).getDoubleValue() +
                     transferSplitFractions.get(i + 1).getDoubleValue() * doubleSupportDurations.get(i + 1).getDoubleValue();

               double exitMultiplier = Math.exp(-omega * (timeSpentOnEntryCMP + recursionTime)) * ( 1.0 - Math.exp(-omega * timeSpentOnExitCMP));

               Assert.assertEquals(exitMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
               Assert.assertFalse(Double.isNaN(exitMultiplier));

               recursionTime += stepDuration;
            }

            // setup for in transfer
            isInTransfer = true;
            exitCMPRecursionMultipliers.compute(j, j + 1, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            recursionTime = currentStepDuration;
            for (int i = 0; i < j; i++)
            {
               double stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue() + singleSupportDurations.get(i + 1).getDoubleValue();

               double timeSpentOnEntryCMP = (1.0 - transferSplitFractions.get(i).getDoubleValue()) * doubleSupportDurations.get(i).getDoubleValue() +
                     swingSplitFractions.get(i).getDoubleValue() * singleSupportDurations.get(i).getDoubleValue();
               double timeSpentOnExitCMP = (1.0 - swingSplitFractions.get(i).getDoubleValue()) * singleSupportDurations.get(i).getDoubleValue() +
                     transferSplitFractions.get(i + 1).getDoubleValue() * doubleSupportDurations.get(i + 1).getDoubleValue();

               double exitMultiplier = Math.exp(-omega * (timeSpentOnEntryCMP + recursionTime)) * ( 1.0 - Math.exp(-omega * timeSpentOnExitCMP));

               Assert.assertEquals(exitMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
               Assert.assertFalse(Double.isNaN(exitMultiplier));

               recursionTime += stepDuration;
            }
         }
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testNStepTwoCMPCalculationFinalTransfer()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int j = 1; j < maxSteps; j++)
      {
         for (int iter = 0; iter < iters; iter++)
         {
            for (int i = 0; i < maxSteps; i++)
            {
               doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
               singleSupportDurations.get(i).set(5.0 * random.nextDouble());
               transferSplitFractions.get(i).set(0.8 * random.nextDouble());
               swingSplitFractions.get(i).set(0.8 * random.nextDouble());
            }
            doubleSupportDurations.get(maxSteps).set(2.0 * random.nextDouble());
            transferSplitFractions.get(maxSteps).set(0.8 * random.nextDouble());

            // setup for in swing
            boolean isInTransfer = false;
            boolean useTwoCMPs = true;

            exitCMPRecursionMultipliers.compute(j, j, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            double currentStepDuration = doubleSupportDurations.get(0).getDoubleValue() + singleSupportDurations.get(0).getDoubleValue();
            double recursionTime = swingSplitFractions.get(0).getDoubleValue() * currentStepDuration;

            for (int i = 0; i < j; i++)
            {
               double stepDuration;
               if (i + 1 == j)
                  stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue();
               else
                  stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue() + singleSupportDurations.get(i + 1).getDoubleValue();

               double timeSpentOnEntryCMP = (1.0 - transferSplitFractions.get(i).getDoubleValue()) * doubleSupportDurations.get(i).getDoubleValue() +
                     swingSplitFractions.get(i).getDoubleValue() * singleSupportDurations.get(i).getDoubleValue();
               double timeSpentOnExitCMP = (1.0 - swingSplitFractions.get(i).getDoubleValue()) * singleSupportDurations.get(i).getDoubleValue() +
                     transferSplitFractions.get(i + 1).getDoubleValue() * doubleSupportDurations.get(i + 1).getDoubleValue();

               double exitMultiplier = Math.exp(-omega * (timeSpentOnEntryCMP + recursionTime)) * ( 1.0 - Math.exp(-omega * timeSpentOnExitCMP));

               Assert.assertEquals(exitMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
               Assert.assertFalse(Double.isNaN(exitMultiplier));

               recursionTime += stepDuration;
            }

            // setup for in transfer
            isInTransfer = true;
            exitCMPRecursionMultipliers.compute(j, j, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            recursionTime = currentStepDuration;
            for (int i = 0; i < j; i++)
            {
               double stepDuration;
               if (i + 1 == j)
                  stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue();
               else
                  stepDuration = doubleSupportDurations.get(i + 1).getDoubleValue() + singleSupportDurations.get(i + 1).getDoubleValue();

               double timeSpentOnEntryCMP = (1.0 - transferSplitFractions.get(i).getDoubleValue()) * doubleSupportDurations.get(i).getDoubleValue() +
                     swingSplitFractions.get(i).getDoubleValue() * singleSupportDurations.get(i).getDoubleValue();
               double timeSpentOnExitCMP = (1.0 - swingSplitFractions.get(i).getDoubleValue()) * singleSupportDurations.get(i).getDoubleValue() +
                     transferSplitFractions.get(i + 1).getDoubleValue() * doubleSupportDurations.get(i + 1).getDoubleValue();

               double exitMultiplier = Math.exp(-omega * (timeSpentOnEntryCMP + recursionTime)) * ( 1.0 - Math.exp(-omega * timeSpentOnExitCMP));

               Assert.assertEquals(exitMultiplier, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
               Assert.assertFalse(Double.isNaN(exitMultiplier));

               recursionTime += stepDuration;
            }
         }
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testNStepOneCMPCalculation()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int j = 1; j < maxSteps; j++)
      {
         for (int iter = 0; iter < iters; iter++)
         {
            for (int i = 0; i < maxSteps; i++)
            {
               doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
               singleSupportDurations.get(i).set(5.0 * random.nextDouble());
               transferSplitFractions.get(i).set(0.8 * random.nextDouble());
               swingSplitFractions.get(i).set(0.8 * random.nextDouble());
            }
            doubleSupportDurations.get(maxSteps).set(2.0 * random.nextDouble());
            transferSplitFractions.get(maxSteps).set(0.8 * random.nextDouble());

            // setup for in swing
            boolean isInTransfer = false;
            boolean useTwoCMPs = false;

            exitCMPRecursionMultipliers.compute(j, j + 1, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            for (int i = 0; i < j ; i++)
            {
               Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
            }

            // setup for in transfer
            isInTransfer = true;
            exitCMPRecursionMultipliers.compute(j, j + 1, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            for (int i = 0; i < j ; i++)
            {
               Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
            }
         }
      }
   }

   @ContinuousIntegrationTest(estimatedDuration = 1.0)
   @Test(timeout = 21000)
   public void testNStepOneCMPCalculationFinalTransfer()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry");
      DoubleYoVariable yoOmega = new DoubleYoVariable("omega", registry);

      double omega = 3.0;
      yoOmega.set(omega);

      int maxSteps = 5;
      int iters = 100;

      Random random = new Random();
      ArrayList<DoubleYoVariable> doubleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> singleSupportDurations = new ArrayList<>();
      ArrayList<DoubleYoVariable> transferSplitFractions = new ArrayList<>();
      ArrayList<DoubleYoVariable> swingSplitFractions = new ArrayList<>();

      for (int i = 0 ; i < maxSteps + 1; i++)
      {
         DoubleYoVariable doubleSupportDuration = new DoubleYoVariable("doubleSupportDuration" + i, registry);
         DoubleYoVariable singleSupportDuration = new DoubleYoVariable("singleSupportDuration" + i, registry);
         doubleSupportDuration.setToNaN();
         singleSupportDuration.setToNaN();
         doubleSupportDurations.add(doubleSupportDuration);
         singleSupportDurations.add(singleSupportDuration);

         DoubleYoVariable transferSplitFraction = new DoubleYoVariable("transferSplitFraction" + i, registry);
         DoubleYoVariable swingSplitFraction = new DoubleYoVariable("swingSplitFraction" + i, registry);
         transferSplitFraction.setToNaN();
         swingSplitFraction.setToNaN();
         transferSplitFractions.add(transferSplitFraction);
         swingSplitFractions.add(swingSplitFraction);
      }

      ExitCMPRecursionMultipliers exitCMPRecursionMultipliers = new ExitCMPRecursionMultipliers("", maxSteps, swingSplitFractions, transferSplitFractions, registry);

      for (int j = 1; j < maxSteps; j++)
      {
         for (int iter = 0; iter < iters; iter++)
         {
            for (int i = 0; i < maxSteps; i++)
            {
               doubleSupportDurations.get(i).set(2.0 * random.nextDouble());
               singleSupportDurations.get(i).set(5.0 * random.nextDouble());
               transferSplitFractions.get(i).set(0.8 * random.nextDouble());
               swingSplitFractions.get(i).set(0.8 * random.nextDouble());
            }
            doubleSupportDurations.get(maxSteps).set(2.0 * random.nextDouble());
            transferSplitFractions.get(maxSteps).set(0.8 * random.nextDouble());

            // setup for in swing
            boolean isInTransfer = false;
            boolean useTwoCMPs = false;

            exitCMPRecursionMultipliers.compute(j, j, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            for (int i = 0; i < j ; i++)
            {
               Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
            }

            // setup for in transfer
            isInTransfer = true;
            exitCMPRecursionMultipliers.compute(j, j, doubleSupportDurations, singleSupportDurations, useTwoCMPs, isInTransfer, omega);

            for (int i = 0; i < j ; i++)
            {
               Assert.assertEquals(0.0, exitCMPRecursionMultipliers.getExitMultiplier(i), epsilon);
            }
         }
      }
   }
}
