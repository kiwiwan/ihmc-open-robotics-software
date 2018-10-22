package us.ihmc.exampleSimulations.skippy;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

@Disabled
public class SkippyV2Test
{

   private static final boolean sleepAfterTest = false;
   private SkippySimulationV2 skippySimulationV2;
   private SkippyRobotV2 skippy;

   @Test
   public void testStanding() throws SimulationExceededMaximumTimeException
   {
      skippy.setQ_hip(0.1);
      skippy.setQ_shoulder(0.1);
      assertTrue(skippySimulationV2.run(10));
      System.out.println("testStanding");
   }

   @Test
   public void testRecoveringFromPush() throws SimulationExceededMaximumTimeException
   {
      double pushDuration = 0.03;
      Vector3D pushForce = new Vector3D(0.0, 0.0, -10.0);

      assertTrue(skippySimulationV2.run(1.0));
      pushRobot(pushDuration, pushForce);
      assertTrue(skippySimulationV2.run(5.0));
      System.out.println("testRecoveringFromPush");
   }

   private void pushRobot(double time, Vector3D force) throws SimulationExceededMaximumTimeException
   {
      SkippyRobotV2 skippy = skippySimulationV2.getSkippy();
      skippy.setRootJointForce(force.getX(), force.getY(), force.getZ());
      assertTrue(skippySimulationV2.run(time));
      skippy.setRootJointForce(0.0, 0.0, 0.0);
      System.out.println("pushRobot");
   }

   @BeforeEach
   public void setupTest()
   {
      skippySimulationV2 = new SkippySimulationV2();
      skippy = skippySimulationV2.getSkippy();
   }

   @AfterEach
   public void afterTest()
   {
      if (sleepAfterTest)
         ThreadTools.sleepForever();
      skippySimulationV2.destroy();
   }

}
