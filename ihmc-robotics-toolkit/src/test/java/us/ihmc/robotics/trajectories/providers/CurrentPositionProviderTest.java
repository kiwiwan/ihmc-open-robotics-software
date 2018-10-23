package us.ihmc.robotics.trajectories.providers;

import static us.ihmc.robotics.Assert.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;

public class CurrentPositionProviderTest
{
   private ReferenceFrame referenceFrame;
   private CurrentPositionProvider provider;

   @BeforeEach
   public void setUp()
   {
      referenceFrame = ReferenceFrame.constructARootFrame("rootNameTEST");
   }

   @AfterEach
   public void tearDown()
   {
      referenceFrame = null;
   }

	@Test // timeout = 30000
   public void testConstructor()
   {
      provider = new CurrentPositionProvider(null);
      provider = new CurrentPositionProvider(referenceFrame);
   }

	@Test // timeout = 30000
   public void testGet()
   {
      provider = new CurrentPositionProvider(referenceFrame);
      FramePoint3D framePointToPack = new FramePoint3D(referenceFrame);

      provider.getPosition(framePointToPack);

      assertEquals(referenceFrame, framePointToPack.getReferenceFrame());
   }
}
