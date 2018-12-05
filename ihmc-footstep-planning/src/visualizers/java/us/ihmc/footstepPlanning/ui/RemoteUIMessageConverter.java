package us.ihmc.footstepPlanning.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import controller_msgs.msg.dds.BodyPathPlanMessage;
import controller_msgs.msg.dds.BodyPathPlanStatisticsMessage;
import controller_msgs.msg.dds.FootstepDataListMessage;
import controller_msgs.msg.dds.FootstepDataMessage;
import controller_msgs.msg.dds.FootstepNodeDataListMessage;
import controller_msgs.msg.dds.FootstepPlannerOccupancyMapMessage;
import controller_msgs.msg.dds.FootstepPlannerParametersPacket;
import controller_msgs.msg.dds.FootstepPlannerStatusMessage;
import controller_msgs.msg.dds.FootstepPlanningRequestPacket;
import controller_msgs.msg.dds.FootstepPlanningToolboxOutputStatus;
import controller_msgs.msg.dds.PlanarRegionsListMessage;
import controller_msgs.msg.dds.PlanningStatisticsRequestMessage;
import controller_msgs.msg.dds.ToolboxStateMessage;
import controller_msgs.msg.dds.VisibilityGraphsParametersPacket;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.communication.IHMCRealtimeROS2Publisher;
import us.ihmc.communication.ROS2Tools;
import us.ihmc.communication.packets.MessageTools;
import us.ihmc.communication.packets.PlanarRegionMessageConverter;
import us.ihmc.communication.packets.ToolboxState;
import us.ihmc.euclid.geometry.ConvexPolygon2D;
import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.referenceFrame.FramePose3D;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.footstepPlanning.FootstepPlan;
import us.ihmc.footstepPlanning.FootstepPlannerStatus;
import us.ihmc.footstepPlanning.FootstepPlannerType;
import us.ihmc.footstepPlanning.FootstepPlanningResult;
import us.ihmc.footstepPlanning.SimpleFootstep;
import us.ihmc.footstepPlanning.VisibilityGraphMessagesConverter;
import us.ihmc.footstepPlanning.communication.FootstepPlannerCommunicationProperties;
import us.ihmc.footstepPlanning.communication.FootstepPlannerMessagerAPI;
import us.ihmc.footstepPlanning.graphSearch.parameters.FootstepPlannerCostParameters;
import us.ihmc.footstepPlanning.graphSearch.parameters.FootstepPlannerParameters;
import us.ihmc.idl.IDLSequence;
import us.ihmc.messager.Messager;
import us.ihmc.pathPlanning.visibilityGraphs.dataStructure.VisibilityMapWithNavigableRegion;
import us.ihmc.pathPlanning.visibilityGraphs.interfaces.VisibilityGraphsParameters;
import us.ihmc.pathPlanning.visibilityGraphs.interfaces.VisibilityMapHolder;
import us.ihmc.pubsub.DomainFactory;
import us.ihmc.robotEnvironmentAwareness.communication.REACommunicationProperties;
import us.ihmc.robotics.geometry.PlanarRegionsList;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.ros2.RealtimeRos2Node;

/**
 * This class is required when using a local version of the Footstep Planner UI and the footstep planning algorithms
 * located in the Footstep Planner Toolbox. It allows users to view the resulting plans calculated by the toolbox. It
 * also allows the user to tune the planner parameters, and request a new plan from the planning toolbox.
 *
 * This class is used to convert the shared memory messages from Java FX used by the Footstep Planner UI
 * to RTPS messages, and vice-versa. It specifically listens to the planning request messages and the output
 * status messages sent to and from the toolbox, and visualizes those. It also allows the user to request a
 * new plan using the local version of the edit footstep planning parameters and a footstep request, requiring
 * conversion from the Java FX messages to the RTPS messages.
 */
public class RemoteUIMessageConverter
{
   private static final boolean verbose = false;

   private final RealtimeRos2Node ros2Node;

   private final Messager messager;

   private final String robotName;

