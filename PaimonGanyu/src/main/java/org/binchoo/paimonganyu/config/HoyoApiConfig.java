package org.binchoo.paimonganyu.config;

import org.binchoo.paimonganyu.hoyoapi.apis.HoyolabAccountApi;
import org.binchoo.paimonganyu.hoyoapi.apis.HoyolabDailyCheckApi;
import org.binchoo.paimonganyu.hoyoapi.apis.HoyolabGameRecordApi;
import org.binchoo.paimonganyu.hoyoapi.webclient.HoyolabAccountWebClient;
import org.binchoo.paimonganyu.hoyoapi.webclient.HoyolabDailyCheckWebClient;
import org.binchoo.paimonganyu.hoyoapi.webclient.HoyolabGameRecordWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "org.binchoo.paimonganyu.hoyoapi.error.aspect")
@Configuration
public class HoyoApiConfig {

    @Bean
    HoyolabAccountApi hoyolabAccountApi() {
        return new HoyolabAccountWebClient();
    }

    @Bean
    HoyolabGameRecordApi hoyolabGameRecordApi() {
        return new HoyolabGameRecordWebClient();
    }

    @Bean
    HoyolabDailyCheckApi hoyolabDailyCheckApi() {
        return new HoyolabDailyCheckWebClient();
    }
}
