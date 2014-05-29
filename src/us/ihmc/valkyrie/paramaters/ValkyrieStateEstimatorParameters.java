package us.ihmc.valkyrie.paramaters;

import static us.ihmc.darpaRoboticsChallenge.stateEstimation.DRCSimulatedSensorNoiseParameters.*;

import java.util.HashMap;

import us.ihmc.sensorProcessing.simulatedSensors.SensorFilterParameters;
import us.ihmc.sensorProcessing.simulatedSensors.SensorNoiseParameters;
import us.ihmc.sensorProcessing.stateEstimation.PointMeasurementNoiseParameters;
import us.ihmc.sensorProcessing.stateEstimation.StateEstimatorParameters;

public class ValkyrieStateEstimatorParameters implements StateEstimatorParameters
{
   private final boolean runningOnRealRobot;

   private final double estimatorDT;

   private final boolean useKinematicsBasedStateEstimator = true;
   private final boolean assumePerfectIMU = true;

   private final double kinematicsPelvisLinearVelocityFilterFreqInHertz;
   private final double kinematicsPelvisPositionFilterFreqInHertz;
   
   private final double jointVelocitySlopTimeForBacklashCompensation;

   private final double jointPositionFilterFrequencyHz;
   private final double jointVelocityFilterFrequencyHz;
   private final double orientationFilterFrequencyHz;
   private final double angularVelocityFilterFrequencyHz;
   private final double linearAccelerationFilterFrequencyHz;

   // State Estimator Filter Parameters
   private final double pointVelocityXYMeasurementStandardDeviation;
   private final double pointVelocityZMeasurementStandardDeviation;

   private final double pointPositionXYMeasurementStandardDeviation;
   private final double pointPositionZMeasurementStandardDeviation;

   private final boolean useTwoPolesForIMUFiltering;
   private final boolean doFiniteDifferenceForJointVelocities;

   private final SensorFilterParameters sensorFilterParameters;

   private final PointMeasurementNoiseParameters pointMeasurementNoiseParameters;

//   private final SensorNoiseParameters sensorNoiseParameters = createNoiseParametersForEstimatorJerryTuning();
   private final SensorNoiseParameters sensorNoiseParameters = createNoiseParametersForEstimatorJerryTuningSeptember2013();

   private final boolean doElasticityCompensation;
   private final double defaultJointStiffness;
   private final HashMap<String, Double> jointSpecificStiffness = new HashMap<String, Double>();

   public ValkyrieStateEstimatorParameters(boolean runningOnRealRobot, double estimatorDT)
   {
      this.runningOnRealRobot = runningOnRealRobot;

      this.estimatorDT = estimatorDT;

      jointPositionFilterFrequencyHz = runningOnRealRobot ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
      jointVelocityFilterFrequencyHz = runningOnRealRobot ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;

      orientationFilterFrequencyHz        = runningOnRealRobot ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
      angularVelocityFilterFrequencyHz    = runningOnRealRobot ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
      linearAccelerationFilterFrequencyHz = runningOnRealRobot ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;

      jointVelocitySlopTimeForBacklashCompensation = 0.03;

      pointVelocityXYMeasurementStandardDeviation = 2.0;
      pointVelocityZMeasurementStandardDeviation  = 2.0;

      pointPositionXYMeasurementStandardDeviation = 0.1;
      pointPositionZMeasurementStandardDeviation  = 0.1;

      useTwoPolesForIMUFiltering = runningOnRealRobot;
      doFiniteDifferenceForJointVelocities = false;

      doElasticityCompensation = false;
      defaultJointStiffness = Double.POSITIVE_INFINITY;

      sensorFilterParameters = new SensorFilterParameters(jointPositionFilterFrequencyHz, jointVelocityFilterFrequencyHz, orientationFilterFrequencyHz,
            angularVelocityFilterFrequencyHz, linearAccelerationFilterFrequencyHz, jointVelocitySlopTimeForBacklashCompensation, estimatorDT,
            useTwoPolesForIMUFiltering, doFiniteDifferenceForJointVelocities, doElasticityCompensation, defaultJointStiffness, jointSpecificStiffness);

      pointMeasurementNoiseParameters = new PointMeasurementNoiseParameters(pointVelocityXYMeasurementStandardDeviation,
            pointVelocityZMeasurementStandardDeviation, pointPositionXYMeasurementStandardDeviation, pointPositionZMeasurementStandardDeviation);
      
      kinematicsPelvisPositionFilterFreqInHertz = Double.POSITIVE_INFINITY;
      kinematicsPelvisLinearVelocityFilterFreqInHertz = 50.0; //16.0;
   }

   @Override
   public SensorFilterParameters getSensorFilterParameters()
   {
      return sensorFilterParameters;
   }

   @Override
   public boolean getAssumePerfectIMU()
   {
      return assumePerfectIMU;
   }

   @Override
   public boolean useKinematicsBasedStateEstimator()
   {
      return useKinematicsBasedStateEstimator;
   }

   @Override
   public PointMeasurementNoiseParameters getPointMeasurementNoiseParameters()
   {
      return pointMeasurementNoiseParameters;
   }

   @Override
   public SensorNoiseParameters getSensorNoiseParameters()
   {
      return sensorNoiseParameters;
   }

   @Override
   public double getEstimatorDT()
   {
      return estimatorDT;
   }

   @Override
   public boolean isRunningOnRealRobot()
   {
      return runningOnRealRobot;
   }

   @Override
   public double getKinematicsPelvisPositionFilterFreqInHertz()
   {
      return kinematicsPelvisPositionFilterFreqInHertz;
   }
   
   @Override
   public double getKinematicsPelvisLinearVelocityFilterFreqInHertz()
   {
      return kinematicsPelvisLinearVelocityFilterFreqInHertz;
   }

   @Override
   public double getCoPFilterFreqInHertz()
   {
      return 4.0;
   }

   @Override
   public boolean useAccelerometerForEstimation()
   {
      return true;
   }

   @Override
   public boolean estimateGravity()
   {
      return false;
   }

   @Override
   public double getGravityFilterFreqInHertz()
   {
      return 5.3052e-4;
   }

   @Override
   public double getPelvisPositionFusingFrequency()
   {
      return 11.7893; // alpha = 0.8 with dt = 0.003
   }

   @Override
   public double getPelvisLinearVelocityFusingFrequency()
   {
      return 0.4261; // alpha = 0.992 with dt = 0.003
   }

   @Override
   public double getDelayTimeForTrustingFoot()
   {
      return 0.02;
   }

   @Override
   public double getForceInPercentOfWeightThresholdToTrustFoot()
   {
      return 0.3;
   }

   @Override
   public boolean estimateIMUDrift()
   {
      return true;
   }

   @Override
   public boolean compensateIMUDrift()
   {
      return true;
   }

   @Override
   public double getIMUDriftFilterFreqInHertz()
   {
      return 0.5332;
   }

   @Override
   public double getFootVelocityUsedForImuDriftFilterFreqInHertz()
   {
      return 0.5332;
   }

   @Override
   public double getFootVelocityThresholdToEnableIMUDriftCompensation()
   {
      return 0.03;
   }

   @Override
   public boolean trustCoPAsNonSlippingContactPoint()
   {
      return false;
   }

   @Override
   public boolean useTwistForPelvisLinearStateEstimation()
   {
      return true;
   }

   @Override
   public double getPelvisLinearVelocityAlphaNewTwist()
   {
      return 0.15;
   }

   @Override
   public boolean createFusedIMUSensor()
   {
      return false;
   }
}
