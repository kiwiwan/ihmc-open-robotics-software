package us.ihmc.avatar.controllerAPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import controller_msgs.msg.dds.PelvisHeightTrajectoryMessage;
import controller_msgs.msg.dds.StopAllTrajectoryMessage;
import us.ihmc.avatar.DRCObstacleCourseStartingLocation;
import us.ihmc.avatar.MultiRobotTestInterface;
import us.ihmc.avatar.testTools.DRCSimulationTestHelper;
import us.ihmc.commonWalkingControlModules.controlModules.rigidBody.RigidBodyControlManager;
import us.ihmc.commonWalkingControlModules.trajectories.LookAheadCoMHeightTrajectoryGenerator;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tools.EuclidCoreTestTools;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.humanoidRobotics.communication.packets.HumanoidMessageTools;
import us.ihmc.robotModels.FullHumanoidRobotModel;
import us.ihmc.robotics.random.RandomGeometry;
import us.ihmc.robotics.screwTheory.MovingReferenceFrame;
import us.ihmc.robotics.screwTheory.RigidBody;
import us.ihmc.sensorProcessing.frames.CommonHumanoidReferenceFrames;
import us.ihmc.simulationConstructionSetTools.bambooTools.BambooTools;
import us.ihmc.simulationConstructionSetTools.util.environments.FlatGroundEnvironment;
import us.ihmc.simulationToolkit.controllers.PushRobotController;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;
import us.ihmc.simulationconstructionset.util.simulationTesting.SimulationTestingParameters;
import us.ihmc.tools.MemoryTools;
import us.ihmc.yoVariables.variable.YoDouble;

public abstract class EndToEndPelvisHeightTrajectoryMessageTest implements MultiRobotTestInterface
{
   private static final SimulationTestingParameters simulationTestingParameters = SimulationTestingParameters.createFromSystemProperties();

   private static final boolean DEBUG = false;

   private DRCSimulationTestHelper drcSimulationTestHelper;

   @ContinuousIntegrationAnnotations.ContinuousIntegrationTest(estimatedDuration = 38.3)
   @Test
   public void testSingleWaypoint() throws Exception
   {
      BambooTools.reportTestStartedMessage(simulationTestingParameters.getShowWindows());

      Random random = new Random(564574L);
      double epsilon = 1.0e-4;

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.DEFAULT;

      drcSimulationTestHelper = new DRCSimulationTestHelper(simulationTestingParameters, getRobotModel());
      drcSimulationTestHelper.setStartingLocation(selectedLocation);
      drcSimulationTestHelper.createSimulation(getClass().getSimpleName());

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(0.5);
      assertTrue(success);

      FullHumanoidRobotModel fullRobotModel = drcSimulationTestHelper.getControllerFullRobotModel();

      double trajectoryTime = 1.0;
      RigidBody pelvis = fullRobotModel.getPelvis();

      FramePoint3D desiredRandomPelvisPosition = getRandomPelvisPosition(random, pelvis);
      Point3D desiredPosition = new Point3D(desiredRandomPelvisPosition);

      if (DEBUG)
      {
         System.out.println(desiredPosition);
      }

      desiredRandomPelvisPosition.changeFrame(ReferenceFrame.getWorldFrame());

      desiredPosition.set(desiredRandomPelvisPosition);
      if (DEBUG)
      {
         System.out.println(desiredPosition);
      }

      PelvisHeightTrajectoryMessage pelvisHeightTrajectoryMessage = HumanoidMessageTools.createPelvisHeightTrajectoryMessage(trajectoryTime,
                                                                                                                             desiredPosition.getZ());

      drcSimulationTestHelper.publishToController(pelvisHeightTrajectoryMessage);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(3.0 + trajectoryTime);
      assertTrue(success);

      SimulationConstructionSet scs = drcSimulationTestHelper.getSimulationConstructionSet();

      // Hard to figure out how to verify the desired there
      //      trajOutput = scs.getVariable("pelvisHeightOffsetSubTrajectoryCubicPolynomialTrajectoryGenerator", "pelvisHeightOffsetSubTrajectoryCurrentValue").getValueAsDouble();
      //      assertEquals(desiredPosition.getZ(), trajOutput, epsilon);
      // Ending up doing a rough check on the actual height
      double pelvisHeight = scs.getVariable("PelvisLinearStateUpdater", "estimatedRootJointPositionZ").getValueAsDouble();
      assertEquals(desiredPosition.getZ(), pelvisHeight, 0.01);

      drcSimulationTestHelper.createVideo(getSimpleRobotName(), 2);
   }

