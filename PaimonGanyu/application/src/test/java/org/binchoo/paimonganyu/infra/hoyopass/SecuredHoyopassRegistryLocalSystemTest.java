package org.binchoo.paimonganyu.infra.hoyopass;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.binchoo.paimonganyu.chatbot.PaimonGanyuChatbotMain;
import org.binchoo.paimonganyu.hoyoapi.pojo.LtuidLtoken;
import org.binchoo.paimonganyu.hoyopass.Hoyopass;
import org.binchoo.paimonganyu.hoyopass.HoyopassCredentials;
import org.binchoo.paimonganyu.hoyopass.Uid;
import org.binchoo.paimonganyu.hoyopass.UserHoyopass;
import org.binchoo.paimonganyu.hoyopass.driven.SigningKeyManagerPort;
import org.binchoo.paimonganyu.hoyopass.driven.UserHoyopassCrudPort;
import org.binchoo.paimonganyu.infra.hoyopass.dynamo.item.UserHoyopassItem;
import org.binchoo.paimonganyu.service.hoyopass.SecuredHoyopassRegistry;
import org.binchoo.paimonganyu.testconfig.TestAmazonClientsConfig;
import org.binchoo.paimonganyu.testconfig.TestLtuidLtokenConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({
        TestAmazonClientsConfig.class,
        TestLtuidLtokenConfig.class,
        PaimonGanyuChatbotMain.class
})
class SecuredHoyopassRegistryLocalSystemTest {

    @Autowired
    SecuredHoyopassRegistry securedHoyopassRegistry;

    @Autowired
    SigningKeyManagerPort signingKeyManager;

    @Autowired
    UserHoyopassCrudPort userHoyopassCrud;

    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    @Autowired
    @Qualifier("valid0")
    TestLtuidLtokenConfig.ValidLtuidLtoken valid0;

    @Autowired
    @Qualifier("valid1")
    TestLtuidLtokenConfig.ValidLtuidLtoken valid1;

    @Autowired
    TestLtuidLtokenConfig.InvalidLtuidLtoken invalid0;

    @BeforeEach
    void cleanEntries() {
        new UserHoyopassTableBuilder(amazonDynamoDB).createTable();
        userHoyopassCrud.deleteAll();
    }

    @AfterEach
    void printEveryEntry() {
        System.out.println(userHoyopassCrud.findAll());
    }

    @Test
    void givenSecuredHoyopass_registerSecureHoyopass_successes() {
        String botUserId = "foobar";

        securedHoyopassRegistry.registerHoyopass(botUserId, getSecuredHoyopassString(valid0));
        UserHoyopass userHoyopass = userHoyopassCrud.findByBotUserId(botUserId)
                .orElseThrow(RuntimeException::new);

        assertThat(userHoyopass.getBotUserId()).isEqualTo(botUserId);
        assertThat(userHoyopass.getCount()).isEqualTo(1);
        assertThat(userHoyopass.getHoyopasses()).map(Hoyopass::getLtuid)
                .anyMatch(ltuid-> ltuid.equals(valid0.getLtuid()));
        assertThat(userHoyopass.getHoyopasses()).map(Hoyopass::getLtoken)
                .anyMatch(ltoken-> ltoken.equals(valid0.getLtoken()));
    }

    @Test
    void givenInvalidSecureHoyopass_registerSecureHoyopass_successes() {
        assertThrows(IllegalStateException.class, () ->
                securedHoyopassRegistry.registerHoyopass(
                        "foobar", "fakeSecureHoyopassString"));
    }

    @Test
    void givenOneHoyopass_registerHoyopass_successful() {
        registerHoyopass("a", valid0);
    }

    @Test
    void givenFakeHoyopass_registerHoyopass_fails() {
        assertThrows(IllegalArgumentException.class, ()->
                registerHoyopass("b", invalid0));
    }

    @Test
    void givenTwoHoyopasses_registerHoyopass_successful() {
        String botUserId = "c";

        UserHoyopass userHoyopass = registerHoyopasses(botUserId, valid0, valid1);

        assertThat(userHoyopass.getCount()).isEqualTo(2);
    }

    @Test
    void givenDuplicateHoyopasses_registeHoyopass_fails() {
        assertThrows(IllegalStateException.class, ()-> {
            registerHoyopass("d", valid0);
            registerHoyopass("d", valid0);
        });
    }

