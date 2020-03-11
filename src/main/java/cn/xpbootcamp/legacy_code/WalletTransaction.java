package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.IdGenerator;
import cn.xpbootcamp.legacy_code.service.IdGeneratorImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;

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
    RedisDistributedLock distributedLock = new RedisDistributedLock();
    IdGenerator idGenerator = new IdGeneratorImpl();

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.id = preAssignedId;
        } else {
            this.id = idGenerator.newId();
        }
        if (!this.id.startsWith("t_")) {
            this.id = "t_" + preAssignedId;
        }
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.orderId = orderId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public boolean execute() throws InvalidTransactionException {
        if (buyerId == null || (sellerId == null || amount < 0.0)) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
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
            long executionInvokedTimestamp = System.currentTimeMillis();
            // 交易超过20天
            if (executionInvokedTimestamp - createdTimestamp > 1728000000) {
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

}