   public void testSingleWaypointWithControlFrame() throws SimulationExceededMaximumTimeException
   {
      BambooTools.reportTestStartedMessage(simulationTestingParameters.getShowWindows());

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.DEFAULT;
      drcSimulationTestHelper = new DRCSimulationTestHelper(simulationTestingParameters, getRobotModel(), new FlatGroundEnvironment());
      drcSimulationTestHelper.setStartingLocation(selectedLocation);
      drcSimulationTestHelper.createSimulation(getClass().getSimpleName());

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(2.0);
      assertTrue(success);

      RigidBody pelvis = drcSimulationTestHelper.getControllerFullRobotModel().getPelvis();
      MovingReferenceFrame pelvisBodyFrame = pelvis.getBodyFixedFrame();
      FramePoint3D expectedPosition = new FramePoint3D(pelvisBodyFrame);
      expectedPosition.changeFrame(ReferenceFrame.getWorldFrame());

      double trajectoryTime = 0.1;
      PelvisHeightTrajectoryMessage pelvisHeightTrajectoryMessage = HumanoidMessageTools.createPelvisHeightTrajectoryMessage(trajectoryTime,
                                                                                                                             expectedPosition.getZ());

      pelvisHeightTrajectoryMessage.getEuclideanTrajectory().setUseCustomControlFrame(true);

      drcSimulationTestHelper.publishToController(pelvisHeightTrajectoryMessage);
      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0 + trajectoryTime);
      assertTrue(success);

      FramePoint3D actualPosition = new FramePoint3D(pelvisBodyFrame);
      actualPosition.changeFrame(ReferenceFrame.getWorldFrame());