   private final AtomicReference<FootstepPlannerParameters> plannerParametersReference;
   private final AtomicReference<VisibilityGraphsParameters> visibilityGraphParametersReference;
   private final AtomicReference<Point3D> plannerStartPositionReference;
   private final AtomicReference<Quaternion> plannerStartOrientationReference;
   private final AtomicReference<Point3D> plannerGoalPositionReference;
   private final AtomicReference<Quaternion> plannerGoalOrientationReference;
   private final AtomicReference<PlanarRegionsList> plannerPlanarRegionReference;
   private final AtomicReference<FootstepPlannerType> plannerTypeReference;
   private final AtomicReference<Double> plannerTimeoutReference;
   private final AtomicReference<RobotSide> plannerInitialSupportSideReference;
   private final AtomicReference<Integer> plannerRequestIdReference;
   private final AtomicReference<Double> plannerHorizonLengthReference;
   private final AtomicReference<Boolean> acceptNewPlanarRegionsReference;
   private final AtomicReference<Integer> currentPlanRequestId;


   private IHMCRealtimeROS2Publisher<ToolboxStateMessage> toolboxStatePublisher;
   private IHMCRealtimeROS2Publisher<FootstepPlannerParametersPacket> plannerParametersPublisher;
   private IHMCRealtimeROS2Publisher<VisibilityGraphsParametersPacket> visibilityGraphsParametersPublisher;
   private IHMCRealtimeROS2Publisher<FootstepPlanningRequestPacket> footstepPlanningRequestPublisher;
   private IHMCRealtimeROS2Publisher<PlanningStatisticsRequestMessage> plannerStatisticsRequestPublisher;

   public static RemoteUIMessageConverter createRemoteConverter(Messager messager, String robotName)
   {
      return createConverter(messager, robotName, DomainFactory.PubSubImplementation.FAST_RTPS);
   }

   public static RemoteUIMessageConverter createIntraprocessConverter(Messager messager, String robotName)
   {
      return createConverter(messager, robotName, DomainFactory.PubSubImplementation.INTRAPROCESS);
   }

   public static RemoteUIMessageConverter createConverter(Messager messager, String robotName, DomainFactory.PubSubImplementation implementation)
   {
      RealtimeRos2Node ros2Node = ROS2Tools.createRealtimeRos2Node(implementation, "ihmc_footstep_planner_ui");
      return new RemoteUIMessageConverter(ros2Node, messager, robotName);
   }

   public RemoteUIMessageConverter(RealtimeRos2Node ros2Node, Messager messager, String robotName)
   {
      this.messager = messager;
      this.robotName = robotName;
      this.ros2Node = ros2Node;

      plannerParametersReference = messager.createInput(FootstepPlannerMessagerAPI.PlannerParametersTopic, null);
      visibilityGraphParametersReference = messager.createInput(FootstepPlannerMessagerAPI.VisibilityGraphsParametersTopic, null);
      plannerStartPositionReference = messager.createInput(FootstepPlannerMessagerAPI.StartPositionTopic);
      plannerStartOrientationReference = messager.createInput(FootstepPlannerMessagerAPI.StartOrientationTopic, new Quaternion());
      plannerGoalPositionReference = messager.createInput(FootstepPlannerMessagerAPI.GoalPositionTopic);
      plannerGoalOrientationReference = messager.createInput(FootstepPlannerMessagerAPI.GoalOrientationTopic, new Quaternion());
      plannerPlanarRegionReference = messager.createInput(FootstepPlannerMessagerAPI.PlanarRegionDataTopic);
      plannerTypeReference = messager.createInput(FootstepPlannerMessagerAPI.PlannerTypeTopic, FootstepPlannerType.A_STAR);
      plannerTimeoutReference = messager.createInput(FootstepPlannerMessagerAPI.PlannerTimeoutTopic, 5.0);
      plannerInitialSupportSideReference = messager.createInput(FootstepPlannerMessagerAPI.InitialSupportSideTopic, RobotSide.LEFT);
      plannerRequestIdReference = messager.createInput(FootstepPlannerMessagerAPI.PlannerRequestIdTopic);
      plannerHorizonLengthReference = messager.createInput(FootstepPlannerMessagerAPI.PlannerHorizonLengthTopic);
      acceptNewPlanarRegionsReference = messager.createInput(FootstepPlannerMessagerAPI.AcceptNewPlanarRegions, true);
      currentPlanRequestId = messager.createInput(FootstepPlannerMessagerAPI.PlannerRequestIdTopic, 0);

      registerPubSubs(ros2Node);

      ros2Node.spin();
   }