    @Test
    void givenHoyopassesForManyUsers_registeHoyopass_successful() {
        IntStream.of(1, 10).forEach(i-> {
            String botUserId = "botUser" + i;
            registerHoyopass(botUserId, valid1);
        });
    }

    @Test
    void listHoyopasses_successful() {
        String botUserId = "e";
        this.registerHoyopasses(botUserId, valid0, valid1);

        List<Hoyopass> hoyopasses = securedHoyopassRegistry.listHoyopasses(botUserId);

        assertThat(hoyopasses.size()).isEqualTo(2);
    }

    @Test
    void givenUnknownBotUserId_listHoyopasses_fails() {
        String botUserId = "botuserid uninserted";

        List<Hoyopass> hoyopasses = securedHoyopassRegistry.listHoyopasses(botUserId);

        assertThat(hoyopasses.size()).isEqualTo(0);
    }

    @Test
    void listUids_successful() {
        String botUserId = "g";
        UserHoyopass userHoyopass = registerHoyopasses(botUserId, valid0, valid1);

        List<Uid> uids = securedHoyopassRegistry.listUids(botUserId);

        userHoyopass.getHoyopasses().forEach((hoyopass)-> {
            assert(uids.containsAll(hoyopass.getUids()));
        });
    }

    @Test
    void whenHoyopassDesignated_listUids_successful() {
        String botUserId = "h";
        UserHoyopass userHoyopass = registerHoyopasses(botUserId, valid0, valid1);

        List<Uid> uids = securedHoyopassRegistry.listUids(botUserId, 0);
        assertThat(uids.containsAll(userHoyopass.listUids(0))).isTrue();

        uids = securedHoyopassRegistry.listUids(botUserId, 1);
        assertThat(uids.containsAll(userHoyopass.listUids(1))).isTrue();
    }

    @Test
    void deleteHoyopass() {
        String botUserId = "i";
        this.registerHoyopasses(botUserId, valid0, valid1);

        securedHoyopassRegistry.deleteHoyopass(botUserId, 0);

        List<Hoyopass> hoyopasses = securedHoyopassRegistry.listHoyopasses(botUserId);
        assertThat(hoyopasses.size()).isEqualTo(1);
    }

    private String getSecuredHoyopassString(LtuidLtoken ltuidLtoken) {
        PublicKey publicKey = signingKeyManager.getPublicKey();
        try {
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            String clientLtuidLtoken = String.format("%s:%s", ltuidLtoken.getLtuid(), ltuidLtoken.getLtoken());
            byte[] encryptedLtuidLtoken = cipher.doFinal(clientLtuidLtoken.getBytes(StandardCharsets.UTF_8));
            byte[] encodedSecureHoyopass = Base64.getEncoder().encode(encryptedLtuidLtoken);
            return new String(encodedSecureHoyopass); // secureHoyopass
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    private UserHoyopass registerHoyopasses(String botUserId, LtuidLtoken... ltuidLtokens) {
        UserHoyopass userHoyopass = null;
        for (LtuidLtoken ltuidLtoken : ltuidLtokens)
            userHoyopass = this.registerHoyopass(botUserId, ltuidLtoken);
        if (userHoyopass != null)
            assertThat(userHoyopass.getCount()).isEqualTo(ltuidLtokens.length);
        return userHoyopass;
    }

    private UserHoyopass registerHoyopass(String botUserId, LtuidLtoken ltuidLtoken) {
        return securedHoyopassRegistry.registerHoyopass(botUserId,
                HoyopassCredentials.builder()
                        .ltuid(ltuidLtoken.getLtuid())
                        .ltoken(ltuidLtoken.getLtoken())
                        .cookieToken(null) // it's ok
                        .build());
    }

    private static final class UserHoyopassTableBuilder {

        private final AmazonDynamoDB dynamoClient;

        public UserHoyopassTableBuilder(AmazonDynamoDB dynamoClient) {
            this.dynamoClient = dynamoClient;
        }

        public void createTable() {
            DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient);
            CreateTableRequest request = mapper.generateCreateTableRequest(UserHoyopassItem.class);
            request.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
            try {
                dynamoClient.createTable(request);
            } catch (Exception e) { // It's ok. Do nothing.
                e.printStackTrace();
            }
            dynamoClient.listTables().getTableNames().forEach(System.out::println);
        }
    }
}