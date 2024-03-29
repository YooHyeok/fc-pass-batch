package com.fc.pass.job.notification;

import com.fc.pass.repository.booking.BookingEntity;
import com.fc.pass.repository.booking.BookingStatus;
import com.fc.pass.repository.notification.NotificationEntity;
import com.fc.pass.repository.notification.NotificationEvent;
import com.fc.pass.repository.notification.NotificationModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class SendNotificationBeforeClassJobConfig {

    private final int CHUNK_SIZE = 10;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;

    public SendNotificationBeforeClassJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, SendNotificationItemWriter sendNotificationItemWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.sendNotificationItemWriter = sendNotificationItemWriter;
    }

    @Bean
    public Job sendNotificationBeforeClassJob() {
        return this.jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();
    }

    /*==================첫 번째 Step 시작 ====================== */

    @Bean
    public Step addNotificationStep() {
        System.out.println("addNotificationStep");
        return this.stepBuilderFactory.get("addNotificationStep")
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

    /**
     * 조회한 데이터에 대한 업데이트가 이루어지지 않으므로 Cursor를 사용할 필요가 없기 때문에 Paging으로 사용했다.
     */
    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE) //ChunkSize만큼의 데이터를 Paging기법을 적용하여 가져온다.
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();
    }

    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
        return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
        System.out.println("addNotificationItemWriter 호출되엠 ");
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
    /* ==================첫번째 Step 종료 ====================== */

    /* ==================두번째 Step 시작 ====================== */

    @Bean
    public Step sendNotificationStep() {
        return this.stepBuilderFactory.get("sendNotificationStep")
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter) // 클래스 타입 인스턴스 생성 후 의존성 주입
                .taskExecutor(new SimpleAsyncTaskExecutor())// Thread가 개속 생성되도록 할것인지 지정된 Thread Pool 내에서 제한된 개수의 Thread를 사용할것인지 변경될 수 있음
                .build();
    }

    /**
     * 이벤트(event)가 수업 전이며, 발송 여부(sent)가 미발송인 알림이 조회 대상이 된다.
     *
     * 멀티 스레드 환경에서는 Reader와 Wrtier가 Thread Safe한지 검증해야한다.
     * Cursor 기반의 방식은 Thread Safe하지 않는다.
     * Thread Safe 해야 하는 경우는 Paging기법을 주로 사용한다.
     * Notification 내에 있는 sent 데이터를 조회해 오고 sent 를 업데이트 해야한다.
     * 따라서 Paging으로 하게 되면 데이터가 누락이 될 수 있기 때문에 Cursor로 해야하는데 Thread Safe하지 않다.
     * 이런 경우에 사용하는것이 SynchronizedItemStreamReader이며, Synchronized하게 반환한다.
     * 어쩔수없이 위와같은 방법을 사용하게 되면 Reader는 순차적으로 실행되고, 뒷부분인 Processor, Writer부분은 멀티스레드로 진행하게 된다.
     * 보통은 Writer 부분이 Cost가 많이 들기 때문에 Writer부분이 먼저 Rest API로 호출하는 부분이 된다.
     */
    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false)) // (수업전, 미발송) 파라미터 데이터 바인딩
                .build();
        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();

    }
}
