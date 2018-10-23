package us.ihmc.robotics.math.trajectories.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.FrameQuaternion;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoFrameYawPitchRoll;


public class YoOrientationProviderTest
{
   private static final double EPSILON = 1e-10;
   
   private String namePrefix = "namePrefix";
   private ReferenceFrame referenceFrame;
   private YoVariableRegistry registry; 
   private YoFrameYawPitchRoll yoFrameOrientation;
   public FrameQuaternion frameOrientationToPack;
   
   @BeforeEach
   public void setUp()
   {
      referenceFrame =ReferenceFrame.constructARootFrame("rootFrame");
      registry = new YoVariableRegistry("yoVariableRegistry");
      yoFrameOrientation = new YoFrameYawPitchRoll(namePrefix, referenceFrame, registry);
      frameOrientationToPack = new FrameQuaternion();
   }

   @AfterEach
   public void tearDown()
   {
      ReferenceFrameTools.clearWorldFrameTree();
   }

	@Test // timeout = 30000
   public void testConstructor_Get()
   {
      YoOrientationProvider yoOrientationProvider = new YoOrientationProvider(yoFrameOrientation);
      yoOrientationProvider.getOrientation(frameOrientationToPack);
      
      double[] yawPitchRollToPack = new double[3];
      yoFrameOrientation.getYawPitchRoll(yawPitchRollToPack);
      
      double[] yawPitchRollToPack2 = new double[3];
      frameOrientationToPack.getYawPitchRoll(yawPitchRollToPack2);
      
      for(int i = 0; i < yawPitchRollToPack.length; i++)
      {
         assertEquals(yawPitchRollToPack[i], yawPitchRollToPack2[i], EPSILON);
      }
      
      assertSame(yoFrameOrientation.getReferenceFrame(), frameOrientationToPack.getReferenceFrame());
   }
}
