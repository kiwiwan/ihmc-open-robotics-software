package us.ihmc.robotics.math;

import static us.ihmc.robotics.Assert.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class YoCounterTest
{
   @Test // timeout = 30000
   public void testCounter()
   {
      YoCounter counter = new YoCounter("numberOfThings", new YoVariableRegistry("countReg"));
      counter.setMaxCount(5);
      
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertTrue(counter.maxCountReached());
      counter.countOne();
      assertTrue(counter.maxCountReached());
      
      counter.resetCount();
      assertFalse(counter.maxCountReached());
      counter.countN(4);
      assertFalse(counter.maxCountReached());
      counter.countOne();
      assertTrue(counter.maxCountReached());
      
      counter.resetCount();
      counter.setMaxCount(3);
      assertFalse(counter.maxCountReached());
      counter.countN(3);
      assertTrue(counter.maxCountReached());
   }
}