   public void destroy()
   {
      ros2Node.destroy();
   }

   private void registerPubSubs(RealtimeRos2Node ros2Node)
   {
      /* subscribers */
      // we want to listen to the incoming request to the planning toolbox
      ROS2Tools.createCallbackSubscription(ros2Node, FootstepPlanningRequestPacket.class,
                                           FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName),
                                           s -> processFootstepPlanningRequestPacket(s.takeNextData()));
      // we want to listen to the resulting body path plan from the toolbox
      ROS2Tools.createCallbackSubscription(ros2Node, BodyPathPlanMessage.class, FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> processBodyPathPlanMessage(s.takeNextData()));
      ROS2Tools.createCallbackSubscription(ros2Node, BodyPathPlanStatisticsMessage.class,
                                           FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> processBodyPathPlanStatistics(s.takeNextData()));
      ROS2Tools.createCallbackSubscription(ros2Node, FootstepPlannerStatusMessage.class,
                                           FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> processFootstepPlannerStatus(s.takeNextData()));
      // we want to listen to the resulting footstep plan from the toolbox
      ROS2Tools.createCallbackSubscription(ros2Node, FootstepPlanningToolboxOutputStatus.class,
                                           FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> processFootstepPlanningOutputStatus(s.takeNextData()));
      // we want to also listen to incoming REA planar region data.
      ROS2Tools.createCallbackSubscription(ros2Node, PlanarRegionsListMessage.class, REACommunicationProperties.publisherTopicNameGenerator,
                                           s -> processIncomingPlanarRegionMessage(s.takeNextData()));
      ROS2Tools.createCallbackSubscription(ros2Node, FootstepNodeDataListMessage.class,
                                           FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> messager.submitMessage(FootstepPlannerMessagerAPI.NodeDataTopic, s.takeNextData()));
      ROS2Tools.createCallbackSubscription(ros2Node, FootstepPlannerOccupancyMapMessage.class,
                                           FootstepPlannerCommunicationProperties.publisherTopicNameGenerator(robotName),
                                           s -> messager.submitMessage(FootstepPlannerMessagerAPI.OccupancyMapTopic, s.takeNextData()));

