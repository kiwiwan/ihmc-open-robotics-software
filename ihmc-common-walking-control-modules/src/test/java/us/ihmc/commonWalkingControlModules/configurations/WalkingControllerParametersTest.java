package us.ihmc.commonWalkingControlModules.configurations;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
@Tag("fast")
public class WalkingControllerParametersTest
{
   @Test(timeout = 30000)
   public void testNoParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      Class<?> c = Class.forName("us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters");
      Method[] allMethods = c.getDeclaredMethods();

      boolean foundMethodWithArgument = false;

      for (Method method : allMethods)
      {
         Class<?>[] parameterTypes = method.getParameterTypes();

         if (parameterTypes.length != 0)
         {
            PrintTools.info(method.getName() + " has arguments!");
            for (Class<?> parameter : parameterTypes)
            {
               System.out.println("   Argument type: " + parameter.getSimpleName());
            }

            foundMethodWithArgument = true;
         }
      }

      if (foundMethodWithArgument)
      {
         fail("This is a parameter class. It should not have methods that take arguments.");
      }
   }
}
