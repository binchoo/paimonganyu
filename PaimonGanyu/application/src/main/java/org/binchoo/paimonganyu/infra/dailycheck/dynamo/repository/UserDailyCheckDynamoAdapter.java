package org.binchoo.paimonganyu.infra.dailycheck.dynamo.repository;

import lombok.RequiredArgsConstructor;
import org.binchoo.paimonganyu.dailycheck.UserDailyCheck;
import org.binchoo.paimonganyu.dailycheck.UserDailyCheckStatus;
import org.binchoo.paimonganyu.dailycheck.driven.UserDailyCheckCrudPort;
import org.binchoo.paimonganyu.infra.dailycheck.dynamo.item.UserDailyCheckItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserDailyCheckDynamoAdapter implements UserDailyCheckCrudPort {

    private final UserDailyCheckDynamoRepository dynamoRepository;

    @Override
    public UserDailyCheck save(UserDailyCheck userDailyCheck) {
        return UserDailyCheckItem.toDomain(dynamoRepository.save(UserDailyCheckItem.fromDomain(userDailyCheck)));
    }

    @Override
    public List<UserDailyCheck> findByBotUserIdLtuid(String botUserId, String ltuid) {
        String id = concat(botUserId, ltuid);
        return dynamoRepository.findByBotUserIdLtuid(id).stream()
                .map(UserDailyCheckItem::toDomain)
                .collect(Collectors.toList());
    }

    private String concat(String botUserId, String ltuid) {
        return botUserId + "-" + ltuid;
    }
}
