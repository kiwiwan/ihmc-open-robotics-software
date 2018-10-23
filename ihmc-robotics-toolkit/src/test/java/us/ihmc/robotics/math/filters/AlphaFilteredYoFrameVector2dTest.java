package us.ihmc.robotics.math.filters;

import java.util.Random;

import org.junit.After;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;
import us.ihmc.euclid.tools.EuclidCoreTestTools;
import us.ihmc.euclid.tuple2D.Vector2D;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class AlphaFilteredYoFrameVector2dTest
{
   private static final double EPSILON = 1.0e-15;

   @After
   public void tearDown()
   {
      ReferenceFrameTools.clearWorldFrameTree();
   }

   @Test // timeout = 1000
   public void testConsistencyWithAlphaFilteredYoVariable()
   {
      Random random = new Random(3453456);

      for (int i = 0; i < 100; i++)
      {
         double alpha = random.nextDouble();
         YoVariableRegistry registry = new YoVariableRegistry("blop");

         AlphaFilteredYoFrameVector2d filteredVector = new AlphaFilteredYoFrameVector2d("tested", "", registry, alpha, ReferenceFrame.getWorldFrame());
         AlphaFilteredYoVariable xFiltered = new AlphaFilteredYoVariable("xRef", registry, alpha);
         AlphaFilteredYoVariable yFiltered = new AlphaFilteredYoVariable("yRef", registry, alpha);

         Vector2D unfilteredVector = new Vector2D();

         for (int j = 0; j < 10; j++)
         {
            unfilteredVector.add(EuclidCoreRandomTools.nextVector2D(random, 0.0, 0.5));

            filteredVector.update(unfilteredVector);
            xFiltered.update(unfilteredVector.getX());
            yFiltered.update(unfilteredVector.getY());

            EuclidCoreTestTools.assertTuple2DEquals(new Vector2D(xFiltered.getValue(), yFiltered.getValue()), filteredVector, EPSILON);
         }
      }
   }
}
