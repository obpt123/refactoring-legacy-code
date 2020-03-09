package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.*;

public class WalletServiceImplTest {
    private static final double COMPARE_DELTA = 0.00001;

    @Test
    public void shouldSuccessWhenMoveMoneyGivenAmountLessThanBuyerBalance() {

        User buyer = createUser(1, 100.0);
        User seller = createUser(2, 50.0);
        UserRepository mockedUserRepo = createMockedUserRepo(buyer, seller);
        WalletService walletService = createWalletServiceWithUserRepo(mockedUserRepo);

        String result = walletService.moveMoney("fake_id", 1, 2, 80);

        assertTrue(() -> result.endsWith("fake_id"));
        assertEquals(20, buyer.getBalance(), COMPARE_DELTA);
        assertEquals(130, seller.getBalance(), COMPARE_DELTA);
    }

    @Test
    public void shouldFailureWhenMoveMoneyGivenAmountGreatThanBuyerBalance()
            throws NoSuchFieldException, SecurityException {
        User buyer = createUser(1, 100.0);
        User seller = createUser(2, 50.0);
        UserRepository mockedUserRepo = createMockedUserRepo(buyer, seller);
        WalletService walletService = createWalletServiceWithUserRepo(mockedUserRepo);

        String result = walletService.moveMoney("fake_id", 1, 2, 180);

        assertEquals(null, result);
        assertEquals(100, buyer.getBalance(), COMPARE_DELTA);
        assertEquals(50, seller.getBalance(), COMPARE_DELTA);
    }

    private User createUser(long id, double balance) {
        User user = new User();
        user.setId(id);
        user.setBalance(balance);
        return user;
    }

    private UserRepository createMockedUserRepo(User buyer, User seller) {
        UserRepository mockedUserRepo = mock(UserRepository.class);
        when(mockedUserRepo.find(buyer.getId())).thenReturn(buyer);
        when(mockedUserRepo.find(seller.getId())).thenReturn(seller);
        return mockedUserRepo;
    }

    private WalletService createWalletServiceWithUserRepo(UserRepository userRepo) {
        try {
            WalletServiceImpl walletServiceImpl = new WalletServiceImpl();
            Field userRepoField = WalletServiceImpl.class.getDeclaredField("userRepository");
            FieldSetter.setField(walletServiceImpl, userRepoField, userRepo);
            return walletServiceImpl;
        } catch (Exception e) {
            throw new RuntimeException("create instance error.");
        }
    }
}