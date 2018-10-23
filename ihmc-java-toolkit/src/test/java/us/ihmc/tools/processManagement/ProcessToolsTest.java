package us.ihmc.tools.processManagement;

import java.util.ArrayList;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
public class ProcessToolsTest
{

	@Test(timeout = 30000)
   public void testGetAllSystemProcesses()
   {
      ArrayList<String> processLines = ProcessTools.getAllSystemProcesses();
      
      for (String line : processLines)
         PrintTools.info(this, line);
   }
}
