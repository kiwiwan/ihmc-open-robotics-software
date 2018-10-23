package us.ihmc.commonWalkingControlModules.capturePoint.smoothCMPBasedICPPlanner.CoPGeneration;

import static us.ihmc.robotics.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import us.ihmc.commonWalkingControlModules.configurations.CoPPointName;
import us.ihmc.commons.Epsilons;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameTestTools;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPosition;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsList;
import us.ihmc.graphicsDescription.yoGraphics.plotting.ArtifactList;
import us.ihmc.humanoidRobotics.footstep.FootSpoof;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoFramePoint3D;

public class CoPPointsInFootTest
{
   private static final String testClassName = "CoPPointsInFootTestRegistry";
   private static final double epsilon = Epsilons.ONE_BILLIONTH;
   private final double maxXInSoleFrame = -0.075;
   private final double minXInSoleFrame = -0.05;
   private final double maxYInSoleFrame = +0.05;
   private final double minYInSoleFrame = -0.075;
   private final double xToAnkle = 0.0;
   private final double yToAnkle = -0.15;
   private final double zToAnkle = 0.5;
   private final List<Point2D> footVertexList = Arrays.asList(new Point2D(maxXInSoleFrame, maxYInSoleFrame), new Point2D(maxXInSoleFrame, minYInSoleFrame),
                                                              new Point2D(minXInSoleFrame, maxYInSoleFrame), new Point2D(minXInSoleFrame, minYInSoleFrame));
   private final YoVariableRegistry registry = new YoVariableRegistry(testClassName);
   private final FootSpoof footSpoof = new FootSpoof("DummyFoot", xToAnkle, yToAnkle, zToAnkle, footVertexList, 0.5);
   private final static ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();
   private final ReferenceFrame[] framesToRegister = {worldFrame, footSpoof.getSoleFrame()};
   private CoPPointsInFoot copPointsInFoot;

   @BeforeEach
   public void setup()
   {
      copPointsInFoot = new CoPPointsInFoot(testClassName, 0, framesToRegister, registry);
   }

   @AfterEach
   public void clean()
   {
      copPointsInFoot.reset();
      registry.clear();
      ReferenceFrameTools.clearWorldFrameTree();
   }

