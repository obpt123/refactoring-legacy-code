package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.DistributedLock;
import cn.xpbootcamp.legacy_code.service.IdGenerator;
import cn.xpbootcamp.legacy_code.service.IdGeneratorImpl;
import cn.xpbootcamp.legacy_code.service.RedisDistributedLockImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;

import javax.transaction.InvalidTransactionException;

import com.spun.util.StringUtils;

public class WalletTransaction {
    private String id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String orderId;
    private Long createdTimestamp;
    private Double amount = Double.valueOf(0);
    private STATUS status;
    private String walletTransactionId;

    WalletService walletService = new WalletServiceImpl();
    DistributedLock distributedLock = new RedisDistributedLockImpl();
    IdGenerator idGenerator = new IdGeneratorImpl();

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        this.id = buildId(preAssignedId);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.orderId = orderId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.createdTimestamp = System.currentTimeMillis();
    }

    private String buildId(String preAssignedId) {
        if (StringUtils.isEmpty(preAssignedId)) {
            return preAssignedId.startsWith("t_") ? preAssignedId : "t_" + preAssignedId;
        } else {
            return "t_" + idGenerator.newId();
        }
    }

    public boolean execute() throws InvalidTransactionException {
        validateInput();
        if (status == STATUS.EXECUTED)
            return true;
        boolean isLocked = false;
        try {
            isLocked = distributedLock.lock(id);

            // 锁定未成功，返回false
            if (!isLocked) {
                return false;
            }
            if (status == STATUS.EXECUTED)
                return true; // double check

            if (hasExpired()) {
                this.status = STATUS.EXPIRED;
                return false;
            }
            String walletTransactionId = walletService.moveMoney(id, buyerId, sellerId, amount);
            if (walletTransactionId != null) {
                this.walletTransactionId = walletTransactionId;
                this.status = STATUS.EXECUTED;
                return true;
            } else {
                this.status = STATUS.FAILED;
                return false;
            }
        } finally {
            if (isLocked) {
                distributedLock.unlock(id);
            }
        }
    }

    private void validateInput() throws InvalidTransactionException {
        if (buyerId == null || sellerId == null || amount < 0.0) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
    }

    private boolean hasExpired() {
        long executionInvokedTimestamp = System.currentTimeMillis();
        return executionInvokedTimestamp - createdTimestamp > 20 * 24 * 3600 * 1000;
    }

}