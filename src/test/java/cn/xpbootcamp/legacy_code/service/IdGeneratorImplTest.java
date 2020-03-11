package cn.xpbootcamp.legacy_code.service;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class IdGeneratorImplTest {
    @Test
    public void shouldReturnUniqueStringWhenNewId() {
        IdGenerator idGenerator=new IdGeneratorImpl();
        int loopCount = 10000;
        long totalIds = IntStream.range(0, loopCount).boxed().map((p) -> idGenerator.newId()).distinct()
                .count();
        assertEquals(loopCount, totalIds);
    }
}