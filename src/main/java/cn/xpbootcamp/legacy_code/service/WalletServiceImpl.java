package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;

public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();
    private IdGenerator idGenerator = new IdGeneratorImpl();

    public String moveMoney(String tranId, long buyerId, long sellerId, double amount) {
        User buyer = userRepository.find(buyerId);
        if (buyer.getBalance() < amount) {
            return null;
        }
        User seller = userRepository.find(sellerId);
        seller.setBalance(seller.getBalance() + amount);
        buyer.setBalance(buyer.getBalance() - amount);
        return idGenerator.newId() + tranId;
    }
}
