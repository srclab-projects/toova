package test.java.xyz.srclab.common.collect;

import org.testng.Assert;
import org.testng.annotations.Test;
import xyz.srclab.common.base.Nums;
import xyz.srclab.common.collect.ArrayCollects;
import xyz.srclab.common.collect.Collects;
import xyz.srclab.common.collect.ListOps;
import xyz.srclab.common.test.TestLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author sunqian
 */
public class CollectsTest {

    private static final TestLogger logger = TestLogger.DEFAULT;

    @Test
    public void testArray() {
        String[] stringArray = ArrayCollects.newArray("1", "2", "3");
        logger.log(ArrayCollects.joinToString(stringArray));
        Assert.assertEquals(
                ArrayCollects.joinToString(stringArray),
                "1, 2, 3"
        );
    }

    @Test
    public void testList() {
        String[] strings = ArrayCollects.newArray("1", "2", "3");
        ArrayCollects.asList(strings).set(0, "111");
        ArrayCollects.asList(strings).set(1, "222");
        ArrayCollects.asList(strings).set(2, "333");
        Assert.assertEquals(
                ArrayCollects.joinToString(strings),
                "111, 222, 333"
        );
        Assert.assertEquals(
                Collects.joinToString(ArrayCollects.asList(strings)),
                "111, 222, 333"
        );

        int[] ints = {1, 2, 3};
        ArrayCollects.asList(ints).set(0, 111);
        ArrayCollects.asList(ints).set(1, 222);
        ArrayCollects.asList(ints).set(2, 333);
        Assert.assertEquals(
                ArrayCollects.joinToString(ints),
                "111, 222, 333"
        );
        Assert.assertEquals(
                Collects.joinToString(ArrayCollects.asList(ints)),
                "111, 222, 333"
        );
    }

    @Test
    public void testOps() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        ListOps<String> listOps = ListOps.opsFor(list);
        int sum = listOps.addAll(ArrayCollects.newArray("4", "5", "6"))
                .removeFirst()
                .map(it -> it + "0")
                .map(Nums::toInt)
                .reduce(Integer::sum);
        Assert.assertEquals(sum, 200);

        int[] ints = {1, 2, 3};
        ListOps<Integer> listOps2 = ListOps.opsFor(ArrayCollects.asList(ints));
        int sum2 = listOps2.reduce(Integer::sum);
        Assert.assertEquals(sum2, 6);
        Assert.expectThrows(UnsupportedOperationException.class, () -> {
            listOps2.addAll(Arrays.asList(4, 5, 6));
        });
    }
}
