package cn.xpbootcamp.legacy_code;

import javax.transaction.InvalidTransactionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

public class WalletTransactionTest {
    @Test
    public void shouldReturnTrueWhenExecuteAndCanLock() throws InvalidTransactionException {

        RedisDistributedLock redisDistributedLock = createDistributedLock(true);
        WalletService walletService = createWalletService("fake_moveMoneyId");
        WalletTransaction transaction = new WalletTransaction("fake_preId", 1L, 2L, 123L, "fake_orderId");
        setDistributedLock(transaction, redisDistributedLock);
        setWalletService(transaction, walletService);

        boolean executeResult = transaction.execute();

        assertEquals(true, executeResult);
    }

    @Test
    public void shouldReturnFalseWhenExecuteAndCannotLock() throws InvalidTransactionException {

        RedisDistributedLock redisDistributedLock = createDistributedLock(false);
        WalletService walletService = createWalletService("fake_moveMoneyId");
        WalletTransaction transaction = new WalletTransaction("fake_preId", 1L, 2L, 123L, "fake_orderId");
        setDistributedLock(transaction, redisDistributedLock);
        setWalletService(transaction, walletService);

        boolean executeResult = transaction.execute();

        assertEquals(false, executeResult);
    }

    @Test
    public void shouldReturnFalseWhenExecuteAndCannotMoveMoney() throws InvalidTransactionException {

        RedisDistributedLock redisDistributedLock = createDistributedLock(false);
        WalletService walletService = createWalletService(null);
        WalletTransaction transaction = new WalletTransaction("fake_preId", 1L, 2L, 123L, "fake_orderId");
        setDistributedLock(transaction, redisDistributedLock);
        setWalletService(transaction, walletService);

        boolean executeResult = transaction.execute();

        assertEquals(false, executeResult);
    }

    private RedisDistributedLock createDistributedLock(boolean lockedResult) {
        RedisDistributedLock mockedRedisLock = mock(RedisDistributedLock.class);
        when(mockedRedisLock.lock(anyString())).thenReturn(lockedResult);
        return mockedRedisLock;
    }

    private WalletService createWalletService(String moveMoneyId) {
        WalletService mockedWalletService = mock(WalletService.class);
        when(mockedWalletService.moveMoney(anyString(), anyLong(), anyLong(), anyDouble())).thenReturn(moveMoneyId);
        return mockedWalletService;
    }

    private void setDistributedLock(WalletTransaction walletTransaction, RedisDistributedLock distributedLock) {
        try {
            Field distributedLockField = WalletTransaction.class.getDeclaredField("distributedLock");
            FieldSetter.setField(walletTransaction, distributedLockField, distributedLock);
        } catch (Exception e) {
            throw new RuntimeException("Set distribute lock field error.", e);
        }
    }

    private void setWalletService(WalletTransaction walletTransaction, WalletService walletService) {
        try {
            Field walletServiceField = WalletTransaction.class.getDeclaredField("walletService");
            FieldSetter.setField(walletTransaction, walletServiceField, walletService);
        } catch (Exception e) {
            throw new RuntimeException("Set wallet service field error.", e);
        }
    }
}