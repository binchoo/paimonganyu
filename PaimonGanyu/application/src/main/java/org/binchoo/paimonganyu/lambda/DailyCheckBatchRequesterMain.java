package org.binchoo.paimonganyu.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binchoo.paimonganyu.dailycheck.driving.DailyCheckService;
import org.binchoo.paimonganyu.hoyoapi.HoyolabDailyCheckApi;
import org.binchoo.paimonganyu.hoyoapi.autoconfig.HoyoApiWebClientConfigurer;
import org.binchoo.paimonganyu.hoyopass.driven.UserHoyopassCrudPort;
import org.binchoo.paimonganyu.infra.dailycheck.dynamo.repository.UserDailyCheckDynamoAdapter;
import org.binchoo.paimonganyu.infra.dailycheck.dynamo.repository.UserDailyCheckDynamoRepository;
import org.binchoo.paimonganyu.infra.dailycheck.web.DailyCheckClientAdapter;
import org.binchoo.paimonganyu.infra.hoyopass.dynamo.repository.UserHoyopassDynamoAdapter;
import org.binchoo.paimonganyu.infra.hoyopass.dynamo.repository.UserHoyopassDynamoRepository;
import org.binchoo.paimonganyu.lambda.config.DynamoDBClientConfig;
import org.binchoo.paimonganyu.lambda.config.SqsClientConfig;
import org.binchoo.paimonganyu.lambda.config.UserDailyCheckTableConfig;
import org.binchoo.paimonganyu.lambda.config.UserHoyopassTableConfig;
import org.binchoo.paimonganyu.service.dailycheck.DailyCheckServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        SqsClientConfig.class, HoyoApiWebClientConfigurer.class,
        DynamoDBClientConfig.class, UserHoyopassTableConfig.class, UserDailyCheckTableConfig.class
})
@Configuration
public class DailyCheckBatchRequesterMain {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * @param dailyCheckApi from {@link HoyoApiWebClientConfigurer}
     * @param repository from {@link UserDailyCheckTableConfig}
     */
    @Bean
    public DailyCheckService dailyCheckService(HoyolabDailyCheckApi dailyCheckApi,
                                               UserDailyCheckDynamoRepository repository) {
        return new DailyCheckServiceImpl(
                new DailyCheckClientAdapter(dailyCheckApi),
                new UserDailyCheckDynamoAdapter(repository));
    }

    /**
     * @param repository from {@link UserHoyopassTableConfig}
     */
    @Bean
    public UserHoyopassCrudPort userHoyopassCrudPort(UserHoyopassDynamoRepository repository) {
        return new UserHoyopassDynamoAdapter(repository);
    }
}