      EuclidCoreTestTools.assertTuple3DEquals(expectedPosition, actualPosition, 2.0e-3);
   }

   protected FramePoint3D getRandomPelvisPosition(Random random, RigidBody pelvis)
   {
      FramePoint3D desiredRandomPelvisPosition = new FramePoint3D(pelvis.getBodyFixedFrame());
      desiredRandomPelvisPosition.set(RandomGeometry.nextPoint3D(random, 0.10, 0.20, 0.05));
      desiredRandomPelvisPosition.setZ(desiredRandomPelvisPosition.getZ() - 0.1);
      return desiredRandomPelvisPosition;
   }

   public void testSingleWaypointInUserMode() throws Exception
   {
      BambooTools.reportTestStartedMessage(simulationTestingParameters.getShowWindows());

      Random random = new Random(564574L);

      DRCObstacleCourseStartingLocation selectedLocation = DRCObstacleCourseStartingLocation.DEFAULT;

      drcSimulationTestHelper = new DRCSimulationTestHelper(simulationTestingParameters, getRobotModel());
      drcSimulationTestHelper.setStartingLocation(selectedLocation);
      drcSimulationTestHelper.createSimulation(getClass().getSimpleName());

      ThreadTools.sleep(1000);
      boolean success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(0.5);
      assertTrue(success);

      FullHumanoidRobotModel fullRobotModel = drcSimulationTestHelper.getControllerFullRobotModel();

      double trajectoryTime = 1.0;
      RigidBody pelvis = fullRobotModel.getPelvis();

      FramePoint3D desiredRandomPelvisPosition = getRandomPelvisPosition(random, pelvis);
      Point3D desiredPosition = new Point3D(desiredRandomPelvisPosition);

      System.out.println(desiredPosition);

      desiredRandomPelvisPosition.changeFrame(ReferenceFrame.getWorldFrame());

      desiredPosition.set(desiredRandomPelvisPosition);
      System.out.println(desiredPosition);

      PelvisHeightTrajectoryMessage pelvisHeightTrajectoryMessage = HumanoidMessageTools.createPelvisHeightTrajectoryMessage(trajectoryTime,
                                                                                                                             desiredPosition.getZ());

      pelvisHeightTrajectoryMessage.setEnableUserPelvisControl(true);
      drcSimulationTestHelper.publishToController(pelvisHeightTrajectoryMessage);

      success = drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0 + trajectoryTime);
      assertTrue(success);

      double pelvisHeight = fullRobotModel.getPelvis().getParentJoint().getFrameAfterJoint().getTransformToWorldFrame().getTranslationZ();
      assertEquals(desiredPosition.getZ(), pelvisHeight, 0.01);

      drcSimulationTestHelper.createVideo(getSimpleRobotName(), 2);
   }

   public void testSingleWaypointThenManualChange() throws Exception
   {
      BambooTools.reportTestStartedMessage(simulationTestingParameters.getShowWindows());

      drcSimulationTestHelper = new DRCSimulationTestHelper(simulationTestingParameters, getRobotModel());
      drcSimulationTestHelper.createSimulation(getClass().getSimpleName());

      ThreadTools.sleep(1000);
      assertTrue(drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.0));

      SimulationConstructionSet scs = drcSimulationTestHelper.getSimulationConstructionSet();
      String namespace = LookAheadCoMHeightTrajectoryGenerator.class.getSimpleName();
      YoDouble offsetHeight = (YoDouble) scs.getVariable(namespace, "offsetHeightAboveGround");

      FullHumanoidRobotModel fullRobotModel = drcSimulationTestHelper.getControllerFullRobotModel();
      RigidBody pelvis = fullRobotModel.getPelvis();
      MovingReferenceFrame pelvisFrame = pelvis.getBodyFixedFrame();

      FramePoint3D pelvisPosition = new FramePoint3D(pelvisFrame);
      pelvisPosition.changeFrame(ReferenceFrame.getWorldFrame());
      double initialPelvisHeight = pelvisPosition.getZ();

      Random random = new Random(4929L);
      for (int i = 0; i < 5; i++)
      {
         double offset1 = 0.06 * 2.0 * (random.nextDouble() - 0.5);
         double offset2 = 0.06 * 2.0 * (random.nextDouble() - 0.5);

         // Move pelvis using YoVariable
         offsetHeight.set(offset1);
         assertTrue(drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.5));
         pelvisPosition.setToZero(pelvisFrame);
         pelvisPosition.changeFrame(ReferenceFrame.getWorldFrame());
         Assert.assertEquals(initialPelvisHeight + offset1, pelvisPosition.getZ(), 0.01);

         // Move pelvis through message
         double desiredHeight = initialPelvisHeight + offset2;
         PelvisHeightTrajectoryMessage pelvisHeightTrajectoryMessage = HumanoidMessageTools.createPelvisHeightTrajectoryMessage(0.5, desiredHeight);
         drcSimulationTestHelper.publishToController(pelvisHeightTrajectoryMessage);
         assertTrue(drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(1.5));
         pelvisPosition.setToZero(pelvisFrame);
         pelvisPosition.changeFrame(ReferenceFrame.getWorldFrame());
         Assert.assertEquals(desiredHeight, pelvisPosition.getZ(), 0.01);
      }
   }

   /**
    * This test is to reproduce a bug found on Valkyrie where sending a stop all trajectory would cause the
    * robot to increase its height.
    */
   public void testStopAllTrajectory() throws Exception
   {
      BambooTools.reportTestStartedMessage(simulationTestingParameters.getShowWindows());

      drcSimulationTestHelper = new DRCSimulationTestHelper(simulationTestingParameters, getRobotModel(), new FlatGroundEnvironment());
      drcSimulationTestHelper.createSimulation(getClass().getSimpleName());
      drcSimulationTestHelper.setupCameraForUnitTest(new Point3D(0.4, 0.0, 1.2), new Point3D(0.4, 12.0, 1.2));
      ThreadTools.sleep(1000);

      // Apply a push to the robot so we get some tracking error going
      FullHumanoidRobotModel fullRobotModel = drcSimulationTestHelper.getControllerFullRobotModel();
      double forceMagnitude = fullRobotModel.getTotalMass() * 2.0;
      String pushJointName = fullRobotModel.getPelvis().getParentJoint().getName();
      PushRobotController pushController = new PushRobotController(drcSimulationTestHelper.getRobot(), pushJointName, new Vector3D(),
                                                                   1.0 / forceMagnitude);
      drcSimulationTestHelper.getSimulationConstructionSet().addYoGraphic(pushController.getForceVisualizer());
      pushController.applyForce(new Vector3D(0.0, 0.0, 1.0), forceMagnitude, Double.POSITIVE_INFINITY);

      assertTrue(drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(RigidBodyControlManager.INITIAL_GO_HOME_TIME + 1.0));
      CommonHumanoidReferenceFrames referenceFrames = drcSimulationTestHelper.getReferenceFrames();
      referenceFrames.updateFrames();
      double initialPelvisHeight = referenceFrames.getPelvisFrame().getTransformToWorldFrame().getTranslationZ();

      // Step the trajectory repeatedly
      StopAllTrajectoryMessage stopMessage = new StopAllTrajectoryMessage();
      for (int i = 0; i < 50; i++)
      {
         drcSimulationTestHelper.publishToController(stopMessage);
         assertTrue(drcSimulationTestHelper.simulateAndBlockAndCatchExceptions(0.1));
      }

      // Would be nicer to check desired values but we currently have so many difference height control schemes that that would not be easy.
      referenceFrames.updateFrames();
      double finalPelvisHeight = referenceFrames.getPelvisFrame().getTransformToWorldFrame().getTranslationZ();
      Assert.assertEquals(initialPelvisHeight, finalPelvisHeight, 1.0e-5);
   }

   @BeforeEach
   public void showMemoryUsageBeforeTest()
   {
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " before test.");
   }

   @AfterEach
   public void destroySimulationAndRecycleMemory()
   {
      if (simulationTestingParameters.getKeepSCSUp())
      {
         ThreadTools.sleepForever();
      }

      // Do this here in case a test fails. That way the memory will be recycled.
      if (drcSimulationTestHelper != null)
      {
         drcSimulationTestHelper.destroySimulation();
         drcSimulationTestHelper = null;
      }

      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " after test.");
   }
}