      // publishers
      plannerParametersPublisher = ROS2Tools
            .createPublisher(ros2Node, FootstepPlannerParametersPacket.class, FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName));
      visibilityGraphsParametersPublisher = ROS2Tools
            .createPublisher(ros2Node, VisibilityGraphsParametersPacket.class, FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName));
      toolboxStatePublisher = ROS2Tools.createPublisher(ros2Node, ToolboxStateMessage.class,
                                                        FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName));
      footstepPlanningRequestPublisher = ROS2Tools
            .createPublisher(ros2Node, FootstepPlanningRequestPacket.class, FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName));
      plannerStatisticsRequestPublisher = ROS2Tools
            .createPublisher(ros2Node, PlanningStatisticsRequestMessage.class, FootstepPlannerCommunicationProperties.subscriberTopicNameGenerator(robotName));

      messager.registerTopicListener(FootstepPlannerMessagerAPI.ComputePathTopic, request -> requestNewPlan());
      messager.registerTopicListener(FootstepPlannerMessagerAPI.RequestPlannerStatistics, request -> requestPlannerStatistics());
      messager.registerTopicListener(FootstepPlannerMessagerAPI.AbortPlanningTopic, request -> requestAbortPlanning());
   }

   private void processFootstepPlanningRequestPacket(FootstepPlanningRequestPacket packet)
   {
      if (verbose)
         PrintTools.info("Received a planning request.");

      Point3D goalPosition = packet.getGoalPositionInWorld();
      Quaternion goalOrientation = packet.getGoalOrientationInWorld();
      Point3D startPosition = packet.getStanceFootPositionInWorld();
      Quaternion startOrientation = packet.getStanceFootOrientationInWorld();
      FootstepPlannerType plannerType = FootstepPlannerType.fromByte(packet.getRequestedFootstepPlannerType());
      RobotSide initialSupportSide = RobotSide.fromByte(packet.getInitialStanceRobotSide());
      int plannerRequestId = packet.getPlannerRequestId();

      double timeout = packet.getTimeout();
      double horizonLength = packet.getHorizonLength();

      messager.submitMessage(FootstepPlannerMessagerAPI.StartPositionTopic, startPosition);
      messager.submitMessage(FootstepPlannerMessagerAPI.GoalPositionTopic, goalPosition);

      messager.submitMessage(FootstepPlannerMessagerAPI.StartOrientationTopic, startOrientation);
      messager.submitMessage(FootstepPlannerMessagerAPI.GoalOrientationTopic, goalOrientation);

      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerTypeTopic, plannerType);

      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerTimeoutTopic, timeout);
      messager.submitMessage(FootstepPlannerMessagerAPI.InitialSupportSideTopic, initialSupportSide);

      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerRequestIdTopic, plannerRequestId);

      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerHorizonLengthTopic, horizonLength);
   }

   private void processBodyPathPlanMessage(BodyPathPlanMessage packet)
   {
      PlanarRegionsListMessage planarRegionsListMessage = packet.getPlanarRegionsList();
      PlanarRegionsList planarRegionsList = PlanarRegionMessageConverter.convertToPlanarRegionsList(planarRegionsListMessage);
      FootstepPlanningResult result = FootstepPlanningResult.fromByte(packet.getFootstepPlanningResult());
      List<? extends Point3DReadOnly> bodyPath = packet.getBodyPath();

      messager.submitMessage(FootstepPlannerMessagerAPI.PlanarRegionDataTopic, planarRegionsList);
      messager.submitMessage(FootstepPlannerMessagerAPI.PlanningResultTopic, result);
      messager.submitMessage(FootstepPlannerMessagerAPI.BodyPathDataTopic, bodyPath);

      if (verbose)
         PrintTools.info("Received a body path planning result from the toolbox.");
   }

   private void processBodyPathPlanStatistics(BodyPathPlanStatisticsMessage packet)
   {
      VisibilityMapHolder startVisibilityMap = VisibilityGraphMessagesConverter.convertToSingleSourceVisibilityMap(packet.getStartVisibilityMap());
      VisibilityMapHolder goalVisibilityMap = VisibilityGraphMessagesConverter.convertToSingleSourceVisibilityMap(packet.getGoalVisibilityMap());
      VisibilityMapHolder interRegionVisibilityMap = VisibilityGraphMessagesConverter.convertToInterRegionsVisibilityMap(packet.getInterRegionsMap());

      List<VisibilityMapWithNavigableRegion> navigableRegionList = VisibilityGraphMessagesConverter.convertToNavigableRegionsList(packet.getNavigableRegions());

      messager.submitMessage(FootstepPlannerMessagerAPI.StartVisibilityMap, startVisibilityMap);
      messager.submitMessage(FootstepPlannerMessagerAPI.GoalVisibilityMap, goalVisibilityMap);
      messager.submitMessage(FootstepPlannerMessagerAPI.NavigableRegionData, navigableRegionList);
      messager.submitMessage(FootstepPlannerMessagerAPI.InterRegionVisibilityMap, interRegionVisibilityMap);
   }

   private void processFootstepPlannerStatus(FootstepPlannerStatusMessage packet)
   {
      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerStatusTopic, FootstepPlannerStatus.fromByte(packet.getFootstepPlannerStatus()));
   }

   private void processFootstepPlanningOutputStatus(FootstepPlanningToolboxOutputStatus packet)
   {
      FootstepDataListMessage footstepDataListMessage = packet.getFootstepDataList();
      int plannerRequestId = packet.getPlanId();
      FootstepPlanningResult result = FootstepPlanningResult.fromByte(packet.getFootstepPlanningResult());
      FootstepPlan footstepPlan = convertToFootstepPlan(footstepDataListMessage);
      List<? extends Point3DReadOnly> bodyPath = packet.getBodyPath();
      Pose3D lowLevelGoal = packet.getLowLevelPlannerGoal();

      if (plannerRequestId > currentPlanRequestId.get())
         messager.submitMessage(FootstepPlannerMessagerAPI.PlannerRequestIdTopic, plannerRequestId);
     
      ThreadTools.sleep(100);

      messager.submitMessage(FootstepPlannerMessagerAPI.FootstepPlanTopic, footstepPlan);
      messager.submitMessage(FootstepPlannerMessagerAPI.ReceivedPlanIdTopic, plannerRequestId);
      messager.submitMessage(FootstepPlannerMessagerAPI.PlanningResultTopic, result);
      messager.submitMessage(FootstepPlannerMessagerAPI.PlannerTimeTakenTopic, packet.getTimeTaken());
      messager.submitMessage(FootstepPlannerMessagerAPI.BodyPathDataTopic, bodyPath);
      if (lowLevelGoal != null)
      {
         messager.submitMessage(FootstepPlannerMessagerAPI.LowLevelGoalPositionTopic, lowLevelGoal.getPosition());
         messager.submitMessage(FootstepPlannerMessagerAPI.LowLevelGoalOrientationTopic, lowLevelGoal.getOrientation());
      }

      if (verbose)
         PrintTools.info("Received a footstep planning result from the toolbox.");
   }

   private void processIncomingPlanarRegionMessage(PlanarRegionsListMessage packet)
   {
      if (acceptNewPlanarRegionsReference.get())
      {
         messager.submitMessage(FootstepPlannerMessagerAPI.PlanarRegionDataTopic, PlanarRegionMessageConverter.convertToPlanarRegionsList(packet));

         if (verbose)
            PrintTools.info("Received updated planner regions.");
      }
   }

   private void requestNewPlan()
   {
      toolboxStatePublisher.publish(MessageTools.createToolboxStateMessage(ToolboxState.WAKE_UP));

      if (verbose)
         PrintTools.info("Told the toolbox to wake up.");
      
      FootstepPlannerParametersPacket plannerParametersPacket = new FootstepPlannerParametersPacket();
      FootstepPlannerParameters footstepPlannerParameters = plannerParametersReference.get();

      copyFootstepPlannerParametersToPacket(plannerParametersPacket, footstepPlannerParameters);
      plannerParametersPublisher.publish(plannerParametersPacket);

      VisibilityGraphsParametersPacket visibilityGraphsParametersPacket = new VisibilityGraphsParametersPacket();
      VisibilityGraphsParameters visibilityGraphsParameters = visibilityGraphParametersReference.get();

      copyVisibilityGraphsParametersToPacket(visibilityGraphsParametersPacket, visibilityGraphsParameters);
      visibilityGraphsParametersPublisher.publish(visibilityGraphsParametersPacket);
      
      if (verbose)
         PrintTools.info("Sent out some parameters");

      submitFootstepPlanningRequestPacket();
   }

   private void requestPlannerStatistics()
   {
      plannerStatisticsRequestPublisher.publish(new PlanningStatisticsRequestMessage());
   }

   private void requestAbortPlanning()
   {
      if (verbose)
         PrintTools.info("Sending out a sleep request.");
      toolboxStatePublisher.publish(MessageTools.createToolboxStateMessage(ToolboxState.SLEEP));
   }

   public static void copyFootstepPlannerParametersToPacket(FootstepPlannerParametersPacket packet, FootstepPlannerParameters parameters)
   {
      if (parameters == null)
      {
         return;
      }

      packet.setCheckForBodyBoxCollisions(parameters.checkForBodyBoxCollisions());
      packet.setPerformHeuristicSearchPolicies(parameters.performHeuristicSearchPolicies());
      packet.setIdealFootstepWidth(parameters.getIdealFootstepWidth());
      packet.setIdealFootstepLength(parameters.getIdealFootstepLength());
      packet.setWiggleInsideDelta(parameters.getWiggleInsideDelta());
      packet.setMaximumStepReach(parameters.getMaximumStepReach());
      packet.setMaximumStepYaw(parameters.getMaximumStepYaw());
      packet.setMinimumStepWidth(parameters.getMinimumStepWidth());
      packet.setMinimumStepLength(parameters.getMinimumStepLength());
      packet.setMinimumStepYaw(parameters.getMinimumStepYaw());
      packet.setMaximumStepReachWhenSteppingUp(parameters.getMaximumStepReachWhenSteppingUp());
      packet.setMaximumStepZWhenSteppingUp(parameters.getMaximumStepZWhenSteppingUp());
      packet.setMaximumStepXWhenForwardAndDown(parameters.getMaximumStepXWhenForwardAndDown());
      packet.setMaximumStepZWhenForwardAndDown(parameters.getMaximumStepZWhenForwardAndDown());
      packet.setMaximumStepZ(parameters.getMaximumStepZ());
      packet.setMinimumFootholdPercent(parameters.getMinimumFootholdPercent());
      packet.setMinimumSurfaceInclineRadians(parameters.getMinimumSurfaceInclineRadians());
      packet.setWiggleIntoConvexHullOfPlanarRegions(parameters.getWiggleIntoConvexHullOfPlanarRegions());
      packet.setRejectIfCannotFullyWiggleInside(parameters.getRejectIfCannotFullyWiggleInside());
      packet.setMaximumXyWiggleDistance(parameters.getMaximumXYWiggleDistance());
      packet.setMaximumYawWiggle(parameters.getMaximumYawWiggle());
      packet.setMaximumZPenetrationOnValleyRegions(parameters.getMaximumZPenetrationOnValleyRegions());
      packet.setMaximumStepWidth(parameters.getMaximumStepWidth());
      packet.setMinimumDistanceFromCliffBottoms(parameters.getMinimumDistanceFromCliffBottoms());
      packet.setCliffHeightToAvoid(parameters.getCliffHeightToAvoid());
      packet.setReturnBestEffortPlan(parameters.getReturnBestEffortPlan());
      packet.setMinimumStepsForBestEffortPlan(parameters.getMinimumStepsForBestEffortPlan());
      packet.setBodyGroundClearance(parameters.getBodyGroundClearance());
      packet.setBodyBoxHeight(parameters.getBodyBoxHeight());
      packet.setBodyBoxDepth(parameters.getBodyBoxDepth());
      packet.setBodyBoxWidth(parameters.getBodyBoxWidth());
      packet.setBodyBoxBaseX(parameters.getBodyBoxBaseX());
      packet.setBodyBoxBaseY(parameters.getBodyBoxBaseY());
      packet.setBodyBoxBaseZ(parameters.getBodyBoxBaseZ());
      packet.setMinXClearanceFromStance(parameters.getMinXClearanceFromStance());
      packet.setMinYClearanceFromStance(parameters.getMinYClearanceFromStance());

      FootstepPlannerCostParameters costParameters = parameters.getCostParameters();

      packet.getCostParameters().setUseQuadraticDistanceCost(costParameters.useQuadraticDistanceCost());
      packet.getCostParameters().setUseQuadraticHeightCost(costParameters.useQuadraticHeightCost());

      packet.getCostParameters().setAStarHeuristicsWeight(costParameters.getAStarHeuristicsWeight().getValue());
      packet.getCostParameters().setVisGraphWithAStarHeuristicsWeight(costParameters.getVisGraphWithAStarHeuristicsWeight().getValue());
      packet.getCostParameters().setDepthFirstHeuristicsWeight(costParameters.getDepthFirstHeuristicsWeight().getValue());
      packet.getCostParameters().setBodyPathBasedHeuristicsWeight(costParameters.getBodyPathBasedHeuristicsWeight().getValue());

      packet.getCostParameters().setYawWeight(costParameters.getYawWeight());
      packet.getCostParameters().setPitchWeight(costParameters.getPitchWeight());
      packet.getCostParameters().setRollWeight(costParameters.getRollWeight());
      packet.getCostParameters().setStepUpWeight(costParameters.getStepUpWeight());
      packet.getCostParameters().setStepDownWeight(costParameters.getStepDownWeight());
      packet.getCostParameters().setForwardWeight(costParameters.getForwardWeight());
      packet.getCostParameters().setLateralWeight(costParameters.getLateralWeight());
      packet.getCostParameters().setCostPerStep(costParameters.getCostPerStep());
   }

   public static void copyVisibilityGraphsParametersToPacket(VisibilityGraphsParametersPacket packet, VisibilityGraphsParameters parameters)
   {
      if (parameters == null)
      {
         return;
      }

      packet.setMaxInterRegionConnectionLength(parameters.getMaxInterRegionConnectionLength());
      packet.setNormalZThresholdForAccessibleRegions(parameters.getNormalZThresholdForAccessibleRegions());
      packet.setExtrusionDistance(parameters.getExtrusionDistance());
      packet.setExtrusionDistanceIfNotTooHighToStep(parameters.getExtrusionDistanceIfNotTooHighToStep());
      packet.setTooHighToStepDistance(parameters.getTooHighToStepDistance());
      packet.setClusterResolution(parameters.getClusterResolution());
      packet.setExplorationDistanceFromStartGoal(parameters.getExplorationDistanceFromStartGoal());
      packet.setPlanarRegionMinArea(parameters.getPlanarRegionMinArea());
      packet.setPlanarRegionMinSize(parameters.getPlanarRegionMinSize());
      packet.setRegionOrthogonalAngle(parameters.getRegionOrthogonalAngle());
      packet.setSearchHostRegionEpsilon(parameters.getSearchHostRegionEpsilon());
   }

   private void submitFootstepPlanningRequestPacket()
   {
      FootstepPlanningRequestPacket packet = new FootstepPlanningRequestPacket();
      packet.getStanceFootPositionInWorld().set(plannerStartPositionReference.get());
      packet.getStanceFootOrientationInWorld().set(plannerStartOrientationReference.get());
      packet.getGoalPositionInWorld().set(plannerGoalPositionReference.get());
      packet.getGoalOrientationInWorld().set(plannerGoalOrientationReference.get());
      if (plannerInitialSupportSideReference.get() != null)
         packet.setInitialStanceRobotSide(plannerInitialSupportSideReference.get().toByte());
      if (plannerTimeoutReference.get() != null)
         packet.setTimeout(plannerTimeoutReference.get());
      if (plannerTypeReference.get() != null)
         packet.setRequestedFootstepPlannerType(plannerTypeReference.get().toByte());
      if (plannerRequestIdReference.get() != null)
         packet.setPlannerRequestId(plannerRequestIdReference.get());
      if (plannerHorizonLengthReference.get() != null)
         packet.setHorizonLength(plannerHorizonLengthReference.get());
      if (plannerPlanarRegionReference.get() != null)
         packet.getPlanarRegionsListMessage().set(PlanarRegionMessageConverter.convertToPlanarRegionsListMessage(plannerPlanarRegionReference.get()));

      footstepPlanningRequestPublisher.publish(packet);
   }

   private static FootstepPlan convertToFootstepPlan(FootstepDataListMessage footstepDataListMessage)
   {
      FootstepPlan footstepPlan = new FootstepPlan();

      for (FootstepDataMessage footstepMessage : footstepDataListMessage.getFootstepDataList())
      {
         FramePose3D stepPose = new FramePose3D();
         stepPose.setPosition(footstepMessage.getLocation());
         stepPose.setOrientation(footstepMessage.getOrientation());
         SimpleFootstep simpleFootstep = footstepPlan.addFootstep(RobotSide.fromByte(footstepMessage.getRobotSide()), stepPose);

         IDLSequence.Object<Point3D> predictedContactPoints = footstepMessage.getPredictedContactPoints2d();
         if (!predictedContactPoints.isEmpty())
         {
            ConvexPolygon2D foothold = new ConvexPolygon2D();
            for (int i = 0; i < predictedContactPoints.size(); i++)
            {
               foothold.addVertex(predictedContactPoints.get(i));
            }
            foothold.update();
            simpleFootstep.setFoothold(foothold);
         }
      }

      return footstepPlan;
   }
}
