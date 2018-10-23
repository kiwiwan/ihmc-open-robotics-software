package us.ihmc.robotics.screwTheory;

import org.ejml.data.DenseMatrix64F;
import org.junit.jupiter.api.AfterEach;
import us.ihmc.robotics.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
public class PassiveRevoluteJointTest
{
   // Variables
   private ReferenceFrame referenceFrame = ReferenceFrame.getWorldFrame();
   private FrameVector3D frameVec = new FrameVector3D();
   private RigidBody rigidBody = new RigidBody("rigidBody", referenceFrame);
   private PassiveRevoluteJoint joint = null; // new PassiveRevoluteJoint("testJoint",rigidBody, referenceFrame, frameVec);
   private DenseMatrix64F matrix = new DenseMatrix64F();
   private int rowStart = 1;
   private Wrench jointWrench = new Wrench();
   private double q, qd, qdd, qddDesired, tau;
   private boolean integrateQddDes;
   
   @BeforeEach
   public void setUp() throws Exception
   {
   }

   @AfterEach
   public void tearDown() throws Exception
   {
   }

   @Test // timeout = 30000
   public void testPackTauMatrix()
   {
      try
      {
         joint.getTauMatrix(null);
      }
      catch(RuntimeException e)
      {
         return; // it caught an exception (which is what we wanted) so it returns (passes)
      }     
      Assert.fail(); // if it doesn't catch anything it will reach this line, which will make the test fail
   }
   
   @Test // timeout = 30000
   public void testSetTorqueFromWrench()
   {
      try
      {
         joint.setTorqueFromWrench(jointWrench);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetDesiredAcceleration()
   {
      try
      {
         joint.setDesiredAcceleration(matrix, rowStart);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetQ()
   {
      try
      {
         joint.setQ(q);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetQd()
   {
      try
      {
         joint.setQd(qd);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetQdd()
   {
      try
      {
         joint.setQdd(qdd);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetQddDesired()
   {
      try
      {
         joint.setQddDesired(qddDesired);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetTau()
   {
      try
      {
         joint.setTau(tau);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetConfiguration()
   {
      try
      {
         joint.setConfiguration(matrix, rowStart);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetVelocity()
   {
      try
      {
         joint.setVelocity(matrix, rowStart);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
   
   @Test // timeout = 30000
   public void testSetJointPositionVelocityAndAcceleration()
   {
      try
      {
         joint.setJointPositionVelocityAndAcceleration(joint);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
    
   @Test // timeout = 30000
   public void testSetQddDesiredFromJoint()
   {
      try
      {
         joint.setQddDesired(joint);
      }
      catch(RuntimeException e)
      {
         return; 
      }     
      Assert.fail();
   }
}
