package cn.xuanyuanli.core.util.snowflake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SnowflakeIdWorkerTest {

    private SnowflakeIdWorker idWorker;

    @BeforeEach
    void setUp() {
        idWorker = new SnowflakeIdWorker(1, 1);
    }

    @Test
    void testNextId() {
        long id1 = idWorker.nextId();
        long id2 = idWorker.nextId();
        assertNotEquals(id1, id2);
    }

    @Test
    void testNextIdWithClockBack() {
        idWorker.setClock(true);
        try (MockedStatic<SystemClock> mockedClock = mockStatic(SystemClock.class)) {
            mockedClock.when(SystemClock::now).thenReturn(-2L);
            assertThrows(RuntimeException.class, () -> idWorker.nextId());
        }
    }

    @Test
    void testParseUid() {
        long id = idWorker.nextId();
        String parsedUid = idWorker.parseUid(id);
        assertNotNull(parsedUid);
        assertTrue(parsedUid.contains("\"UID\":"));
    }

    @Test
    void testParseUidString() {
        long id = idWorker.nextId();
        String parsedUid = idWorker.parseUid(String.valueOf(id));
        assertNotNull(parsedUid);
        assertTrue(parsedUid.contains("\"UID\":"));
    }

    @Test
    void testTilNextMillis() {
        long lastTimestamp = System.currentTimeMillis();
        long nextTimestamp = idWorker.tilNextMillis(lastTimestamp);
        assertTrue(nextTimestamp > lastTimestamp);
    }

    @Test
    void testTimeGen() {
        idWorker.setClock(true);
        try (MockedStatic<SystemClock> mockedClock = mockStatic(SystemClock.class)) {
            mockedClock.when(SystemClock::now).thenReturn(1000L);
            assertEquals(1000L, idWorker.timeGen());
        }
    }

    @Test
    void testInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdWorker(-1, 1));
    }

    @Test
    void testInvalidDatacenterId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdWorker(1, -1));
    }

    @Test
    void parseUidLong() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);
        assertEquals(worker.parseUid(583659091574915072L),"{\"UID\":\"583659091574915072\",\"timestamp\":\"2022-05-23 14:12:51\",\"workerId\":\"0\",\"dataCenterId\":\"0\",\"sequence\":\"0\"}");
    }

    @Test
    void parseUidString() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);
        assertEquals(worker.parseUid("583659091574915072"),"{\"UID\":\"100000011001100100101101100111110111010000000000000000000000\",\"timestamp\":\"2022-05-23 14:12:51\",\"workerId\":\"0\",\"dataCenterId\":\"0\",\"sequence\":\"0\"}");
    }
}