   @Test // timeout = 30000
   public void testAddCoPPointToList()
   {
      assertTrue(copPointsInFoot.isEmpty());
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.0, new FramePoint3D());
      copPointsInFoot.addWaypoint(CoPPointName.ENTRY_COP, 0.1, new FramePoint3D());
      assertEquals(2, copPointsInFoot.getNumberOfCoPPoints());
      assertEquals(CoPPointName.MIDFOOT_COP, copPointsInFoot.getCoPPointList().get(0));
      assertEquals(CoPPointName.ENTRY_COP, copPointsInFoot.getCoPPointList().get(1));
      copPointsInFoot.reset();
      assertTrue(copPointsInFoot.getCoPPointList().isEmpty());
   }

   @Test // timeout = 30000
   public void testAddAndSetIncludingFrameWithFramePoint()
   {
      FramePoint3D testLocation = new FramePoint3D(footSpoof.getSoleFrame(), Math.random(), Math.random(), Math.random());
      assertTrue(copPointsInFoot.isEmpty());
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.2, testLocation);
      assertEquals(1, copPointsInFoot.getCoPPointList().size());

      testLocation.changeFrame(worldFrame);
      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(testLocation, copPointsInFoot.get(0).getPosition(), epsilon);
      assertEquals(0.2, copPointsInFoot.get(0).getTime(), epsilon);
   }

   @Test // timeout = 30000
   public void testAddAndSetIncludingFrameWithYoFramePoint()
   {
      YoFramePoint3D testLocation1 = new YoFramePoint3D("TestLocation1", footSpoof.getSoleFrame(), null);
      YoFramePoint3D testLocation2 = new YoFramePoint3D("TestLocation2", footSpoof.getSoleFrame(), null);
      testLocation1.set(Math.random(), Math.random(), Math.random());
      testLocation2.set(Math.random(), Math.random(), Math.random());
      copPointsInFoot.addWaypoint(CoPPointName.ENTRY_COP, 0.87, testLocation1);
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.12, testLocation2);
      assertEquals(2, copPointsInFoot.getCoPPointList().size());

      FramePoint3D pointToTest = new FramePoint3D(testLocation1);
      pointToTest.changeFrame(worldFrame);
      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(pointToTest, copPointsInFoot.get(0).getPosition(), epsilon);
      assertEquals(0.87, copPointsInFoot.get(0).getTime(), epsilon);
      pointToTest = new FramePoint3D(testLocation2);
      pointToTest.changeFrame(worldFrame);
      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(pointToTest, copPointsInFoot.get(1).getPosition(), epsilon);
      assertEquals(0.12, copPointsInFoot.get(1).getTime(), epsilon);
   }

   @Test // timeout = 30000
   public void testAddAndSetIncludingFrameWithCoPTrajectoryPoint()
   {
      CoPTrajectoryPoint testLocation1 = new CoPTrajectoryPoint("TestLocation1", "", null, framesToRegister);
      CoPTrajectoryPoint testLocation2 = new CoPTrajectoryPoint("TestLocation2", "", null, framesToRegister);
      testLocation1.changeFrame(footSpoof.getSoleFrame());
      testLocation1.setPosition(new FramePoint3D(footSpoof.getSoleFrame(), Math.random(), Math.random(), Math.random()));
      testLocation2.changeFrame(footSpoof.getSoleFrame());
      testLocation2.setPosition(new FramePoint3D(footSpoof.getSoleFrame(), Math.random(), Math.random(), Math.random()));
      copPointsInFoot.addWaypoint(CoPPointName.ENTRY_COP, 0.87, testLocation1);
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.12, testLocation2);
      assertEquals(2, copPointsInFoot.getCoPPointList().size());

      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(testLocation1.getPosition(), copPointsInFoot.get(0).getPosition(), epsilon);
      assertEquals(0.87, copPointsInFoot.get(0).getTime(), epsilon);
      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(testLocation2.getPosition(), copPointsInFoot.get(1).getPosition(), epsilon);
      assertEquals(0.12, copPointsInFoot.get(1).getTime(), epsilon);
      FramePoint3D tempFramePointForTesting = new FramePoint3D(footSpoof.getSoleFrame());
      copPointsInFoot.getFinalCoPPosition(tempFramePointForTesting);
      EuclidFrameTestTools.assertFramePoint3DGeometricallyEquals(testLocation2.getPosition(), tempFramePointForTesting, epsilon);
   }

   @Test // timeout = 30000
   public void testSetFeetLocations()
   {
      copPointsInFoot.setFeetLocation(new FramePoint3D(worldFrame, 0.2, 1.35, 2.1), new FramePoint3D(worldFrame, 1.3, 2.4, 6.6));
      FramePoint3D framePointForTesting = new FramePoint3D();
      copPointsInFoot.getSwingFootLocation(framePointForTesting);
      assertEquals(framePointForTesting.getReferenceFrame(), worldFrame);
      assertEquals(framePointForTesting.getX(), 0.2, epsilon);
      assertEquals(framePointForTesting.getY(), 1.35, epsilon);
      assertEquals(framePointForTesting.getZ(), 2.1, epsilon);
      copPointsInFoot.getSupportFootLocation(framePointForTesting);
      assertEquals(framePointForTesting.getX(), 1.3, epsilon);
      assertEquals(framePointForTesting.getY(), 2.4, epsilon);
      assertEquals(framePointForTesting.getZ(), 6.6, epsilon);
   }

   @Test // timeout = 30000
   public void testChangeFrame()
   {
      copPointsInFoot.setFeetLocation(new FramePoint3D(worldFrame, 0.2, 0.1, 0.1), new FramePoint3D(worldFrame, 0.2, -0.1, 0.1));
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.2, new FramePoint3D(worldFrame, 0.2, 0.15, 0.1));
      copPointsInFoot.addWaypoint(CoPPointName.ENTRY_COP, 0.8, new FramePoint3D(worldFrame, 0.15, -0.05, 0.11));
      copPointsInFoot.changeFrame(footSpoof.getSoleFrame());
      FramePoint3D tempFramePoint = new FramePoint3D();
      copPointsInFoot.getSupportFootLocation(tempFramePoint);
      assertEquals(tempFramePoint.getReferenceFrame(), footSpoof.getSoleFrame());
      assertEquals(tempFramePoint.getX(), 0.2 + xToAnkle, epsilon);
      assertEquals(tempFramePoint.getY(), -0.1 + yToAnkle, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.1 + zToAnkle, epsilon);
      assertEquals(copPointsInFoot.get(0).getReferenceFrame(), footSpoof.getSoleFrame());
      copPointsInFoot.get(0).getPosition(tempFramePoint);
      assertEquals(tempFramePoint.getX(), 0.2 + xToAnkle, epsilon);
      assertEquals(tempFramePoint.getY(), 0.15 + yToAnkle, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.1 + zToAnkle, epsilon);
      assertEquals(copPointsInFoot.get(1).getReferenceFrame(), footSpoof.getSoleFrame());
      copPointsInFoot.get(1).getPosition(tempFramePoint);
      assertEquals(tempFramePoint.getX(), 0.15 + xToAnkle, epsilon);
      assertEquals(tempFramePoint.getY(), -0.05 + yToAnkle, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.11 + zToAnkle, epsilon);
   }

   @Test // timeout = 30000
   public void testRegisterFrame()
   {
      double newFrameOriginX = 1;
      double newFrameOriginY = -1;
      double newFrameOriginZ = 1;

      copPointsInFoot.setFeetLocation(new FramePoint3D(worldFrame, 0.2, 0.1, 0.1), new FramePoint3D(worldFrame, 0.2, -0.1, 0.1));
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 0.2, new FramePoint3D(worldFrame, 0.2, 0.15, 0.1));
      copPointsInFoot.addWaypoint(CoPPointName.ENTRY_COP, 0.8, new FramePoint3D(worldFrame, 0.15, -0.05, 0.11));
      ReferenceFrame newFrameToRegister = ReferenceFrame.constructFrameWithUnchangingTransformFromParent("RandomFrameToRegister", worldFrame,
                                                                                                         new RigidBodyTransform(new Quaternion(),
                                                                                                                                new FrameVector3D(worldFrame,
                                                                                                                                                  newFrameOriginX,
                                                                                                                                                  newFrameOriginY,
                                                                                                                                                  newFrameOriginZ)));
      copPointsInFoot.registerReferenceFrame(newFrameToRegister);
      copPointsInFoot.changeFrame(newFrameToRegister);
      FramePoint3D tempFramePoint = new FramePoint3D();
      copPointsInFoot.getSupportFootLocation(tempFramePoint);
      assertEquals(tempFramePoint.getReferenceFrame(), newFrameToRegister);
      assertEquals(tempFramePoint.getX(), 0.2 + newFrameOriginX, epsilon);
      assertEquals(tempFramePoint.getY(), -0.1 + newFrameOriginY, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.1 + newFrameOriginZ, epsilon);
      assertEquals(copPointsInFoot.get(0).getReferenceFrame(), newFrameToRegister);
      copPointsInFoot.get(0).getPosition(tempFramePoint);
      assertEquals(tempFramePoint.getX(), 0.2 + newFrameOriginX, epsilon);
      assertEquals(tempFramePoint.getY(), 0.15 + newFrameOriginY, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.1 + newFrameOriginZ, epsilon);
      assertEquals(copPointsInFoot.get(1).getReferenceFrame(), newFrameToRegister);
      copPointsInFoot.get(1).getPosition(tempFramePoint);
      assertEquals(tempFramePoint.getX(), 0.15 + newFrameOriginX, epsilon);
      assertEquals(tempFramePoint.getY(), -0.05 + newFrameOriginY, epsilon);
      assertEquals(tempFramePoint.getZ(), 0.11 + newFrameOriginZ, epsilon);
   }

   @Test // timeout = 30000
   public void testVisualization()
   {
      YoGraphicsList dummyGraphicsList = new YoGraphicsList("DummyGraphics");
      ArtifactList dummyArtifactList = new ArtifactList("DummyArtifacts");
      copPointsInFoot.setupVisualizers(dummyGraphicsList, dummyArtifactList, 0.05);
      assertEquals(dummyArtifactList.getArtifacts().size(), 10);
      assertEquals(dummyGraphicsList.getYoGraphics().size(), 10);
      copPointsInFoot.addWaypoint(CoPPointName.MIDFOOT_COP, 1.0, new FramePoint3D(footSpoof.getSoleFrame(), 1.0, 2.1, 3.1));

      YoGraphicPosition graphic = (YoGraphicPosition) dummyGraphicsList.getYoGraphics().get(0);
      assertEquals(1.0 - xToAnkle, graphic.getX(), 1e-5);
      assertEquals(2.1 - yToAnkle, graphic.getY(), 1e-5);
      assertEquals(3.1 - zToAnkle, graphic.getZ(), 1e-5);
   }

}
