package us.ihmc.exampleSimulations.genericQuadruped.controller.force;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.exampleSimulations.genericQuadruped.GenericQuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.QuadrupedTestFactory;
import us.ihmc.quadrupedRobotics.controller.force.QuadrupedXGaitTurning720Test;
import us.ihmc.simulationconstructionset.util.ControllerFailureException;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

import java.io.IOException;

public class GenericQuadrupedTurning720Test extends QuadrupedXGaitTurning720Test
{
   @Override
   public QuadrupedTestFactory createQuadrupedTestFactory()
   {
      return new GenericQuadrupedTestFactory();
   }

   @Override
   @Test // timeout = 1900000
   public void rotate720InPlaceRight() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.rotate720InPlaceRight();
   }

   @Override
   @Test // timeout = 2000000
   public void rotate720InPlaceLeft() throws SimulationExceededMaximumTimeException, ControllerFailureException, IOException
   {
      super.rotate720InPlaceLeft();
   }
}
