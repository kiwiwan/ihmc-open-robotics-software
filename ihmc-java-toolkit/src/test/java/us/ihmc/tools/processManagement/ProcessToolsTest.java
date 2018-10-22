package us.ihmc.tools.processManagement;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.PrintTools;

public class ProcessToolsTest
{

	@Test
   public void testGetAllSystemProcesses()
   {
      ArrayList<String> processLines = ProcessTools.getAllSystemProcesses();
      
      for (String line : processLines)
         PrintTools.info(this, line);
   }
}
