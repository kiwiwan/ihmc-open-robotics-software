package us.ihmc.avatar.pushRecovery;

import controller_msgs.msg.dds.FootstepDataListMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.stateMachine.core.StateTransitionCondition;

import java.util.Random;

import static org.junit.Assert.assertTrue;

public abstract class AvatarICPOptimizationPushRecoveryBTest extends AvatarICPOptimizationPushRecoveryTestSetup
{
   @Test // timeout = 150000
   public void testPushICPOptimizationNoPush() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationOutwardPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);

      // push timing:
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.5 * swingTime;

      // push parameters:
      Vector3D forceDirection = new Vector3D(0.0, -1.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.1;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationOutwardPushInSlowSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createSlowForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push timing:t
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.5 * swingTime;

      // push parameters:
      Vector3D forceDirection = new Vector3D(0.0, -1.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.1;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationDiagonalOutwardPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push timing:
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.5 * swingTime;

      // push parameters:
      Vector3D forceDirection = new Vector3D(2.0, -1.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.1;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationDiagonalYawingOutwardPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createYawingForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push parameters:
      StateTransitionCondition firstPushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      StateTransitionCondition secondPushCondition = singleSupportStartConditions.get(RobotSide.LEFT);
      double delay = 0.5 * swingTime;

      Vector3D firstForceDirection = new Vector3D(0.0, -1.0, 0.0);
      Vector3D secondForceDirection = new Vector3D(0.0, 1.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.1;
      pushRobotController.applyForceDelayed(firstPushCondition, delay, firstForceDirection, magnitude, duration);
      boolean success;

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);

      pushRobotController.applyForceDelayed(secondPushCondition, delay, secondForceDirection, magnitude, duration);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);

      pushRobotController.applyForceDelayed(firstPushCondition, delay, firstForceDirection, magnitude, duration);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);

      pushRobotController.applyForceDelayed(secondPushCondition, delay, secondForceDirection, magnitude, duration);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);

      pushRobotController.applyForceDelayed(firstPushCondition, delay, firstForceDirection, magnitude, duration);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);
      assertTrue(success);

      pushRobotController.applyForceDelayed(secondPushCondition, delay, secondForceDirection, magnitude, duration);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(3.0);

      assertTrue(success);

      validateTest(footsteps, false);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationRandomPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push timing:
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.5 * swingTime;

      Random random = new Random(73712L);
      double xDirection = 1.0 - 2.0 * random.nextDouble();
      double yDirection = 1.0 - 2.0 * random.nextDouble();

      // push parameters:
      Vector3D forceDirection = new Vector3D(xDirection, yDirection, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.1;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationLongForwardPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push timing:
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.1 * swingTime;

      // push parameters:
      Vector3D forceDirection = new Vector3D(1.0, 0.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.7 * swingTime;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }

   @Test // timeout = 150000
   public void testPushICPOptimizationLongBackwardPushInSwing() throws Exception
   {
      FootstepDataListMessage footsteps = createForwardWalkingFootstepMessage();
      setupAndRunTest(footsteps);
      drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0);

      // push timing:
      StateTransitionCondition pushCondition = singleSupportStartConditions.get(RobotSide.RIGHT);
      double delay = 0.1 * swingTime;

      // push parameters:
      Vector3D forceDirection = new Vector3D(-1.0, 0.0, 0.0);
      double magnitude = percentWeight * totalMass * 9.81;
      double duration = 0.8 * swingTime;
      pushRobotController.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);

      validateTest(footsteps);
   }
}
