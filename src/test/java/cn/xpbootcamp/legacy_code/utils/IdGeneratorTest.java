package cn.xpbootcamp.legacy_code.utils;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class IdGeneratorTest {
    @Test
    public void shouldReturnUniqueStringWhenGenerateTransactionId() {
        int loopCount = 10000;
        long totalIds = IntStream.range(0, loopCount).boxed().map((p) -> IdGenerator.generateTransactionId()).distinct()
                .count();
        assertEquals(loopCount, totalIds);
    }
}