package test.xyz.srclab.common.bean;

import org.apache.commons.beanutils.BeanUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import xyz.srclab.common.base.Chars;
import xyz.srclab.common.bean.BeanKit;
import xyz.srclab.common.test.TestTask;
import xyz.srclab.common.test.Tester;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author sunqian
 */
public class BeanTest {

    @Test
    public void testBean() {

        Instant instantNow = Instant.now();

        A a = new A();
        a.setP1(123);
        a.setP2(true);
        a.setP3("2233");
        a.setP4(instantNow);
        a.setP5(Chars.toBytes("Hello"));

        B b = BeanKit.copyProperties(a, new B());
        System.out.println(BeanKit.asMap(b));
        Assert.assertEquals(b.getP1(), "123");
        Assert.assertEquals(b.getP2(), "true");
        Assert.assertEquals(b.getP3(), 2233L);
        Assert.assertEquals(b.getP4(), instantNow.atZone(ZoneId.systemDefault()));
        Assert.assertEquals(b.getP5(), "Hello");
    }

    @Test
    public void testVsBeanutils() {

        class ResolverTest extends BeanTestTask {

            ResolverTest(int times) {
                super(times);
            }

            @Override
            protected void runTest() {
                TestBean from = createFrom();
                TestBean to = new TestBean();
                BeanKit.copyProperties(from, to);
            }
        }
        class BeanutilsTest extends BeanTestTask {

            BeanutilsTest(int times) {
                super(times);
            }

            @Override
            protected void runTest() {
                TestBean from = createFrom();
                TestBean to = new TestBean();
                try {
                    BeanUtils.copyProperties(to, from);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int times = 10000000;
        Tester.testTasksParallel(new ResolverTest(times), new BeanutilsTest(times));
    }

    static abstract class BeanTestTask implements TestTask {

        private final int times;

        BeanTestTask(int times) {
            this.times = times;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                runTest();
            }
        }

        protected abstract void runTest();
    }

    private TestBean createFrom() {
        TestBean from = new TestBean();
        from.setP1("123");
        from.setP2(123);
        from.setP3("456");
        from.setP4(456);
        return from;
    }

    public static class A {

        private int p1;
        private boolean p2;
        private String p3;
        private Instant p4;
        private byte[] p5;
        private List<Number> p6 = Arrays.asList(1, 2L, new BigDecimal("123.123"));
        private List<List<Number>> p7 = Arrays.asList(p6, p6, p6);

        public int getP1() {
            return p1;
        }

        public void setP1(int p1) {
            this.p1 = p1;
        }

        public boolean isP2() {
            return p2;
        }

        public void setP2(boolean p2) {
            this.p2 = p2;
        }

        public String getP3() {
            return p3;
        }

        public void setP3(String p3) {
            this.p3 = p3;
        }

        public Instant getP4() {
            return p4;
        }

        public void setP4(Instant p4) {
            this.p4 = p4;
        }

        public byte[] getP5() {
            return p5;
        }

        public void setP5(byte[] p5) {
            this.p5 = p5;
        }

        public List<Number> getP6() {
            return p6;
        }

        public void setP6(List<Number> p6) {
            this.p6 = p6;
        }

        public List<List<Number>> getP7() {
            return p7;
        }

        public void setP7(List<List<Number>> p7) {
            this.p7 = p7;
        }
    }

    public static class B {

        private CharSequence p1;
        private String p2;
        private long p3;
        private ZonedDateTime p4;
        private String p5;
        private List<Double> p6;
        private List<List<String>> p7;

        public CharSequence getP1() {
            return p1;
        }

        public void setP1(CharSequence p1) {
            this.p1 = p1;
        }

        public String getP2() {
            return p2;
        }

        public void setP2(String p2) {
            this.p2 = p2;
        }

        public long getP3() {
            return p3;
        }

        public void setP3(long p3) {
            this.p3 = p3;
        }

        public ZonedDateTime getP4() {
            return p4;
        }

        public void setP4(ZonedDateTime p4) {
            this.p4 = p4;
        }

        public String getP5() {
            return p5;
        }

        public void setP5(String p5) {
            this.p5 = p5;
        }

        public List<Double> getP6() {
            return p6;
        }

        public void setP6(List<Double> p6) {
            this.p6 = p6;
        }

        public List<List<String>> getP7() {
            return p7;
        }

        public void setP7(List<List<String>> p7) {
            this.p7 = p7;
        }
    }

    public static class TestBean {

        private String p1;
        private int p2;
        private String p3;
        private int p4;
        private String p5;
        private int p6;

        public String getP1() {
            return p1;
        }

        public void setP1(String p1) {
            this.p1 = p1;
        }

        public int getP2() {
            return p2;
        }

        public void setP2(int p2) {
            this.p2 = p2;
        }

        public String getP3() {
            return p3;
        }

        public void setP3(String p3) {
            this.p3 = p3;
        }

        public int getP4() {
            return p4;
        }

        public void setP4(int p4) {
            this.p4 = p4;
        }

        public String getP5() {
            return p5;
        }

        public void setP5(String p5) {
            this.p5 = p5;
        }

        public int getP6() {
            return p6;
        }

        public void setP6(int p6) {
            this.p6 = p6;
        }
    }
}