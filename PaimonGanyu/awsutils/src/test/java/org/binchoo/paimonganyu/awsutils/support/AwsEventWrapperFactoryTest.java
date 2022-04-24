package org.binchoo.paimonganyu.awsutils.support;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.binchoo.paimonganyu.awsutils.AwsEventWrapper;
import org.binchoo.paimonganyu.awsutils.dynamo.DynamodbEventWrapper;
import org.binchoo.paimonganyu.awsutils.s3.S3EventObjectReader;
import org.binchoo.paimonganyu.awsutils.sns.SNSEventWrapper;
import org.binchoo.paimonganyu.awsutils.sqs.SQSEventWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author : jbinchoo
 * @since : 2022-04-24
 */
class AwsEventWrapperFactoryTest {

    @DisplayName("AwsEventWrapperFactory 타입이 잘 로드된다.")
    @Test
    void clinit() {
        var foobar = AwsEventWrapperFactory.class;
    }

    @DisplayName("AwsEventWrapperFactory의 커스텀 버전을 생성할 수 있다.")
    @Test
    void configure() {
        var factory = AwsEventWrapperFactory.create(mappingManual -> {
            mappingManual.whenEvent(SQSEvent.class)
                    .wrapBy(CustomSQSEventWrapper.class);
        });
        var event = new SQSEvent();
        var expectedWrapper = new CustomSQSEventWrapper();

        var eventWrapper = factory.getCustomWrapper(event);

        assertThat(eventWrapper).hasSameClassAs(expectedWrapper);
    }

    @DisplayName("SQSEvent에 대해 명세된 이벤트 래퍼를 반환한다.")
    @Test
    void givenSQSEvent_returnsSpecifiedEventWrapper() {
        var event = new SQSEvent();
        var exepectedWraper = new SQSEventWrapper();

        var eventWrapper = AwsEventWrapperFactory.getWrapper(event);

        assertThat(eventWrapper).hasSameClassAs(exepectedWraper);
    }

    @DisplayName("SNSEvent에 대해 명세된 이벤트 래퍼를 반환한다.")
    @Test
    void givenSNSEvent_returnsSpecifiedEventWrapper() {
        var event = new SNSEvent();
        var exepectedWraper = new SNSEventWrapper();

        var eventWrapper = AwsEventWrapperFactory.getWrapper(event);

        assertThat(eventWrapper).hasSameClassAs(exepectedWraper);
    }

    @DisplayName("S3Event에 대해 명세된 이벤트 래퍼를 반환한다.")
    @Test
    void givenS3Event_returnsSpecifiedEventWrapper() {
        var event = new S3Event();
        var s3Client = AmazonS3ClientBuilder.defaultClient();
        var exepectedWraper = new S3EventObjectReader(s3Client);

        var eventWrapper = AwsEventWrapperFactory.getWrapper(event,s3Client);

        assertThat(eventWrapper).hasSameClassAs(exepectedWraper);
    }

    @DisplayName("DynamodbEvent에 대해 명세된 이벤트 래퍼를 반환한다.")
    @Test
    void givenDynamodbEvent_returnsSpecifiedEventWrapper() {
        var event = new DynamodbEvent();
        var dynamodbMapper = new DynamoDBMapper(AmazonDynamoDBClientBuilder.defaultClient());
        var exepectedWraper = new DynamodbEventWrapper(dynamodbMapper);

        var eventWrapper = AwsEventWrapperFactory.getWrapper(event, dynamodbMapper);

        assertThat(eventWrapper).hasSameClassAs(exepectedWraper);
    }

    @DisplayName("생성자 인자 없이는 S3Event에 대해 명세된 이벤트 래퍼를 반환할 수 없다.")
    @Test
    void givenS3EventAndInvalidConstructorArgs_returnsSpecifiedEventWrapper() {
        var event = new S3Event();

        assertThrows(NullPointerException.class, ()-> {
            AwsEventWrapperFactory.getWrapper(event, null);
        });

        assertThrows(ArrayIndexOutOfBoundsException.class, ()-> {
            AwsEventWrapperFactory.getWrapper(event);
        });
    }

    @DisplayName("생성자 인자 없이는 DynamodbEvent에 대해 명세된 이벤트 래퍼를 반환한다.")
    @Test
    void givenDynamodbEventAndInvalidConstructorArgs_returnsSpecifiedEventWrapper() {
        var event = new DynamodbEvent();

        assertThrows(NullPointerException.class, ()-> {
            AwsEventWrapperFactory.getWrapper(event, null);
        });
        assertThrows(ArrayIndexOutOfBoundsException.class, ()-> {
            AwsEventWrapperFactory.getWrapper(event);
        });
    }

    public static final class CustomSQSEventWrapper implements AwsEventWrapper<SQSEvent> {

        @Override
        public <T> List<T> extractPojos(SQSEvent event, Class<T> clazz) {
            return null;
        }
    }
}