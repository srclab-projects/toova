@file:JvmName("Tester")

package xyz.srclab.common.test

import xyz.srclab.common.base.INAPPLICABLE_JVM_NAME
import xyz.srclab.common.base.toTimestamp
import xyz.srclab.common.run.AsyncRunner
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor

fun testTasks(vararg testTasks: TestTask) {
    testTasks(testTasks.toList())
}

fun testTasks(testTasks: Iterable<TestTask>) {
    testTasks0(testTasks = testTasks)
}

fun testTasks(testListener: TestListener, vararg testTasks: TestTask) {
    testTasks(testListener, testTasks.toList())
}

fun testTasks(testListener: TestListener, testTasks: Iterable<TestTask>) {
    testTasks0(AsyncRunner, testListener, testTasks)
}

fun testTasksParallel(vararg testTasks: TestTask) {
    testTasksParallel(testTasks.toList())
}

fun testTasksParallel(testTasks: Iterable<TestTask>) {
    testTasks0(executor = AsyncRunner, testTasks = testTasks)
}

fun testTasksParallel(testListener: TestListener, vararg testTasks: TestTask) {
    testTasksParallel(testListener, testTasks.toList())
}

fun testTasksParallel(testListener: TestListener, testTasks: Iterable<TestTask>) {
    testTasks0(testListener = testListener, testTasks = testTasks)
}

fun testTasks(executor: Executor, vararg testTasks: TestTask) {
    testTasks(executor, testTasks.toList())
}

fun testTasks(executor: Executor, testTasks: Iterable<TestTask>) {
    testTasks0(executor = executor, testTasks = testTasks)
}

fun testTasks(executor: Executor, testListener: TestListener, vararg testTasks: TestTask) {
    testTasks(executor, testListener, testTasks.toList())
}

fun testTasks(executor: Executor, testListener: TestListener, testTasks: Iterable<TestTask>) {
    testTasks0(executor, testListener, testTasks)
}

private fun testTasks0(
    executor: Executor = Executor { command -> command.run() },
    testListener: TestListener = TestListener.DEFAULT,
    testTasks: Iterable<TestTask>,
) {
    val tasks = testTasks.toList()
    val counter = CountDownLatch(tasks.size)

    testListener.beforeRunAll(tasks)

    val costs = mutableListOf<Duration>()

    val awaitStartTime = LocalDateTime.now()
    for (testTask in testTasks) {
        executor.execute {
            testListener.beforeRunEach(testTask)
            val startTime = LocalDateTime.now()
            testTask.run()
            val endTime = LocalDateTime.now()
            val cost = Duration.between(startTime, endTime)
            testListener.afterRunEach(testTask, object : TestTaskResult {
                override val cost: Duration = cost
            })
            synchronized(counter) {
                costs.add(cost)
                counter.countDown()
            }
        }
    }
    counter.await()

    val awaitCost = Duration.between(awaitStartTime, LocalDateTime.now())
    val totalCost = costs.reduce { d1, d2 -> d1.plus(d2) }
    val averageCost = totalCost.dividedBy(tasks.size.toLong())
    testListener.afterRunAll(tasks, object : TestResult {
        override val awaitCost: Duration = awaitCost
        override val totalCost: Duration = totalCost
        override val averageCost: Duration = averageCost
    })
}

interface TestTask {

    @Suppress(INAPPLICABLE_JVM_NAME)
    @JvmDefault
    val name: String
        @JvmName("name") get() = this.javaClass.name

    fun run()

    companion object {

        @JvmStatic
        fun newTask(times: Long, task: () -> Unit): TestTask {
            return object : TestTask {
                override fun run() {
                    for (i in 1..times) {
                        task()
                    }
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        fun newTask(name: String? = null, times: Long = 1, task: () -> Unit): TestTask {
            return object : TestTask {
                override val name: String = name ?: this.javaClass.name
                override fun run() {
                    for (i in 1..times) {
                        task()
                    }
                }
            }
        }
    }
}

interface TestListener {

    fun beforeRunAll(testTasks: List<TestTask>)

    fun beforeRunEach(testTask: TestTask)

    fun afterRunEach(testTask: TestTask, testTaskResult: TestTaskResult)

    fun afterRunAll(testTasks: List<TestTask>, testResult: TestResult)

    companion object {

        @JvmField
        val EMPTY: TestListener = object : TestListener {

            override fun beforeRunAll(testTasks: List<TestTask>) {}

            override fun beforeRunEach(testTask: TestTask) {}

            override fun afterRunEach(testTask: TestTask, testTaskResult: TestTaskResult) {}

            override fun afterRunAll(testTasks: List<TestTask>, testResult: TestResult) {}
        }

        @JvmField
        val DEFAULT: TestListener = withTestLogger(TestLogger.DEFAULT)

        @JvmStatic
        fun withTestLogger(testLogger: TestLogger): TestListener {
            return object : TestListener {

                override fun beforeRunAll(testTasks: List<TestTask>) {
                    testLogger.log("At ${ZonedDateTime.now().toTimestamp()} prepare to run all tasks...")
                }

                override fun beforeRunEach(testTask: TestTask) {
                    testLogger.log("Run task ${testTask.name}...")
                }

                override fun afterRunEach(testTask: TestTask, testTaskResult: TestTaskResult) {
                    testLogger.log("Task ${testTask.name} was accomplished, cost: ${testTaskResult.cost}")
                }

                override fun afterRunAll(testTasks: List<TestTask>, testResult: TestResult) {
                    testLogger.log(
                        "All tasks were accomplished, " +
                                "await cost: ${testResult.awaitCost}, " +
                                "total cost: ${testResult.totalCost}, " +
                                "average cost: ${testResult.averageCost}"
                    )
                }
            }
        }
    }
}

interface TestTaskResult {

    @Suppress(INAPPLICABLE_JVM_NAME)
    val cost: Duration
        @JvmName("cost") get
}

interface TestResult {

    @Suppress(INAPPLICABLE_JVM_NAME)
    val awaitCost: Duration
        @JvmName("awaitCost") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val totalCost: Duration
        @JvmName("totalCost") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val averageCost: Duration
        @JvmName("averageCost") get
}