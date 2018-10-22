package us.ihmc.robotics.math.trajectories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import us.ihmc.euclid.referenceFrame.FrameQuaternion;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.robotics.trajectories.providers.ConstantOrientationProvider;
import us.ihmc.robotics.trajectories.providers.OrientationProvider;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class ProviderBasedConstantOrientationTrajectoryGeneratorTest
{
   private static final double EPSILON = 1e-10;

   private String namePrefix = "namePrefix";
   private ReferenceFrame referenceFrame;
   private OrientationProvider orientationProvider;
   private double finalTime;
   private FrameQuaternion frameOrientation;

   private ProviderBasedConstantOrientationTrajectoryGenerator provider;
   private static int globalCounter = 0;

   @BeforeEach
   public void setUp()
   {
      referenceFrame = ReferenceFrame.constructARootFrame("rootFrame!");
      frameOrientation = new FrameQuaternion(referenceFrame);
      orientationProvider = new ConstantOrientationProvider(frameOrientation);
   }

   @AfterEach
   public void tearDown()
   {
      frameOrientation = null;
      referenceFrame = null;
      orientationProvider = null;
      ReferenceFrameTools.clearWorldFrameTree();
   }

	@Test
   public void testConstructor()
   {
      try
      {
         finalTime = -5.0;
         provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());
         fail();
      }
      catch (RuntimeException rte)
      {
         
      }

      finalTime = 0.0;
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());

      finalTime = Double.POSITIVE_INFINITY;
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());

      try
      {
         provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, null);
         fail();
      }
      catch (NullPointerException npe)
      {
      }

   }

	@Test
   public void testIsDone()
   {
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());
      provider.initialize();
      provider.compute(finalTime);
      assertFalse(provider.isDone());

      provider.compute(finalTime + EPSILON);
      assertTrue(provider.isDone());
   }

	@Test
   public void testGet()
   {
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());
      FrameQuaternion orientationToPack = new FrameQuaternion();

      provider.getOrientation(orientationToPack);
      
      assertEquals(referenceFrame, orientationToPack.getReferenceFrame());
   }

	@Test
   public void testPackAngularVelocity()
   {      
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());      
      FrameVector3D angularVelocityToPack = new FrameVector3D(ReferenceFrame.getWorldFrame(), 10.0, 10.0, 10.0);
      
      assertFalse(referenceFrame.equals(angularVelocityToPack.getReferenceFrame()));
      
      provider.getAngularVelocity(angularVelocityToPack);
      
     assertEquals(0.0, angularVelocityToPack.getX(), EPSILON);
     assertEquals(0.0, angularVelocityToPack.getY(), EPSILON);
     assertEquals(0.0, angularVelocityToPack.getZ(), EPSILON);
     assertSame(referenceFrame, angularVelocityToPack.getReferenceFrame());
   }

	@Test
   public void testPackAngularAcceleration()
   {      
      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime, createRegistry());      
      FrameVector3D angularAccelerationToPack = new FrameVector3D(ReferenceFrame.getWorldFrame(), 10.0, 10.0, 10.0);
      
      assertFalse(referenceFrame.equals(angularAccelerationToPack.getReferenceFrame()));
      
      provider.getAngularAcceleration(angularAccelerationToPack);
      
      assertEquals(0.0, angularAccelerationToPack.getX(), EPSILON);
      assertEquals(0.0, angularAccelerationToPack.getY(), EPSILON);
      assertEquals(0.0, angularAccelerationToPack.getZ(), EPSILON);
      assertSame(referenceFrame, angularAccelerationToPack.getReferenceFrame());
   }

	@Test
   public void testPackAngularData()
   {
      FrameQuaternion orientationToPack = new FrameQuaternion(referenceFrame);
      orientationToPack.setYawPitchRollIncludingFrame(referenceFrame, 4.4, 3.3, 1.4);

      provider = new ProviderBasedConstantOrientationTrajectoryGenerator(namePrefix, referenceFrame, orientationProvider, finalTime,
            createRegistry());
      provider.getOrientation(orientationToPack);

      assertEquals(referenceFrame, orientationToPack.getReferenceFrame());

      provider.getOrientation(orientationToPack);

      assertEquals(referenceFrame, orientationToPack.getReferenceFrame());
         
      FrameVector3D angularVelocityToPack = new FrameVector3D(ReferenceFrame.getWorldFrame(), 10.0, 10.0, 10.0);
      FrameVector3D angularAccelerationToPack = new FrameVector3D(ReferenceFrame.getWorldFrame(), 10.0, 10.0, 10.0);
      
      assertFalse(referenceFrame.equals(angularVelocityToPack.getReferenceFrame()));
      assertTrue(ReferenceFrame.getWorldFrame().equals(angularVelocityToPack.getReferenceFrame()));
       
      assertFalse(referenceFrame.equals(angularAccelerationToPack.getReferenceFrame()));
      assertTrue(ReferenceFrame.getWorldFrame().equals(angularAccelerationToPack.getReferenceFrame()));
      
      provider.getAngularData(orientationToPack, angularVelocityToPack, angularAccelerationToPack);
      
      assertEquals(0.0, orientationToPack.getYaw(), EPSILON);
      assertEquals(0.0, orientationToPack.getPitch(), EPSILON);
      assertEquals(0.0, orientationToPack.getRoll(), EPSILON);
      assertSame(referenceFrame, orientationToPack.getReferenceFrame());
      
      assertEquals(0.0, angularVelocityToPack.getX(), EPSILON);
      assertEquals(0.0, angularVelocityToPack.getY(), EPSILON);
      assertEquals(0.0, angularVelocityToPack.getZ(), EPSILON);
      assertSame(referenceFrame, angularVelocityToPack.getReferenceFrame());
      
      assertEquals(0.0, angularAccelerationToPack.getX(), EPSILON);
      assertEquals(0.0, angularAccelerationToPack.getY(), EPSILON);
      assertEquals(0.0, angularAccelerationToPack.getZ(), EPSILON);
      assertSame(referenceFrame, angularAccelerationToPack.getReferenceFrame());
   }
   
   private YoVariableRegistry createRegistry()
   {
      YoVariableRegistry registry = new YoVariableRegistry("registry" + globalCounter);
      globalCounter++;
      return registry;
   }
}
