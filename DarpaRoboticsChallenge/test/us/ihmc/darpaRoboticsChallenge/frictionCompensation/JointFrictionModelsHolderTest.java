package us.ihmc.darpaRoboticsChallenge.frictionCompensation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import us.ihmc.utilities.frictionModels.AsymmetricCoulombViscousFrictionModel;
import us.ihmc.utilities.frictionModels.AsymmetricCoulombViscousStribeckFrictionModel;
import us.ihmc.utilities.frictionModels.FrictionModel;
import us.ihmc.utilities.frictionModels.FrictionState;
import us.ihmc.utilities.frictionModels.JointFrictionModel;
import us.ihmc.utilities.frictionModels.NoCompensationFrictionModel;
import us.ihmc.yoUtilities.dataStructure.registry.YoVariableRegistry;

public class JointFrictionModelsHolderTest
{
   private static double epsilon = 1e-5;

   private double stictionTransitionVelocity = 0.011;

   private double positiveCoulomb = 5.5;
   private double positiveViscous = 0.2;
   private double negativeCoulomb = 6.1;
   private double negativeViscous = 0.21;

   private double positiveSigma = 220;
   private double positiveFc0 = 50;
   private double positiveFs0 = 30;
   private double positiveCs = 0.015;
   private double negativeSigma = 180;
   private double negativeFc0 = 50;
   private double negativeFs0 = 20;
   private double negativeCs = 0.007;

   private double requestedNonZeroForce = 10;
   private double requestedZeroForce = 0.0;
   private double currentJointVelocityLessThanStictionVelocity = 0.9 * stictionTransitionVelocity;
   private double currentJointVelocityGreaterThanStictionVelocity = 1.1 * stictionTransitionVelocity;
   private double requestedNonZeroJointVelocity = 1.2;
   private double requestedZeroJointVelocity = 0.0;

   private double velocityForStictionInForceMode = stictionTransitionVelocity * Math.signum(requestedNonZeroForce);

   private NoCompensationFrictionModel noCompensatingModel = new NoCompensationFrictionModel();
   private AsymmetricCoulombViscousFrictionModel asymmetricCVModel = new AsymmetricCoulombViscousFrictionModel(stictionTransitionVelocity, positiveCoulomb,
         positiveViscous, negativeCoulomb, negativeViscous);
   private AsymmetricCoulombViscousStribeckFrictionModel asymmetricCVSModel = new AsymmetricCoulombViscousStribeckFrictionModel(stictionTransitionVelocity,
         positiveSigma, positiveFc0, positiveFs0, positiveCs, negativeSigma, negativeFc0, negativeFs0, negativeCs);

   private String name = "simpleHolder";
   private YoVariableRegistry registry = new YoVariableRegistry("simpleRegistry");

   @Test
   public void testConstructorAndFrictionStateSelection()
   {
      JointFrictionModelsHolderForTest holder = new JointFrictionModelsHolderForTest(name, registry);

      // FrictionModel.OFF
      holder.setActiveFrictionModel(FrictionModel.OFF);
      FrictionModel model = holder.getActiveFrictionModel();
      assertEquals(FrictionModel.OFF, model);

      JointFrictionModel jointFrictionModel = holder.getActiveJointFrictionModel();
      assertEquals(noCompensatingModel, jointFrictionModel);

      holder.selectFrictionStateAndFrictionVelocity(requestedNonZeroForce, currentJointVelocityGreaterThanStictionVelocity, requestedNonZeroJointVelocity);
      FrictionState state = holder.getCurrentFrictionState();
      double friction = holder.getCurrentFrictionForce();
      assertEquals(FrictionState.NOT_COMPENSATING, state);
      assertEquals(0.0, friction, epsilon);

      // FrictionModel.ASYMMETRIC_COULOMB_VISCOUS or others
      holder.setActiveFrictionModel(FrictionModel.ASYMMETRIC_COULOMB_VISCOUS);
      FrictionModel model2 = holder.getActiveFrictionModel();
      assertEquals(FrictionModel.ASYMMETRIC_COULOMB_VISCOUS, model2);

      JointFrictionModel jointFrictionModel2 = holder.getActiveJointFrictionModel();
      assertEquals(asymmetricCVModel, jointFrictionModel2);

      double velocity2 = holder.selectFrictionStateAndFrictionVelocity(requestedNonZeroForce, currentJointVelocityGreaterThanStictionVelocity,
            requestedNonZeroJointVelocity);
      FrictionState state2 = holder.getCurrentFrictionState();
      assertEquals(FrictionState.OUT_STICTION, state2);
      assertEquals(currentJointVelocityGreaterThanStictionVelocity, velocity2, epsilon);

      Double velocity3 = holder.selectFrictionStateAndFrictionVelocity(requestedZeroForce, currentJointVelocityGreaterThanStictionVelocity,
            requestedZeroJointVelocity);
      FrictionState state3 = holder.getCurrentFrictionState();
      assertEquals(FrictionState.NOT_COMPENSATING, state3);
      assertNull(velocity3);

      double velocity4 = holder.selectFrictionStateAndFrictionVelocity(requestedNonZeroForce, currentJointVelocityLessThanStictionVelocity,
            requestedZeroJointVelocity);
      FrictionState state4 = holder.getCurrentFrictionState();
      assertEquals(FrictionState.IN_STICTION_FORCE_MODE, state4);
      assertEquals(velocityForStictionInForceMode, velocity4, epsilon);

      double velocity5 = holder.selectFrictionStateAndFrictionVelocity(requestedZeroForce, currentJointVelocityLessThanStictionVelocity,
            requestedNonZeroJointVelocity);
      FrictionState state5 = holder.getCurrentFrictionState();
      assertEquals(FrictionState.IN_STICTION_VELOCITY_MODE, state5);
      assertEquals(requestedNonZeroJointVelocity, velocity5, epsilon);
   }

   private class JointFrictionModelsHolderForTest extends JointFrictionModelsHolder
   {
      public JointFrictionModelsHolderForTest(String name, YoVariableRegistry registry)
      {
         super(name, registry, 0.0);
         frictionModels.put(FrictionModel.OFF, noCompensatingModel);
         frictionModels.put(FrictionModel.ASYMMETRIC_COULOMB_VISCOUS, asymmetricCVModel);
         frictionModels.put(FrictionModel.ASYMMETRIC_COULOMB_VISCOUS_STRIBECK, asymmetricCVSModel);
      }

      @Override
      protected void checkIfExistFrictionModelForThisJoint(FrictionModel requestedFrictionModel)
      {

      }
   }

}
