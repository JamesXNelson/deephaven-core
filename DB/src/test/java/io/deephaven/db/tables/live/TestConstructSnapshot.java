package io.deephaven.db.tables.live;

import io.deephaven.base.SleepUtil;
import io.deephaven.db.v2.LiveTableTestCase;
import io.deephaven.db.v2.remote.ConstructSnapshot;
import io.deephaven.db.v2.sources.LogicalClock;
import org.apache.commons.lang3.mutable.MutableLong;

public class TestConstructSnapshot extends LiveTableTestCase {
    public void testClockChange() throws InterruptedException {
        final MutableLong changed = new MutableLong(0);
        final ConstructSnapshot.SnapshotControl control = new ConstructSnapshot.SnapshotControl() {
            @Override
            public Boolean usePreviousValues(long beforeClockValue) {
                // noinspection AutoBoxing
                return LogicalClock.getState(beforeClockValue) == LogicalClock.State.Updating;
            }

            @Override
            public boolean snapshotConsistent(final long currentClockValue, final boolean usingPreviousValues) {
                return true;
            }
        };
        Runnable snapshot_test =
                () -> ConstructSnapshot.callDataSnapshotFunction("snapshot test", control, (usePrev, beforeClock) -> {
                    SleepUtil.sleep(1000);
                    if (ConstructSnapshot.concurrentAttemptInconsistent()) {
                        changed.increment();
                    }
                    return true;
                });

        changed.setValue(0);
        final Thread t = new Thread(snapshot_test);
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        t.start();
        t.join();
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
        assertEquals(0, changed.longValue());

        changed.setValue(0);
        final Thread t2 = new Thread(snapshot_test);
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        t2.start();
        SleepUtil.sleep(100);
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
        t2.join();
        assertEquals(1, changed.longValue());
    }
}
