package us.ihmc.simulationConstructionSetTools.util.dataProcessors;

import org.junit.After;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.simulationconstructionset.util.simulationTesting.SimulationTestingParameters;

@Tag("fast")
public class RobotAllJointsDataCheckerTest
{
   private static final SimulationTestingParameters simulationTestingParameters = SimulationTestingParameters.createFromSystemProperties();


   @After
   public void destroySimulationAndRecycleMemory()
   {
      if (simulationTestingParameters.getKeepSCSUp())
      {
         //ThreadTools.sleepForever();
      }
   }

   @Test // timeout = 36000
   public void test()
   {
      TwoLinkRobotForTesting twoLinkRobotForTesting = new TwoLinkRobotForTesting();

      SimulationConstructionSet scs = new SimulationConstructionSet(twoLinkRobotForTesting, simulationTestingParameters);
      scs.setDT(0.00001, 100);
      scs.startOnAThread();

      twoLinkRobotForTesting.setElbowPosition(0.0);
      twoLinkRobotForTesting.setUpperPosition(3.0);
      
      twoLinkRobotForTesting.setElbowVelocity(-2.0);
      twoLinkRobotForTesting.setUpperVelocity(-3.0);
      
      scs.simulate(6.0);
      
      while(scs.isSimulating())
      {
         Thread.yield();
      }
      
      RobotAllJointsDataChecker robotAllJointsDataChecker = new RobotAllJointsDataChecker(scs, twoLinkRobotForTesting);
      robotAllJointsDataChecker.cropFirstPoint();
      
      scs.applyDataProcessingFunction(robotAllJointsDataChecker);
   }

}
