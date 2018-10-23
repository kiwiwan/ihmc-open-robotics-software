package us.ihmc.simulationConstructionSetTools.whiteBoard;


import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
@Tag("flaky")

public class TCPYoWhiteBoardTest extends YoWhiteBoardTest
{

	@Test // timeout = 30000
   public void testTCPWhiteBoardOne() throws IOException
   {
      String IPAddress = "localHost";
      int port = 8456;

      TCPYoWhiteBoard leftWhiteBoard = new TCPYoWhiteBoard("leftTest", port);
      TCPYoWhiteBoard rightWhiteBoard = new TCPYoWhiteBoard("rightTest", IPAddress, port);
      
      leftWhiteBoard.startTCPThread();
      rightWhiteBoard.startTCPThread();

      int numberOfTests = 500;
      doASynchronizedWriteThenReadTest(leftWhiteBoard, rightWhiteBoard, numberOfTests, 501, 1001);
   }

	@Test // timeout = 30000
   public void testTCPWhiteBoardTwo() throws IOException
   {
      String IPAddress = "localHost";
      int port = 8456;

      TCPYoWhiteBoard leftWhiteBoard = new TCPYoWhiteBoard("leftTest", port);
      TCPYoWhiteBoard rightWhiteBoard = new TCPYoWhiteBoard("rightTest", IPAddress, port);

      leftWhiteBoard.startTCPThread();
      rightWhiteBoard.startTCPThread();

      int numberOfTests = 500;
      doAnAsynchronousTest(leftWhiteBoard, rightWhiteBoard, numberOfTests, 500, 1000);
   }
}
