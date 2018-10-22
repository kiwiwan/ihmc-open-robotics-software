package us.ihmc.humanoidRobotics.kryo;

import org.junit.jupiter.api.Test;

import us.ihmc.communication.kryo.KryoNetClassListTestHelper;

public class IHMCCommunicationKryoNetClassListTest
{
   @Test
   public void testAllClassesRegisteredArePackets()
   {
      IHMCCommunicationKryoNetClassList netClassList = new IHMCCommunicationKryoNetClassList();
      KryoNetClassListTestHelper.testAllClassesRegisteredArePackets(netClassList);
   }

   @Test
   public void testAllPacketFieldsAreRegistered()
         throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException
   {
      IHMCCommunicationKryoNetClassList netClassList = new IHMCCommunicationKryoNetClassList();
      KryoNetClassListTestHelper.testAllPacketFieldsAreRegistered(netClassList);
   }
}
