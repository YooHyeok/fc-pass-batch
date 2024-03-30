package com.fc.pass.job.pass;

import com.fc.pass.repository.booking.BookingEntity;
import com.fc.pass.repository.booking.BookingRepository;
import com.fc.pass.repository.booking.BookingStatus;
import com.fc.pass.repository.pass.PassEntity;
import com.fc.pass.repository.pass.PassRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;

import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 수업 종료 후 이용권 차감
 * ItemReader를 통해 예약상태가 완료이며, 사용되지 않은 이용권에 대해 예약 종료일시가 현재시점에서 과거인 예약 조회후
 * ItemProcessor를 통해 해당 예약에 등록된 이용권의 잔여 횟수를 차감하고, 이용권 사용여부를 초기화한 뒤
 * ItemWriter를 통해 Pass를 수정하고, pass수정이 완료되면 Booking을 수정하도록 처리한다.
 * Processor와 Writer는 모두 Async방식을 통해 멀티스레드로 구성하여 병렬로 작업 처리를 하였다.
 */
@Configuration
public class UsePassesJobConfig {
    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;

    public UsePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, PassRepository passRepository, BookingRepository bookingRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
    }

    @Bean
    public Job usePassesJob() {
        return this.jobBuilderFactory.get("usePassesJob")
                .start(usePassesStep())
                .build();
    }

    @Bean
    public Step usePassesStep() {
        return this.stepBuilderFactory.get("usePassesStep")
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE)// Futrue는 Promise와 비슷
                .reader(usePassItemReader())
                .processor(usePassesAsyncItemProcessor())
                .writer(usePassesAsyncItemWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<BookingEntity> usePassItemReader() {
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassItemReader")
                .entityManagerFactory(entityManagerFactory)
                // 예약상태가 완료이며, 사용되지 않은 이용권에 대해 예약 종료일시가 현재시점에서 과거인 예약이 이용권 차감 대상이 된다.
                .queryString("SELECT b FROM BookingEntity b JOIN FETCH b.passEntity WHERE b.status = :status and b.usedPass = false and b.endedAt < :endedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now()))
                .build();

    }

    @Bean
    public AsyncItemProcessor<BookingEntity, BookingEntity> usePassesAsyncItemProcessor() {
        AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(usePassesItemProccessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProccessor() {
        return bookingEntity -> {
            //이용권 잔여 횟수 차감
            PassEntity passEntity = bookingEntity.getPassEntity();
            passEntity.setRemainingCount(passEntity.getRemainingCount() - 1);
            bookingEntity.setPassEntity(passEntity);

            //이용권 사용 여부 업데이트
            bookingEntity.setUsedPass(true);
            return bookingEntity;
        };
    }

    @Bean
    public AsyncItemWriter<BookingEntity> usePassesAsyncItemWriter() {
        AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(usePassesItemWriter());
        return asyncItemWriter;
    }

    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter() {
        return bookingEntities -> {
            for (BookingEntity bookingEntity: bookingEntities) {
                //잔여 횟수 업데이트
                int updateCount = passRepository.updateRemainingCount(bookingEntity.getPassSeq(), bookingEntity.getPassEntity().getRemainingCount());
                //잔여 횟수 업데이트 완료시 이용권 사용 여부 업데이트
                if (updateCount > 0) {
                    bookingRepository.updateUsedPass(bookingEntity.getPassSeq(), bookingEntity.isUsedPass());
                }

            }
        };
    }
}
