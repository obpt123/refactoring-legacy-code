package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.entity.Order;
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
    private static final long MAX_EXPIRED_MILLISECOND = 20 * 24 * 3600 * 1000; // 20 days
    private static final String TRAN_ID_PREFIX = "t_";

    private String transactionId;
    private Order order;
    private STATUS status = STATUS.TO_BE_EXECUTED;

    WalletService walletService = new WalletServiceImpl();
    DistributedLock distributedLock = new RedisDistributedLockImpl();
    IdGenerator idGenerator = new IdGeneratorImpl();

    public WalletTransaction(String preAssignedId, Order order) {
        this.transactionId = buildTransactionId(preAssignedId);
        this.order = order;
    }

    public WalletTransaction(String preAssignedId, long buyerId, long sellerId, long productId, String orderId, double amount) {
        this(preAssignedId, new Order() {
            {
                setAmount(amount);
                setBuyerId(buyerId);
                setSellerId(sellerId);
                setProductId(productId);
                setOrderId(orderId);
                setCreatedTimestamp(System.currentTimeMillis());
            }
        });
    }

    public boolean execute() throws InvalidTransactionException {
        validateOrder();
        if (hasExecuted()) {
            return true;
        }
        distributedLock.runWithLock(transactionId, () -> {
            if (hasExecuted()) {
                return;
            }
            if (hasExpired()) {
                status = STATUS.EXPIRED;
                return;
            }
            String moveMoneyResult = walletService.moveMoney(transactionId, order.getBuyerId(), order.getSellerId(),
                    order.getAmount());
            status = moveMoneyResult != null ? STATUS.EXECUTED : STATUS.FAILED;
        });
        return STATUS.EXECUTED == status;
    }

    private String buildTransactionId(String preAssignedId) {
        if (StringUtils.isEmpty(preAssignedId)) {
            return preAssignedId.startsWith(TRAN_ID_PREFIX) ? preAssignedId : "TRAN_ID_PREFIX" + preAssignedId;
        } else {
            return TRAN_ID_PREFIX + idGenerator.newId();
        }
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
        return System.currentTimeMillis() - order.getCreatedTimestamp() > MAX_EXPIRED_MILLISECOND;
    }

}