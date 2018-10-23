package us.ihmc.robotics.math;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.axisAngle.AxisAngle;
import us.ihmc.euclid.referenceFrame.FrameQuaternion;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameRandomTools;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.euclid.tuple4D.Vector4D;
import us.ihmc.robotics.geometry.AngleTools;
import us.ihmc.robotics.math.trajectories.SimpleOrientationTrajectoryGenerator;
import us.ihmc.robotics.random.RandomGeometry;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class QuaternionCalculusTest
{
   private static final double EPSILON = 1.0e-10;

   @AfterEach
   public void tearDown()
   {
      ReferenceFrameTools.clearWorldFrameTree();
   }

   @Test // timeout = 30000
   public void testLogAndExpAlgebra() throws Exception
   {
      Random random = new Random(651651961L);

      for (int i = 0; i < 10000; i++)
      {
         QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
         Quaternion q = RandomGeometry.nextQuaternion(random);

         Vector4D qLog = new Vector4D();
         Quaternion vExp = new Quaternion();
         
         quaternionCalculus.log(q, qLog);
         Vector3D v = new Vector3D(qLog.getX(),qLog.getY(),qLog.getZ()); 
         
         quaternionCalculus.exp(v, vExp);

         assertTrue(Math.abs(q.getX() - vExp.getX()) < 10e-10);
         assertTrue(Math.abs(q.getY() - vExp.getY()) < 10e-10);
         assertTrue(Math.abs(q.getZ() - vExp.getZ()) < 10e-10);
         assertTrue(Math.abs(q.getS() - vExp.getS()) < 10e-10);

      }
   }
   

   @Test // timeout = 30000
   public void testConversionQDotToAngularVelocityBackAndForth() throws Exception
   {
      Random random = new Random(651651961L);

      for (int i = 0; i < 10000; i++)
      {
         QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
         Quaternion q = RandomGeometry.nextQuaternion(random);
         double length = RandomNumbers.nextDouble(random, 0.0, 10.0);
         Vector3D expectedAngularVelocity = RandomGeometry.nextVector3D(random, length);
         if (random.nextBoolean())
            expectedAngularVelocity.negate();
         Vector3D actualAngularVelocity = new Vector3D();
         Vector4D qDot = new Vector4D();

         quaternionCalculus.computeQDot(q, expectedAngularVelocity, qDot);
         quaternionCalculus.computeAngularVelocityInWorldFrame(q, qDot, actualAngularVelocity);

         assertTrue(expectedAngularVelocity.epsilonEquals(actualAngularVelocity, EPSILON));
      }
   }

   @Test // timeout = 30000
   public void testConversionQDDotToAngularAccelerationBackAndForth() throws Exception
   {
      Random random = new Random(651651961L);

      for (int i = 0; i < 10000; i++)
      {
         QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
         Quaternion q = RandomGeometry.nextQuaternion(random);
         double length = RandomNumbers.nextDouble(random, 0.0, 10.0);
         Vector3D angularVelocity = RandomGeometry.nextVector3D(random, length);
         if (random.nextBoolean())
            angularVelocity.negate();
         Vector3D expectedAngularAcceleration = RandomGeometry.nextVector3D(random, length);
         if (random.nextBoolean())
            expectedAngularAcceleration.negate();
         Vector3D actualAngularAcceleration = new Vector3D();
         Vector4D qDot = new Vector4D();
         Vector4D qDDot = new Vector4D();

         quaternionCalculus.computeQDot(q, angularVelocity, qDot);

         quaternionCalculus.computeQDDot(q, qDot, expectedAngularAcceleration, qDDot);
         quaternionCalculus.computeAngularAcceleration(q, qDot, qDDot, actualAngularAcceleration);
         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, EPSILON));

         quaternionCalculus.computeQDDot(q, angularVelocity, actualAngularAcceleration, qDDot);
         quaternionCalculus.computeAngularAcceleration(q, qDot, qDDot, actualAngularAcceleration);
         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, EPSILON));

         quaternionCalculus.computeQDDot(q, qDot, angularVelocity, actualAngularAcceleration, qDDot);
         quaternionCalculus.computeAngularAcceleration(q, qDot, qDDot, actualAngularAcceleration);
         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, EPSILON));

         quaternionCalculus.computeQDDot(q, qDot, expectedAngularAcceleration, qDDot);
         quaternionCalculus.computeAngularAcceleration(q, qDDot, angularVelocity, actualAngularAcceleration);
         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, EPSILON));
      }
   }

   @Test // timeout = 30000
   public void testVelocityFromFDAgainstTrajectory() throws Exception
   {
      QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
      SimpleOrientationTrajectoryGenerator traj = new SimpleOrientationTrajectoryGenerator("traj", ReferenceFrame.getWorldFrame(), new YoVariableRegistry("null"));
      double trajectoryTime = 1.0;
      traj.setTrajectoryTime(trajectoryTime);
      Random random = new Random(65265L);
      FrameQuaternion initialOrientation = EuclidFrameRandomTools.nextFrameQuaternion(random, ReferenceFrame.getWorldFrame());
      FrameQuaternion finalOrientation = EuclidFrameRandomTools.nextFrameQuaternion(random, ReferenceFrame.getWorldFrame());
      traj.setInitialOrientation(initialOrientation);
      traj.setFinalOrientation(finalOrientation);
      traj.initialize();

      double dt = 1.0e-4;
      double dtForFD = 1.0e-6;

      FrameQuaternion orientation = new FrameQuaternion();
      FrameVector3D expectedAngularVelocity = new FrameVector3D();
      Quaternion q = new Quaternion();
      Vector4D qDot = new Vector4D();
      Quaternion qPrevious = new Quaternion();
      Quaternion qNext = new Quaternion();
      Vector3D actualAngularVelocity = new Vector3D();

      for (double time = dt; time <= trajectoryTime - dt; time += dt)
      {
         traj.compute(time);
         traj.getOrientation(orientation);
         traj.getAngularVelocity(expectedAngularVelocity);
         q.set(orientation);
         traj.compute(time - dtForFD);
         traj.getOrientation(orientation);
         qPrevious.set(orientation);
         traj.compute(time + dtForFD);
         traj.getOrientation(orientation);
         qNext.set(orientation);

         quaternionCalculus.computeQDotByFiniteDifferenceCentral(qPrevious, qNext, dtForFD, qDot);
         quaternionCalculus.computeAngularVelocityInWorldFrame(q, qDot, actualAngularVelocity);

         assertTrue(expectedAngularVelocity.epsilonEquals(actualAngularVelocity, 1.0e-8));
      }
   }

   @Test // timeout = 30000
   public void testFDSimpleCase() throws Exception
   {
      QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
      Random random = new Random(65265L);
      double integrationTime = 1.0;
      double angleVelocity = RandomNumbers.nextDouble(random, 0.0, 2.0 * Math.PI) / integrationTime;
      Vector3D expectedAngularVelocity = new Vector3D(angleVelocity, 0.0, 0.0);
      Vector3D expectedAngularAcceleration = new Vector3D();
      AxisAngle axisAnglePrevious = new AxisAngle(1.0, 0.0, 0.0, 0.0);
      AxisAngle axisAngleCurrent = new AxisAngle(1.0, 0.0, 0.0, 0.0);
      AxisAngle axisAngleNext = new AxisAngle(1.0, 0.0, 0.0, 0.0);
      Quaternion qPrevious = new Quaternion();
      Quaternion qCurrent = new Quaternion();
      Quaternion qNext = new Quaternion();
      Vector4D qDot = new Vector4D();
      Vector4D qDDot = new Vector4D();

      Vector3D actualAngularVelocity = new Vector3D();
      Vector3D actualAngularAcceleration = new Vector3D();

      double dt = 1.0e-4;
      for (double time = dt; time < integrationTime; time += dt)
      {
         axisAnglePrevious.setAngle(AngleTools.trimAngleMinusPiToPi(angleVelocity * (time - dt)) - Math.PI);
         qPrevious.set(axisAnglePrevious);
         axisAngleCurrent.setAngle(AngleTools.trimAngleMinusPiToPi(angleVelocity * time) - Math.PI);
         qCurrent.set(axisAngleCurrent);
         axisAngleNext.setAngle(AngleTools.trimAngleMinusPiToPi(angleVelocity * (time + dt)) - Math.PI);
         qNext.set(axisAngleNext);

         quaternionCalculus.computeQDotByFiniteDifferenceCentral(qPrevious, qNext, dt, qDot);
         quaternionCalculus.computeAngularVelocityInWorldFrame(qCurrent, qDot, actualAngularVelocity);

         quaternionCalculus.computeQDDotByFiniteDifferenceCentral(qPrevious, qCurrent, qNext, dt, qDDot);
         quaternionCalculus.computeAngularAcceleration(qCurrent, qDot, qDDot, actualAngularAcceleration);

         boolean sameVelocity = expectedAngularVelocity.epsilonEquals(actualAngularVelocity, 1.0e-7);
         if (!sameVelocity)
         {
            System.out.println("Expected angular velocity: " + expectedAngularVelocity);
            System.out.println("Actual   angular velocity: " + actualAngularVelocity);
         }
         assertTrue(sameVelocity);
         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, 1.0e-7));
      }
   }

   @Test // timeout = 30000
   public void testAccelerationFromFDAgainstTrajectory() throws Exception
   {
      QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
      SimpleOrientationTrajectoryGenerator traj = new SimpleOrientationTrajectoryGenerator("traj", ReferenceFrame.getWorldFrame(), new YoVariableRegistry("null"));
      double trajectoryTime = 1.0;
      traj.setTrajectoryTime(trajectoryTime);
      Random random = new Random(65265L);
      FrameQuaternion initialOrientation = EuclidFrameRandomTools.nextFrameQuaternion(random, ReferenceFrame.getWorldFrame());
      FrameQuaternion finalOrientation = EuclidFrameRandomTools.nextFrameQuaternion(random, ReferenceFrame.getWorldFrame());
      traj.setInitialOrientation(initialOrientation);
      traj.setFinalOrientation(finalOrientation);
      traj.initialize();

      double dt = 1.0e-4;
      double dtForFD = 1.0e-4;

      FrameQuaternion orientation = new FrameQuaternion();
      FrameVector3D expectedAngularAcceleration = new FrameVector3D();
      Quaternion q = new Quaternion();
      Vector4D qDot = new Vector4D();
      Vector4D qDDot = new Vector4D();
      Quaternion qPrevious = new Quaternion();
      Quaternion qNext = new Quaternion();
      Vector3D actualAngularAcceleration = new Vector3D();

      for (double time = dt; time <= trajectoryTime - dt; time += dt)
      {
         traj.compute(time);
         traj.getOrientation(orientation);
         traj.getAngularAcceleration(expectedAngularAcceleration);
         q.set(orientation);
         traj.compute(time - dtForFD);
         traj.getOrientation(orientation);
         qPrevious.set(orientation);
         traj.compute(time + dtForFD);
         traj.getOrientation(orientation);
         qNext.set(orientation);

         quaternionCalculus.computeQDotByFiniteDifferenceCentral(qPrevious, qNext, dtForFD, qDot);
         quaternionCalculus.computeQDDotByFiniteDifferenceCentral(qPrevious, q, qNext, dtForFD, qDDot);
         quaternionCalculus.computeAngularAcceleration(q, qDot, qDDot, actualAngularAcceleration);

         assertTrue(expectedAngularAcceleration.epsilonEquals(actualAngularAcceleration, 1.0e-5));
      }
   }

   @Test // timeout = 30000
   public void testInterpolateAgainstQuat4d() throws Exception
   {
      QuaternionCalculus quaternionCalculus = new QuaternionCalculus();
      Random random = new Random(6546545L);
      Quaternion q0 = RandomGeometry.nextQuaternion(random);
      Quaternion q1 = RandomGeometry.nextQuaternion(random);
      Quaternion expectedQInterpolated = new Quaternion();
      Quaternion actualQInterpolated = new Quaternion();

      for (double alpha = 0.0; alpha <= 1.0; alpha += 1.0e-6)
      {
         expectedQInterpolated.interpolate(q0, q1, alpha);
         quaternionCalculus.interpolate(alpha, q0, q1, actualQInterpolated);

         assertTrue(expectedQInterpolated.epsilonEquals(actualQInterpolated, EPSILON));
      }
   }
}
