package us.ihmc.avatar.footstepPlanning;

import controller_msgs.msg.dds.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.opencv.core.Point3;
import us.ihmc.affinity.CPUTopology;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.communication.IHMCRealtimeROS2Publisher;
import us.ihmc.communication.controllerAPI.StatusMessageOutputManager;
import us.ihmc.communication.packets.MessageTools;
import us.ihmc.communication.packets.PlanarRegionMessageConverter;
import us.ihmc.euclid.geometry.Pose2D;
import us.ihmc.euclid.referenceFrame.FramePoint2D;
import us.ihmc.euclid.referenceFrame.FramePose3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple2D.Vector2D;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.footstepPlanning.*;
import us.ihmc.footstepPlanning.graphSearch.graph.visualization.MultiStagePlannerListener;
import us.ihmc.footstepPlanning.graphSearch.parameters.FootstepPlannerParameters;
import us.ihmc.footstepPlanning.graphSearch.parameters.YoFootstepPlannerParameters;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsListRegistry;
import us.ihmc.pathPlanning.bodyPathPlanner.WaypointDefinedBodyPathPlanner;
import us.ihmc.pathPlanning.statistics.ListOfStatistics;
import us.ihmc.pathPlanning.statistics.PlannerStatistics;
import us.ihmc.pathPlanning.statistics.StatisticsType;
import us.ihmc.pathPlanning.statistics.VisibilityGraphStatistics;
import us.ihmc.pathPlanning.visibilityGraphs.YoVisibilityGraphParameters;
import us.ihmc.pathPlanning.visibilityGraphs.interfaces.VisibilityGraphsParameters;
import us.ihmc.pathPlanning.visibilityGraphs.tools.BodyPathPlan;
import us.ihmc.robotics.geometry.PlanarRegionsList;
import us.ihmc.robotics.graphics.YoGraphicPlanarRegionsList;
import us.ihmc.robotics.referenceFrames.PoseReferenceFrame;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.tools.lists.PairList;
import us.ihmc.wholeBodyController.RobotContactPointParameters;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MultiStageFootstepPlanningManager implements PlannerCompletionCallback
{
   private static final boolean debug = true;

   private static final int initialNumberOfPathStages = 1;
   private static final int initialNumberOfStepStages = 2;

   private static final int absoluteMaxNumberOfPathStages = 4;
   private static final int absoluteMaxNumberOfStepStages = 4;

   private final YoVariableRegistry registry = new YoVariableRegistry(getClass().getSimpleName());

   private final YoEnum<FootstepPlannerType> activePlanner = new YoEnum<>("activePlanner", registry, FootstepPlannerType.class);

   private final AtomicReference<FootstepPlanningRequestPacket> latestRequestReference = new AtomicReference<>(null);
   private final AtomicReference<FootstepPlannerParametersPacket> latestFootstepPlannerParametersReference = new AtomicReference<>(null);
   private final AtomicReference<VisibilityGraphsParametersPacket> latestVisibilityGraphsParametersReference = new AtomicReference<>(null);

   private final AtomicReference<List<Point3D>> waypointPlan = new AtomicReference<>(null);
   private final AtomicReference<BodyPathPlan> bodyPathPlan = new AtomicReference<>(null);
   private final AtomicReference<FootstepPlan> footstepPlan = new AtomicReference<>(null);

   private Optional<PlanarRegionsList> planarRegionsList = Optional.empty();

   private final YoBoolean isDone = new YoBoolean("isDone", registry);
   private final YoBoolean requestedPlanarRegions = new YoBoolean("RequestedPlanarRegions", registry);
   private final YoDouble plannerTime = new YoDouble("PlannerTime", registry);
   private final YoDouble timeSpentBeforeFootstepPlanner = new YoDouble("timeSpentBeforeFootstepPlanner", registry);
   private final YoDouble timeSpentInFootstepPlanner = new YoDouble("timeSpentInFootstepPlanner", registry);
   private final YoDouble timeout = new YoDouble("PlannerTimeout", registry);
   private final YoInteger planId = new YoInteger("planId", registry);
   private final YoInteger globalPathSequenceIndex = new YoInteger("globalPathSequenceIndex", registry);
   private final YoInteger globalStepSequenceIndex = new YoInteger("globalStepSequenceIndex", registry);
   private final YoDouble planningHorizonLength = new YoDouble("planningHorizonLength", registry);
   private final YoEnum<FootstepPlanningResult> pathResult = new YoEnum<>("pathPlanningResult", registry, FootstepPlanningResult.class);
   private final YoEnum<FootstepPlanningResult> stepResult = new YoEnum<>("stepResult", registry, FootstepPlanningResult.class);

   private final YoGraphicPlanarRegionsList yoGraphicPlanarRegionsList;

   private final List<FootstepPlannerObjective> pathPlanningObjectivePool = new ArrayList<>();

   private final YoBoolean isDonePlanningPath = new YoBoolean("isDonePlanningPath", registry);

   private FootstepPlannerObjective mainObjective;

   private final ConcurrentList<PathPlanningStage> pathPlanningStagePool = new ConcurrentList<>();
   private final ConcurrentList<PathPlanningStage> allPathPlanningStages = new ConcurrentList<>();
   private final ConcurrentMap<PathPlanningStage, FootstepPlannerObjective> pathPlanningStagesInProgress = new ConcurrentMap<>();
   private final ConcurrentMap<PathPlanningStage, ScheduledFuture<?>> pathPlanningTasks = new ConcurrentMap<>();

   private final ConcurrentList<FootstepPlanningResult> completedPathResults = new ConcurrentList<>();
   private final ConcurrentPairList<Integer, List<Point3D>> completedPathWaypoints = new ConcurrentPairList<>();

   private final ConcurrentPairList<Integer, PlannerStatistics<?>> completedPathPlanStatistics = new ConcurrentPairList<>();

   private final PlannerGoalRecommendationHandler goalRecommendationHandler;
   private final List<FootstepPlannerObjective> stepPlanningObjectivePool = new ArrayList<>();

   private final YoBoolean isDonePlanningSteps = new YoBoolean("isDonePlanningSteps", registry);

   private final ConcurrentList<FootstepPlanningStage> stepPlanningStagePool = new ConcurrentList<>();
   private final ConcurrentList<FootstepPlanningStage> allStepPlanningStages = new ConcurrentList<>();
   private final ConcurrentMap<FootstepPlanningStage, FootstepPlannerObjective> stepPlanningStagesInProgress = new ConcurrentMap<>();
   private final ConcurrentMap<FootstepPlanningStage, ScheduledFuture<?>> stepPlanningTasks = new ConcurrentMap<>();

   private final ConcurrentList<FootstepPlanningResult> completedStepResults = new ConcurrentList<>();
   private final ConcurrentPairList<Integer, FootstepPlan> completedStepPlans = new ConcurrentPairList<>();

   private final ConcurrentPairList<Integer, PlannerStatistics<?>> completedStepPlanStatistics = new ConcurrentPairList<>();

   private long tickDurationMs;

   private final YoFootstepPlannerParameters footstepPlanningParameters;
   private final YoVisibilityGraphParameters visibilityGraphsParameters;
   private IHMCRealtimeROS2Publisher<TextToSpeechPacket> textToSpeechPublisher;

   private final RobotContactPointParameters<RobotSide> contactPointParameters;

   protected final WaypointDefinedBodyPathPlanner bodyPathPlanner = new WaypointDefinedBodyPathPlanner();

   private final StatusMessageOutputManager statusOutputManager;
   private final ScheduledExecutorService executorService;
   private final YoBoolean initialize = new YoBoolean("initialize" + registry.getName(), registry);

   private final MultiStagePlannerListener plannerListener;

   private final int maxNumberOfPathPlanners;
   private final int maxNumberOfStepPlanners;

   public MultiStageFootstepPlanningManager(RobotContactPointParameters<RobotSide> contactPointParameters, FootstepPlannerParameters footstepPlannerParameters,
                                            VisibilityGraphsParameters visibilityGraphsParameters, StatusMessageOutputManager statusOutputManager,
                                            YoVariableRegistry parentRegistry, YoGraphicsListRegistry graphicsListRegistry, long tickDurationMs)
   {
      this.contactPointParameters = contactPointParameters;
      this.statusOutputManager = statusOutputManager;
      this.tickDurationMs = tickDurationMs;
      this.yoGraphicPlanarRegionsList = new YoGraphicPlanarRegionsList("FootstepPlannerPlanarRegions", 200, 30, registry);
      goalRecommendationHandler = new PlannerGoalRecommendationHandler(allStepPlanningStages, stepPlanningStagesInProgress);

      CPUTopology topology = new CPUTopology();
      int numberOfCores = topology.getNumberOfCores();

      ThreadFactory threadFactory = ThreadTools.getNamedThreadFactory(getClass().getSimpleName());
      executorService = Executors.newScheduledThreadPool(numberOfCores, threadFactory);

      this.footstepPlanningParameters = new YoFootstepPlannerParameters(registry, footstepPlannerParameters);
      this.visibilityGraphsParameters = new YoVisibilityGraphParameters(visibilityGraphsParameters, registry);

      activePlanner.set(FootstepPlannerType.PLANAR_REGION_BIPEDAL);

      graphicsListRegistry.registerYoGraphic("footstepPlanning", yoGraphicPlanarRegionsList);
      isDone.set(false);
      planId.set(FootstepPlanningRequestPacket.NO_PLAN_ID);

      for (int i = 0; i < initialNumberOfPathStages; i++)
      {
         PathPlanningStage pathPlanningStage = createNewPathPlanningStage();
         registry.addChild(pathPlanningStage.getYoVariableRegistry());
         allPathPlanningStages.add(pathPlanningStage);
         pathPlanningStagePool.add(pathPlanningStage);
      }
      maxNumberOfPathPlanners = Math.max(initialNumberOfPathStages, absoluteMaxNumberOfPathStages);

      long updateFrequency = 1000;
      plannerListener = new MultiStagePlannerListener(statusOutputManager, updateFrequency);

      for (int i = 0; i < Math.min(initialNumberOfStepStages, absoluteMaxNumberOfStepStages); i++)
      {
         FootstepPlanningStage stepPlanningStage = createNewStepPlanningStage();
         registry.addChild(stepPlanningStage.getYoVariableRegistry());
         allStepPlanningStages.add(stepPlanningStage);
         stepPlanningStagePool.add(stepPlanningStage);
      }
      maxNumberOfStepPlanners = Math.max(initialNumberOfStepStages, Math.min(numberOfCores, absoluteMaxNumberOfStepStages));

      isDonePlanningPath.set(false);
      isDonePlanningSteps.set(false);

      isDonePlanningPath.addVariableChangedListener(v -> {
         if (!isDonePlanningPath.getBooleanValue())
            statusOutputManager.reportStatusMessage(FootstepPlanningMessageReporter.packStatus(FootstepPlannerStatus.PLANNING_PATH));
         else
            statusOutputManager.reportStatusMessage(FootstepPlanningMessageReporter.packStatus(FootstepPlannerStatus.PLANNING_STEPS));
      });
      isDonePlanningSteps.addVariableChangedListener(v -> {
         if (isDonePlanningSteps.getBooleanValue())
            statusOutputManager.reportStatusMessage(FootstepPlanningMessageReporter.packStatus(FootstepPlannerStatus.IDLE));
      });

      parentRegistry.addChild(registry);
      initialize.set(true);
   }

   private PathPlanningStage createNewPathPlanningStage()
   {
      PathPlanningStage pathPlanningStage = new PathPlanningStage(allPathPlanningStages.size(), footstepPlanningParameters, visibilityGraphsParameters,
                                                                  activePlanner);
      pathPlanningStage.addCompletionCallback(this);
      return pathPlanningStage;
   }

   private FootstepPlanningStage createNewStepPlanningStage()
   {
      FootstepPlanningStage footstepPlanningStage = new FootstepPlanningStage(allStepPlanningStages.size(), contactPointParameters, footstepPlanningParameters,
                                                                              bodyPathPlanner, activePlanner, plannerListener, planId, tickDurationMs);
      footstepPlanningStage.addCompletionCallback(this);
      footstepPlanningStage.setPlannerGoalRecommendationHandler(goalRecommendationHandler);
      return footstepPlanningStage;
   }

   private PathPlanningStage spawnNextAvailablePathPlanner()
   {
      PathPlanningStage stageToReturn;

      if (pathPlanningStagePool.isEmpty() && allPathPlanningStages.getCopyForReading().size() < maxNumberOfPathPlanners)
      { // get one from the pool
         stageToReturn = createNewPathPlanningStage();
         allPathPlanningStages.add(stageToReturn);
      }
      else
      { // get one from the pool
         stageToReturn = pathPlanningStagePool.remove(0);
      }

      return stageToReturn;
   }

   private FootstepPlanningStage spawnNextAvailableFootstepPlanner()
   {
      FootstepPlanningStage stageToReturn;

      if (stepPlanningStagePool.isEmpty() && allStepPlanningStages.getCopyForReading().size() < maxNumberOfStepPlanners)
      { // create a new stage
         stageToReturn = createNewStepPlanningStage();
         allStepPlanningStages.add(stageToReturn);
      }
      else
      { // get one from the pool
         stageToReturn = stepPlanningStagePool.remove(0);
      }

      return stageToReturn;
   }

   private PathPlanningStage cleanupPathPlanningStage(PathPlanningStage planningStage)
   {
      pathPlanningStagesInProgress.remove(planningStage);
      planningStage.destroyStageRunnable();
      pathPlanningTasks.remove(planningStage).cancel(true);
      pathPlanningStagePool.add(planningStage);

      return planningStage;
   }

   private FootstepPlanningStage cleanupStepPlanningStage(FootstepPlanningStage planningStage)
   {
      stepPlanningStagesInProgress.remove(planningStage);
      planningStage.destroyStageRunnable();
      stepPlanningTasks.remove(planningStage).cancel(true);
      stepPlanningStagePool.add(planningStage);

      return planningStage;
   }

   private void cleanupAllPlanningStages()
   {
      globalStepSequenceIndex.set(0);

      if (!pathPlanningStagesInProgress.isEmpty())
      {
         for (PathPlanningStage pathPlanningStageInProgress : pathPlanningStagesInProgress.iterator())
         {
            cleanupPathPlanningStage(pathPlanningStageInProgress);
         }
      }

      if (!stepPlanningStagesInProgress.isEmpty())
      {
         for (FootstepPlanningStage stepPlanningStageInProgress : stepPlanningStagesInProgress.iterator())
         {
            cleanupStepPlanningStage(stepPlanningStageInProgress);
         }
      }

      completedPathWaypoints.clear();
      completedStepPlans.clear();

      completedPathPlanStatistics.clear();
      completedStepPlanStatistics.clear();

      completedPathResults.clear();
      completedStepResults.clear();

      pathPlanningObjectivePool.clear();
      stepPlanningObjectivePool.clear();
   }

   private void cancelAllActiveStages()
   {
      isDonePlanningPath.set(true);
      isDonePlanningSteps.set(true);
      isDone.set(true);

      if (!pathPlanningStagesInProgress.isEmpty())
      {
         for (PathPlanningStage stage : pathPlanningStagesInProgress.iterator())
         {
            stage.cancelPlanning();
         }
      }

      if (!stepPlanningStagesInProgress.isEmpty())
      {
         for (FootstepPlanningStage stage : stepPlanningStagesInProgress.iterator())
         {
            stage.cancelPlanning();
         }
      }
   }

   private void assignGoalsToAvailablePathPlanners()
   {
      if (pathPlanningObjectivePool.isEmpty())
         return;

      if (pathPlanningStagePool.isEmpty())
         return;

      while (!pathPlanningObjectivePool.isEmpty() && !pathPlanningStagePool.isEmpty())
      {
         PathPlanningStage planner = spawnNextAvailablePathPlanner();
         FootstepPlannerObjective plannerGoal = pathPlanningObjectivePool.remove(0);

         globalPathSequenceIndex.increment();
         planner.setFootstepPlannerObjective(plannerGoal);
         planner.setPlanSequenceId(globalPathSequenceIndex.getIntegerValue());

         PlanarRegionsList planarRegionsList;
         if (this.planarRegionsList.isPresent())
         {
            planarRegionsList = this.planarRegionsList.get();
            yoGraphicPlanarRegionsList.submitPlanarRegionsListToRender(planarRegionsList);
            yoGraphicPlanarRegionsList.processPlanarRegionsListQueue();
         }
         else
         {
            planarRegionsList = null;
            yoGraphicPlanarRegionsList.clear();
         }

         planner.setPlanarRegionsList(planarRegionsList);
         planner.requestInitialize();

         Runnable runnable = planner.createStageRunnable();
         ScheduledFuture<?> plannerTask = executorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
         pathPlanningTasks.put(planner, plannerTask);

         pathPlanningStagesInProgress.put(planner, plannerGoal);

         if (debug)
            PrintTools.info("Just started up planning path objective " + globalStepSequenceIndex.getIntegerValue() + " on stage " + planner.getStageId());
      }
   }

   private void assignGoalsToAvailableStepPlanners()
   {
      if (stepPlanningObjectivePool.isEmpty())
         return;

      if (stepPlanningStagePool.isEmpty())
         return;

      while (!stepPlanningObjectivePool.isEmpty() && !stepPlanningStagePool.isEmpty())
      {
         FootstepPlanningStage planner = spawnNextAvailableFootstepPlanner();
         FootstepPlannerObjective plannerGoal = stepPlanningObjectivePool.remove(0);

         globalStepSequenceIndex.increment();
         planner.setFootstepPlannerObjective(plannerGoal);
         planner.setPlanSequenceId(globalStepSequenceIndex.getIntegerValue());

         PlanarRegionsList planarRegionsList;
         if (this.planarRegionsList.isPresent())
         {
            planarRegionsList = this.planarRegionsList.get();
            yoGraphicPlanarRegionsList.submitPlanarRegionsListToRender(planarRegionsList);
            yoGraphicPlanarRegionsList.processPlanarRegionsListQueue();
         }
         else
         {
            planarRegionsList = null;
            yoGraphicPlanarRegionsList.clear();
         }

         planner.setPlanarRegions(planarRegionsList);
         planner.requestInitialize();

         Runnable runnable = planner.createStageRunnable();
         ScheduledFuture<?> plannerTask = executorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
         stepPlanningTasks.put(planner, plannerTask);

         stepPlanningStagesInProgress.put(planner, plannerGoal);

         if (debug)
            PrintTools.info("Just started up planning step objective " + globalStepSequenceIndex.getIntegerValue() + " on stage " + planner.getStageId());
      }
   }

   private void updateStepPlanningObjectives()
   {
      while (goalRecommendationHandler.hasNewFootstepPlannerObjectives())
         stepPlanningObjectivePool.add(goalRecommendationHandler.pollNextFootstepPlannerObjective());
   }

   @Override
   public void pathPlanningIsComplete(FootstepPlanningResult pathPlanningResult, PathPlanningStage stageFinished)
   {
      completedPathResults.add(pathPlanningResult);

      if (pathPlanningResult != null && pathPlanningResult.validForExecution())
      {
         completedPathWaypoints.add(stageFinished.getPlanSequenceId(), stageFinished.getWaypoints());
      }

      completedPathPlanStatistics.add(stageFinished.getPlanSequenceId(), stageFinished.getPlannerStatistics());

      cleanupPathPlanningStage(stageFinished);

      if (debug)
         PrintTools.info("Stage " + stageFinished.getStageId() + " just finished planning its path.");
   }

   @Override
   public void stepPlanningIsComplete(FootstepPlanningResult stepPlanningResult, FootstepPlanningStage stageFinished)
   {
      completedStepResults.add(stepPlanningResult);

      if (stepPlanningResult != null && stepPlanningResult.validForExecution())
      {
         int sequence = stageFinished.getPlanSequenceId();
         FootstepPlan plan = stageFinished.getPlan();
         if (plan != null)
            completedStepPlans.add(sequence, plan);
      }

      completedStepPlanStatistics.add(stageFinished.getPlanSequenceId(), stageFinished.getPlannerStatistics());

      cleanupStepPlanningStage(stageFinished);

      if (debug)
         PrintTools.info("Stage " + stageFinished.getStageId() + " just finished planning its steps.");
   }

   public void processRequest(FootstepPlanningRequestPacket request)
   {
      isDone.set(false);
      isDonePlanningSteps.set(false);
      isDonePlanningPath.set(false);
      latestRequestReference.set(request);
   }

   public void processFootstepPlannerParameters(FootstepPlannerParametersPacket parameters)
   {
      latestFootstepPlannerParametersReference.set(parameters);
   }

   public void processVisibilityGraphsParameters(VisibilityGraphsParametersPacket parameters)
   {
      latestVisibilityGraphsParametersReference.set(parameters);
   }

   public void processPlanningStatisticsRequest()
   {
      EnumMap<StatisticsType, PlannerStatistics<?>> mapToPopulate = new EnumMap<>(StatisticsType.class);

      Iterable<ImmutablePair<Integer, PlannerStatistics<?>>> pathIterable = completedPathPlanStatistics.iterable();
      if (pathIterable != null)
      {
         for (ImmutablePair<Integer, PlannerStatistics<?>> pair : pathIterable)
         {
            if (pair != null)
               concatenateStatistics(mapToPopulate, pair.getLeft(), pair.getRight());
         }
      }

      Iterable<ImmutablePair<Integer, PlannerStatistics<?>>> stepIterable = completedStepPlanStatistics.iterable();
      if (stepIterable != null)
      {
         for (ImmutablePair<Integer, PlannerStatistics<?>> pair : stepIterable)
         {
            if (pair != null)
               concatenateStatistics(mapToPopulate, pair.getLeft(), pair.getRight());
         }
      }

      ListOfStatistics statistics = convertToListOfStatistics(mapToPopulate);
      sendPlannerStatistics(statistics);
   }

   private boolean initialize()
   {
      isDone.set(false);
      isDonePlanningPath.set(false);
      isDonePlanningSteps.set(false);
      requestedPlanarRegions.set(false);
      plannerTime.set(0.0);

      for (PathPlanningStage stage : allPathPlanningStages.iterable())
         stage.setTextToSpeechPublisher(textToSpeechPublisher);

      for (FootstepPlanningStage stage : allStepPlanningStages.iterable())
         stage.setTextToSpeechPublisher(textToSpeechPublisher);

      FootstepPlanningRequestPacket request = latestRequestReference.getAndSet(null);
      if (request == null)
         return false;

      planId.set(request.getPlannerRequestId());
      FootstepPlannerType requestedPlannerType = FootstepPlannerType.fromByte(request.getRequestedFootstepPlannerType());

      FootstepPlannerParametersPacket footstepPlannerParameters = latestFootstepPlannerParametersReference.getAndSet(null);
      if (footstepPlannerParameters != null)
         footstepPlanningParameters.set(footstepPlannerParameters);

      VisibilityGraphsParametersPacket visibilityGraphsParameters = latestVisibilityGraphsParametersReference.getAndSet(null);
      if (visibilityGraphsParameters != null)
         this.visibilityGraphsParameters.set(visibilityGraphsParameters);

      if (debug)
      {
         PrintTools.info("Starting to plan. Plan id: " + request.getPlannerRequestId() + ". Timeout: " + request.getTimeout());
      }

      if (requestedPlannerType != null)
      {
         activePlanner.set(requestedPlannerType);
      }

      PlanarRegionsListMessage planarRegionsListMessage = request.getPlanarRegionsListMessage();
      if (planarRegionsListMessage == null)
      {
         this.planarRegionsList = Optional.empty();
      }
      else
      {
         PlanarRegionsList planarRegionsList = PlanarRegionMessageConverter.convertToPlanarRegionsList(planarRegionsListMessage);
         this.planarRegionsList = Optional.of(planarRegionsList);
      }

      mainObjective = new FootstepPlannerObjective();

      FramePose3D initialStancePose = new FramePose3D(ReferenceFrame.getWorldFrame());
      initialStancePose.setPosition(new Point3D(request.getStanceFootPositionInWorld()));
      initialStancePose.setOrientation(new Quaternion(request.getStanceFootOrientationInWorld()));
      mainObjective.setInitialStanceFootPose(initialStancePose);

      FramePose3D goalPose = new FramePose3D(ReferenceFrame.getWorldFrame());
      goalPose.setPosition(new Point3D(request.getGoalPositionInWorld()));
      goalPose.setOrientation(new Quaternion(request.getGoalOrientationInWorld()));

      mainObjective.setInitialStanceFootSide(RobotSide.fromByte(request.getInitialStanceRobotSide()));

      FootstepPlannerGoal goal = new FootstepPlannerGoal();
      goal.setFootstepPlannerGoalType(FootstepPlannerGoalType.POSE_BETWEEN_FEET);
      goal.setGoalPoseBetweenFeet(goalPose);
      mainObjective.setGoal(goal);

      double timeout = request.getTimeout();
      if (timeout > 0.0 && Double.isFinite(timeout))
      {
         mainObjective.setTimeout(timeout);
         this.timeout.set(timeout);
      }
      else
      {
         mainObjective.setTimeout(Double.POSITIVE_INFINITY);
      }

      if (requestedPlannerType.plansPath())
      {
         double horizonLength = request.getHorizonLength();
         if (horizonLength > 0 && Double.isFinite(horizonLength))
            mainObjective.setHorizonLength(horizonLength);

         planningHorizonLength.set(horizonLength);

         pathPlanningObjectivePool.add(mainObjective);
         assignGoalsToAvailablePathPlanners();
      }
      else
      {
         isDonePlanningPath.set(true);
         stepPlanningObjectivePool.add(mainObjective);
         assignGoalsToAvailableStepPlanners();
      }

      return true;
   }

   public void update()
   {
      if (initialize.getBooleanValue())
      {
         if (!initialize()) // Return until the initialization succeeds
            return;
         initialize.set(false);
      }

      updateInternal();

      checkPlannersForFailures();
   }

   private void updateInternal()
   {
      plannerTime.add(Conversions.millisecondsToSeconds(tickDurationMs));
      if (plannerTime.getDoubleValue() > 1000.0)
      {
         if (debug)
            PrintTools.info("Hard timeout at " + plannerTime.getDoubleValue());
         reportPlannerFailed(FootstepPlanningResult.TIMED_OUT_BEFORE_SOLUTION);
         return;
      }

      if (!isDonePlanningPath.getBooleanValue())
      {
         assignGoalsToAvailablePathPlanners();

         FootstepPlanningResult pathStatus = getWorstResult(completedPathResults.getCopyForReading());
         if (!pathStatus.validForExecution())
         {
            reportPlannerFailed(pathStatus);
            return;
         }

         // path planner hasn't failed, so update the path planner status and send out new plan if finished
         boolean noMorePathsToPlan = pathPlanningStagesInProgress.isEmpty() && stepPlanningObjectivePool.isEmpty();
         if (noMorePathsToPlan) // path planning just finished
         {
            pathResult.set(pathStatus);

            sendMessageToUI("Result of path planning: " + planId.getIntegerValue() + ", " + pathStatus.toString());

            concatenatePathWaypoints();
            if (!computeBodyPath())
               reportPlannerFailed(pathResult.getEnumValue());

            statusOutputManager.reportStatusMessage(packPathResult(bodyPathPlan.get(), pathStatus));

            timeSpentBeforeFootstepPlanner.set(plannerTime.getDoubleValue());
            mainObjective.setTimeout(timeout.getDoubleValue() - timeSpentBeforeFootstepPlanner.getDoubleValue());
            stepPlanningObjectivePool.add(mainObjective);

            isDonePlanningPath.set(true);
         }
      }
      else
      {
         FootstepPlanningResult stepStatus = getWorstResult(completedStepResults.getCopyForReading());
         if (!stepStatus.validForExecution())
         {
            reportPlannerFailed(stepStatus);
            return;
         }

         updateStepPlanningObjectives();

         // check if there are any more goals, and assign them to the available planners
         assignGoalsToAvailableStepPlanners();

         // step planner hasn't failed, so update the step planner status and send out the new plan if finished
         boolean noMoreStepsToPlan = stepPlanningStagesInProgress.isEmpty() && stepPlanningObjectivePool.isEmpty();
         if (noMoreStepsToPlan) // step planning just finished.
         {
            stepResult.set(stepStatus);

            sendMessageToUI("Result of step planning: " + planId.getIntegerValue() + ", " + stepStatus.toString());

            concatenateFootstepPlans();
            FootstepPlan footstepPlan = this.footstepPlan.getAndSet(null);
            FootstepPlanningToolboxOutputStatus footstepPlanMessage = packStepResult(footstepPlan, bodyPathPlan.getAndSet(null), stepStatus,
                                                                                     plannerTime.getDoubleValue());
            statusOutputManager.reportStatusMessage(footstepPlanMessage);

            timeSpentInFootstepPlanner.set(plannerTime.getDoubleValue() - timeSpentBeforeFootstepPlanner.getDoubleValue());

            isDonePlanningSteps.set(true);
         }
      }

      plannerListener.tickAndUpdate();

      boolean isDone = stepPlanningStagesInProgress.isEmpty();
      isDone &= latestRequestReference.get() == null;
      isDone &= pathPlanningStagesInProgress.isEmpty();
      isDone &= stepPlanningObjectivePool.isEmpty();
      isDone &= !goalRecommendationHandler.hasNewFootstepPlannerObjectives();
      isDone &= isDonePlanningPath.getBooleanValue();
      isDone &= isDonePlanningSteps.getBooleanValue();

      if (isDone)
         plannerListener.plannerFinished(null);

      this.isDone.set(isDone);
   }

   private void checkPlannersForFailures()
   {
      Iterable<PathPlanningStage> pathIterable = pathPlanningTasks.iterator();
      if (pathIterable != null)
      {
         for (PathPlanningStage stage : pathIterable)
         {
            ScheduledFuture<?> task = pathPlanningTasks.getCopyForReading().get(stage);

            if (task.isDone())
            {
               try
               {
                  task.get();
               }
               catch (ExecutionException exception)
               {
                  exception.getCause().printStackTrace();
                  PrintTools.info(exception.getMessage());
                  exception.printStackTrace();
               }
               catch (InterruptedException ex)
               {
                  ex.getCause().printStackTrace();
               }

               throw new RuntimeException("Something killed path planning stage " + stage.getStageId() + " before it could complete.");
            }
         }
      }

      Iterable<FootstepPlanningStage> stepIterable = stepPlanningTasks.iterator();
      if (stepIterable != null)
      {
         for (FootstepPlanningStage stage : stepPlanningTasks.iterator())
         {
            ScheduledFuture<?> task = stepPlanningTasks.getCopyForReading().get(stage);

            if (task.isDone())
            {
               try
               {
                  task.get();
               }
               catch (ExecutionException exception)
               {
                  exception.getCause().printStackTrace();
                  PrintTools.info(exception.getMessage());
                  exception.printStackTrace();
               }
               catch (InterruptedException ex)
               {
                  ex.getCause().printStackTrace();
               }

               throw new RuntimeException("Something killed step planning stage " + stage.getStageId() + " before it could complete.");
            }
         }
      }
   }

   private static FootstepPlanningResult getWorstResult(List<FootstepPlanningResult> results)
   {
      FootstepPlanningResult worstResult = FootstepPlanningResult.OPTIMAL_SOLUTION;
      if (results == null) // hasn't been assigned yet.
         return worstResult;

      for (FootstepPlanningResult result : results)
         worstResult = FootstepPlanningResult.getWorstResult(worstResult, result);

      return worstResult;
   }

   private void reportPlannerFailed(FootstepPlanningResult result)
   {
      statusOutputManager.reportStatusMessage(packStepResult(null, null, result, plannerTime.getDoubleValue()));
      statusOutputManager.reportStatusMessage(FootstepPlanningMessageReporter.packStatus(FootstepPlannerStatus.IDLE));
      isDonePlanningSteps.set(true);
      isDonePlanningPath.set(true);
      isDone.set(true);
   }

   private void concatenatePathWaypoints()
   {
      if (completedPathWaypoints.isEmpty())
      {
         waypointPlan.set(null);
         return;
      }

      PairList<Integer, List<Point3D>> completedPathWaypoints = this.completedPathWaypoints.getCopyForReading();
      completedPathWaypoints.sort(Comparator.comparingInt(ImmutablePair<Integer, List<Point3D>>::getLeft));

      List<Point3D> allWaypoints = new ArrayList<>();
      for (ImmutablePair<Integer, List<Point3D>> completedWaypoints : completedPathWaypoints)
      {
         allWaypoints.addAll(completedWaypoints.getRight());
      }

      waypointPlan.set(allWaypoints);
   }

   private boolean computeBodyPath()
   {
      FootstepPlanningResult pathResult = this.pathResult.getEnumValue();
      if (waypointPlan.get() == null || !pathResult.validForExecution())
      {
         bodyPathPlan.set(null);
         return false;
      }

      if (pathResult == FootstepPlanningResult.PLANNER_FAILED)
      {
         timeSpentBeforeFootstepPlanner.set(plannerTime.getDoubleValue());
         timeSpentInFootstepPlanner.set(0.0);
         return false;
      }

      if (waypointPlan.get().size() < 2)
      {
         if (footstepPlanningParameters.getReturnBestEffortPlan())
         {
            computeBestEffortPlan(planningHorizonLength.getDoubleValue());
         }
         else
         {
            timeSpentBeforeFootstepPlanner.set(plannerTime.getDoubleValue());
            timeSpentInFootstepPlanner.set(0.0);
            this.pathResult.set(FootstepPlanningResult.PLANNER_FAILED);
            return false;
         }
      }

      bodyPathPlanner.setWaypoints(waypointPlan.get());
      bodyPathPlanner.compute();

      bodyPathPlan.set(bodyPathPlanner.compute());
      return true;
   }

   private void computeBestEffortPlan(double horizonLength)
   {
      FramePose3D bodyGoalPose = new FramePose3D(mainObjective.getGoal().getGoalPoseBetweenFeet());
      FramePose3D bodyStartPose = new FramePose3D();

      RobotSide side = mainObjective.getInitialStanceFootSide() != null ? mainObjective.getInitialStanceFootSide() : RobotSide.LEFT;
      FramePose3D stanceFootPose = mainObjective.getInitialStanceFootPose();

      double defaultStepWidth = footstepPlanningParameters.getIdealFootstepWidth();
      ReferenceFrame stanceFrame = new PoseReferenceFrame("stanceFrame", stanceFootPose);
      FramePoint2D bodyStartPoint = new FramePoint2D(stanceFrame);
      bodyStartPoint.setY(side.negateIfLeftSide(defaultStepWidth / 2.0));
      bodyStartPoint.changeFrameAndProjectToXYPlane(ReferenceFrame.getWorldFrame());

      bodyStartPose.setPosition(bodyStartPoint.getX(), bodyStartPoint.getY(), 0.0);
      bodyStartPose.setOrientationYawPitchRoll(stanceFootPose.getYaw(), 0.0, 0.0);

      Vector2D goalDirection = new Vector2D(bodyGoalPose.getPosition());
      goalDirection.sub(bodyStartPose.getX(), bodyStartPose.getY());
      goalDirection.scale(horizonLength / goalDirection.length());
      Point3D waypoint = new Point3D(bodyStartPose.getPosition());
      waypoint.add(goalDirection.getX(), goalDirection.getY(), 0.0);
      waypointPlan.get().add(waypoint);
   }

   private void concatenateFootstepPlans()
   {
      if (completedStepPlans.isEmpty())
      {
         footstepPlan.set(null);
         return;
      }

      PairList<Integer, FootstepPlan> completedStepPlans = this.completedStepPlans.getCopyForReading();
      completedStepPlans.sort(Comparator.comparingInt(ImmutablePair<Integer, FootstepPlan>::getLeft));

      int firstPlan = Integer.MAX_VALUE;
      FootstepPlan totalFootstepPlan = new FootstepPlan();

      for (ImmutablePair<Integer, FootstepPlan> footstepPlanPairs : completedStepPlans)
      {
         FootstepPlan footstepPlan = footstepPlanPairs.getRight();
         int number = footstepPlanPairs.getLeft();

         if (footstepPlan.hasLowLevelPlanGoal() && number < firstPlan)
         {
            totalFootstepPlan.setLowLevelPlanGoal(footstepPlan.getLowLevelPlanGoal());
            firstPlan = number;
         }

         totalFootstepPlan.addFootstepPlan(footstepPlan);
      }

      footstepPlan.set(totalFootstepPlan);
   }

   private void sendMessageToUI(String message)
   {
      textToSpeechPublisher.publish(MessageTools.createTextToSpeechPacket(message));
   }

   protected boolean isDone()
   {
      return isDone.getBooleanValue();
   }

   private BodyPathPlanMessage packPathResult(BodyPathPlan bodyPathPlan, FootstepPlanningResult status)
   {
      return FootstepPlanningMessageReporter.packPathResult(bodyPathPlan, status, planarRegionsList, planId.getIntegerValue());
   }

   private FootstepPlanningToolboxOutputStatus packStepResult(FootstepPlan footstepPlan, BodyPathPlan bodyPathPlan, FootstepPlanningResult status,
                                                              double timeTaken)
   {
      return FootstepPlanningMessageReporter.packStepResult(footstepPlan, bodyPathPlan, status, timeTaken, planarRegionsList, planId.getIntegerValue());
   }

   private void concatenateStatistics(EnumMap<StatisticsType, PlannerStatistics<?>> mapToPopulate, int segmentId, PlannerStatistics<?> plannerStatistics)
   {
      if (plannerStatistics == null)
         return;

      switch (plannerStatistics.getStatisticsType())
      {
      case LIST:
         concatenateListOfStatistics(mapToPopulate, segmentId, (ListOfStatistics) plannerStatistics);
         break;
      case VISIBILITY_GRAPH:
         VisibilityGraphStatistics incomingStatistics = (VisibilityGraphStatistics) plannerStatistics;
         VisibilityGraphStatistics statistics;
         if (mapToPopulate.containsKey(StatisticsType.VISIBILITY_GRAPH))
            statistics = (VisibilityGraphStatistics) mapToPopulate.get(StatisticsType.VISIBILITY_GRAPH);
         else
         {
            statistics = new VisibilityGraphStatistics();
            mapToPopulate.put(StatisticsType.VISIBILITY_GRAPH, statistics);
         }

         if (segmentId == 1)
            statistics.setStartMapId(incomingStatistics.getStartMapId());
         if (segmentId == globalStepSequenceIndex.getIntegerValue())
            statistics.setGoalMapId(incomingStatistics.getGoalMapId());

         statistics.getStartVisibilityMap().addConnections(incomingStatistics.getStartVisibilityMap().getConnections());
         statistics.getGoalVisibilityMap().addConnections(incomingStatistics.getGoalVisibilityMap().getConnections());
         statistics.getInterRegionsVisibilityMap().addConnections(incomingStatistics.getInterRegionsVisibilityMap().getConnections());
         for (int i = 0; i < incomingStatistics.getNumberOfNavigableRegions(); i++)
            statistics.addNavigableRegion(incomingStatistics.getNavigableRegion(i));

         break;
      }
   }

   private void concatenateListOfStatistics(EnumMap<StatisticsType, PlannerStatistics<?>> mapToPopulate, int segmentId, ListOfStatistics listOfStatistics)
   {
      while (listOfStatistics.getNumberOfStatistics() > 0)
         concatenateStatistics(mapToPopulate, segmentId, listOfStatistics.pollStatistics());
   }

   private ListOfStatistics convertToListOfStatistics(EnumMap<StatisticsType, PlannerStatistics<?>> statisticsMap)
   {
      if (statisticsMap.containsKey(StatisticsType.LIST))
         throw new IllegalArgumentException("Statistics haven't been unpacked properly");

      ListOfStatistics statistics = new ListOfStatistics();
      for (StatisticsType type : statisticsMap.keySet())
         statistics.addStatistics(statisticsMap.get(type));

      return statistics;
   }

   private void sendPlannerStatistics(PlannerStatistics<?> plannerStatistics)
   {
      switch (plannerStatistics.getStatisticsType())
      {
      case LIST:
         sendListOfStatistics((ListOfStatistics) plannerStatistics);
         break;
      case VISIBILITY_GRAPH:
         statusOutputManager.reportStatusMessage(VisibilityGraphMessagesConverter.convertToBodyPathPlanStatisticsMessage(planId.getIntegerValue(),
                                                                                                                         (VisibilityGraphStatistics) plannerStatistics));
         break;
      }
   }

   private void sendListOfStatistics(ListOfStatistics listOfStatistics)
   {
      while (listOfStatistics.getNumberOfStatistics() > 0)
         sendPlannerStatistics(listOfStatistics.pollStatistics());
   }

   public void setTextToSpeechPublisher(IHMCRealtimeROS2Publisher<TextToSpeechPacket> textToSpeechPublisher)
   {
      this.textToSpeechPublisher = textToSpeechPublisher;
   }

   public void wakeUp()
   {
      if (debug)
         PrintTools.debug(this, "Waking up");

      initialize.set(true);
   }

   public void sleep()
   {
      processPlanningStatisticsRequest();

      if (debug)
         PrintTools.debug(this, "Going to sleep");

      cancelAllActiveStages();
      cleanupAllPlanningStages();

      for (FootstepPlanningStage planningTask : stepPlanningTasks.iterator())
         stepPlanningTasks.remove(planningTask).cancel(true);
   }

   public void destroy()
   {
      sleep();

      if (debug)
         PrintTools.debug(this, "Destroyed");
   }
}
