package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Order;
import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.DistributedLock;
import cn.xpbootcamp.legacy_code.service.IdGenerator;
import cn.xpbootcamp.legacy_code.service.IdGeneratorImpl;
import cn.xpbootcamp.legacy_code.service.RedisDistributedLockImpl;
import cn.xpbootcamp.legacy_code.service.SystemClocker;
import cn.xpbootcamp.legacy_code.service.SystemClockerImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;

import javax.transaction.InvalidTransactionException;

import com.spun.util.StringUtils;

public class WalletTransaction {
    private String id;

    private Order order;
    private STATUS status;
    private String walletTransactionId;

    WalletService walletService = new WalletServiceImpl();
    DistributedLock distributedLock = new RedisDistributedLockImpl();
    IdGenerator idGenerator = new IdGeneratorImpl();
    SystemClocker systemClocker = new SystemClockerImpl();

    public WalletTransaction(String preAssignedId, Order order) {
        this.id = buildId(preAssignedId);
        this.order = order;
        this.status = STATUS.TO_BE_EXECUTED;
    }

    public WalletTransaction(String preAssignedId, long buyerId, long sellerId, long productId, String orderId) {
        this(preAssignedId, new Order() {
            {
                setAmount(0);
                setBuyerId(buyerId);
                setSellerId(sellerId);
                setProductId(productId);
                setOrderId(orderId);
                setCreatedTimestamp(System.currentTimeMillis());
            }
        });
    }

    private String buildId(String preAssignedId) {
        if (StringUtils.isEmpty(preAssignedId)) {
            return preAssignedId.startsWith("t_") ? preAssignedId : "t_" + preAssignedId;
        } else {
            return "t_" + idGenerator.newId();
        }
    }

    public boolean execute() throws InvalidTransactionException {
        validateOrder();
        if (hasExecuted()) {
            return true;
        }
        distributedLock.runWithLock(this.id, () -> {
            if (hasExecuted()) {
                return;
            }
            if (hasExpired()) {
                status = STATUS.EXPIRED;
                return;
            }
            walletTransactionId = walletService.moveMoney(id, order.getBuyerId(), order.getSellerId(),
                    order.getAmount());
            if (walletTransactionId != null) {
                this.status = STATUS.EXECUTED;
            } else {
                this.status = STATUS.FAILED;
            }
        });
        return STATUS.EXECUTED == status;
    }

    private boolean hasExecuted() {
        return status == STATUS.EXECUTED;
    }

    private void validateOrder() throws InvalidTransactionException {
        if (order.getAmount() < 0.0) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
    }

    private boolean hasExpired() {
        long executionInvokedTimestamp = systemClocker.currentTimeMillis();
        return executionInvokedTimestamp - order.getCreatedTimestamp() > 20 * 24 * 3600 * 1000;
    }

}