package us.ihmc.avatar.referenceFrames;

import static us.ihmc.robotics.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gnu.trove.map.hash.TLongObjectHashMap;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.humanoidRobotics.frames.HumanoidReferenceFrames;
import us.ihmc.robotModels.FullHumanoidRobotModel;
import us.ihmc.robotics.referenceFrames.PoseReferenceFrame;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.screwTheory.OneDoFJoint;
import us.ihmc.sensorProcessing.frames.ReferenceFrameHashCodeResolver;
import us.ihmc.sensorProcessing.frames.ReferenceFrames;
import us.ihmc.tools.MemoryTools;

public abstract class ReferenceFrameHashTest
{

   @BeforeEach
   public void showMemoryUsageBeforeTest()
   {
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " before test.");
   }

   @AfterEach
   public void showMemoryUsageAfterTest()
   {
      ReferenceFrameTools.clearWorldFrameTree();
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " after test.");
   }

   @Test // timeout = 30000
   public void testGetReferenceFrameFromHashCodeReturnsSameNamedFrames()
   {
      DRCRobotModel robotModelA = getRobotModel();
      FullHumanoidRobotModel fullRobotModelA = robotModelA.createFullRobotModel();
      HumanoidReferenceFrames referenceFramesA = new HumanoidReferenceFrames(fullRobotModelA);
   
      DRCRobotModel robotModelB = getRobotModel();
      FullHumanoidRobotModel fullRobotModelB = robotModelB.createFullRobotModel();
      HumanoidReferenceFrames referenceFramesB = new HumanoidReferenceFrames(fullRobotModelB);
      ReferenceFrameHashCodeResolver referenceFrameHashCodeResolverB = new ReferenceFrameHashCodeResolver(fullRobotModelB, referenceFramesB);
   
      ReferenceFrame midFeetZUpFrameA = referenceFramesA.getMidFeetZUpFrame();
      long nameBasedHashCode = midFeetZUpFrameA.hashCode();
      
      ReferenceFrame midZUpFrameB = referenceFrameHashCodeResolverB.getReferenceFrameFromNameBaseHashCode(nameBasedHashCode);
      checkReferenceFramesMatch(midFeetZUpFrameA, midZUpFrameB);
   }

   @Test // timeout = 30000
   public void testAllFramesInFullRobotModelMatchHumanoidReferenceFramesThroughHashCode()
   {
      DRCRobotModel robotModelA = getRobotModel();
      FullHumanoidRobotModel fullRobotModel = robotModelA.createFullRobotModel();
      HumanoidReferenceFrames referenceFramesA = new HumanoidReferenceFrames(fullRobotModel);
      ReferenceFrameHashCodeResolver referenceFrameHashCodeResolverA = new ReferenceFrameHashCodeResolver(fullRobotModel, referenceFramesA);
   
//      System.out.println(fullRobotModel.getChest().getBodyFixedFrame().getName() + " hashCode: " + fullRobotModel.getChest().getBodyFixedFrame().getNameBasedHashCode());
      for (OneDoFJoint joint : fullRobotModel.getOneDoFJoints())
      {
         ReferenceFrame frameBeforeJoint = joint.getFrameBeforeJoint();
         ReferenceFrame frameAfterJoint = joint.getFrameAfterJoint();
         ReferenceFrame comLinkBefore = joint.getPredecessor().getBodyFixedFrame();
         ReferenceFrame comLinkAfter = joint.getSuccessor().getBodyFixedFrame();
   
         System.out.println(frameBeforeJoint.getName() + " hashCode: " + frameBeforeJoint.hashCode());
         System.out.println(frameAfterJoint.getName() + " hashCode: " + frameAfterJoint.hashCode());
         System.out.println(comLinkBefore.getName() + " hashCode: " + comLinkBefore.hashCode());
         System.out.println(comLinkAfter.getName() + " hashCode: " + comLinkAfter.hashCode());
         
         ReferenceFrame otherFrameBeforeJoint = referenceFrameHashCodeResolverA.getReferenceFrameFromNameBaseHashCode(frameBeforeJoint.hashCode());
         ReferenceFrame otherFrameAfterJoint = referenceFrameHashCodeResolverA.getReferenceFrameFromNameBaseHashCode(frameAfterJoint.hashCode());
         ReferenceFrame otherCoMlinkBefore = referenceFrameHashCodeResolverA.getReferenceFrameFromNameBaseHashCode(comLinkBefore.hashCode());
         ReferenceFrame otherCoMLinkAfter = referenceFrameHashCodeResolverA.getReferenceFrameFromNameBaseHashCode(comLinkAfter.hashCode());
   
         checkReferenceFramesMatch(frameBeforeJoint, otherFrameBeforeJoint);
         checkReferenceFramesMatch(frameAfterJoint, otherFrameAfterJoint);
         checkReferenceFramesMatch(comLinkBefore, otherCoMlinkBefore);
         checkReferenceFramesMatch(comLinkAfter, otherCoMLinkAfter);
      }
   }

   @Test // timeout = 30000
   public void testAllFramesGottenFromHumanoidReferenceFrameMethodsAreInTheHashList()
         throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      DRCRobotModel robotModelA = getRobotModel();
      FullHumanoidRobotModel fullRobotModel = robotModelA.createFullRobotModel();
      HumanoidReferenceFrames referenceFrames = new HumanoidReferenceFrames(fullRobotModel);
      ReferenceFrameHashCodeResolver referenceFrameHashCodeResolver = new ReferenceFrameHashCodeResolver(fullRobotModel, referenceFrames);
      Method[] declaredMethods = referenceFrames.getClass().getMethods();
      for (Method method : declaredMethods)
      {
         if (method.getReturnType() == ReferenceFrame.class)
         {
            if (method.getParameterCount() == 0)
            {
               ReferenceFrame referenceFrame = (ReferenceFrame) method.invoke(referenceFrames);
               if(referenceFrame != null)
               {
                  ReferenceFrame referenceFrameFromNameBaseHashCode = referenceFrameHashCodeResolver.getReferenceFrameFromNameBaseHashCode(referenceFrame.hashCode());
                  System.out.println(referenceFrame.getName() + " hashCode: " + referenceFrame.hashCode());
                  assertNotNull(referenceFrame.getName() + " was not in the reference frame hash map. fix ReferenceFrameHashCodeResolver!", referenceFrameFromNameBaseHashCode);
                  checkReferenceFramesMatch(referenceFrame, referenceFrameFromNameBaseHashCode);
               }
            }
            else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == RobotSide.class)
            {
               for(RobotSide robotSide : RobotSide.values)
               {
                  ReferenceFrame referenceFrame = (ReferenceFrame) method.invoke(referenceFrames, robotSide);
                  if(referenceFrame != null)
                  {
                     ReferenceFrame referenceFrameFromNameBaseHashCode = referenceFrameHashCodeResolver.getReferenceFrameFromNameBaseHashCode(referenceFrame.hashCode());
                     assertNotNull("called " + method.getName() + ": " + referenceFrame.getName() + " was not in the reference frame hash map. fix ReferenceFrameHashCodeResolver!", referenceFrameFromNameBaseHashCode);
                     checkReferenceFramesMatch(referenceFrame, referenceFrameFromNameBaseHashCode);
                  }
               }
            }
         }
      }
   }

   @Test // timeout = 30000
   public void testAllFramesGottenFromFullRobotModelMethodsAreInTheHashList()
         throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      DRCRobotModel robotModelA = getRobotModel();
      FullHumanoidRobotModel fullRobotModel = robotModelA.createFullRobotModel();
      HumanoidReferenceFrames referenceFrames = new HumanoidReferenceFrames(fullRobotModel);
      Method[] declaredMethods = fullRobotModel.getClass().getMethods();
      ReferenceFrameHashCodeResolver referenceFrameHashCodeResolver = new ReferenceFrameHashCodeResolver(fullRobotModel, referenceFrames);
      for (Method method : declaredMethods)
      {
         if (method.getReturnType() == ReferenceFrame.class)
         {
            if (method.getParameterCount() == 0)
            {
               ReferenceFrame referenceFrame = (ReferenceFrame) method.invoke(fullRobotModel);
               if(referenceFrame != null)
               {
                  ReferenceFrame referenceFrameFromNameBaseHashCode = referenceFrameHashCodeResolver.getReferenceFrameFromNameBaseHashCode(referenceFrame.hashCode());
                  assertNotNull(referenceFrame.getName() + " was not in the reference frame hash map. fix ReferenceFrameHashCodeResolver!", referenceFrameFromNameBaseHashCode);
                  System.out.println(referenceFrame.getName() + " hashCode: " + referenceFrame.hashCode());
                  checkReferenceFramesMatch(referenceFrame, referenceFrameFromNameBaseHashCode);
               }
            }
            else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == RobotSide.class)
            {
               for(RobotSide robotSide : RobotSide.values)
               {
                  ReferenceFrame referenceFrame = (ReferenceFrame) method.invoke(fullRobotModel, robotSide);
                  if(referenceFrame != null)
                  {
                     ReferenceFrame referenceFrameFromNameBaseHashCode = referenceFrameHashCodeResolver.getReferenceFrameFromNameBaseHashCode(referenceFrame.hashCode());
                     assertNotNull("called " + method.getName() + ": " + referenceFrame.getName() + " was not in the reference frame hash map. fix ReferenceFrameHashCodeResolver!", referenceFrameFromNameBaseHashCode);
                     checkReferenceFramesMatch(referenceFrame, referenceFrameFromNameBaseHashCode);
                  }
               }
            }
         }
      }
   }

   @Test // timeout = 30000, expected = IllegalArgumentException.class
   public void testAddingTwoFramesWithTheSameNameThrowsException()
   {
      DRCRobotModel robotModelA = getRobotModel();
      FullHumanoidRobotModel fullRobotModel = robotModelA.createFullRobotModel();
      TestReferenceFrames referenceFrames = new TestReferenceFrames();
   
      //should throw an IllegalArgumentException
      ReferenceFrameHashCodeResolver referenceFrameHashCodeResolverA = new ReferenceFrameHashCodeResolver(fullRobotModel, referenceFrames);
   
   }

   private void checkReferenceFramesMatch(ReferenceFrame referenceFrameA, ReferenceFrame referenceFrameB)
   {
      assertEquals("reference frame names didnt match", referenceFrameA.getName(), referenceFrameB.getName());
      assertEquals("hash codes didn't match", referenceFrameA.hashCode(), referenceFrameB.hashCode());
      
      if (referenceFrameA.getParent() != null || referenceFrameB.getParent() != null)
      {
         assertEquals("parent reference frame names didnt match", referenceFrameA.getParent().getName(), referenceFrameB.getParent().getName());
         assertEquals("parent hash codes didn't match", referenceFrameA.getParent().hashCode(), referenceFrameB.getParent().hashCode());
      }
   }
   
   //create two frames with the same name
   public class TestReferenceFrames implements ReferenceFrames
   {
      private final PoseReferenceFrame comFrame = new PoseReferenceFrame("comFrame", ReferenceFrame.getWorldFrame());
      private final PoseReferenceFrame comFrame2 = new PoseReferenceFrame("comFrame", ReferenceFrame.getWorldFrame());
      
      @Override
      public void updateFrames()
      {
         
      }

      @Override
      public ReferenceFrame getCenterOfMassFrame()
      {
         return comFrame;
      }
      
      public ReferenceFrame getCenterOfMassFrame2()
      {
         return comFrame2;
      }

      @Override
      public TLongObjectHashMap<ReferenceFrame> getReferenceFrameDefaultHashIds()
      {
         return null;
      }
   }

   public abstract DRCRobotModel getRobotModel();
}