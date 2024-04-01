package com.fc.pass.job.statistics;

import com.fc.pass.repository.booking.BookingEntity;
import com.fc.pass.repository.statistics.StatisticsEntity;
import com.fc.pass.repository.statistics.StatisticsRepository;
import com.fc.pass.util.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class MakeStatisticsJobConfig {
    private final int CHUNK_SIZE = 10;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final StatisticsRepository statisticsRepository;
    private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
    private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;

    public MakeStatisticsJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory,
       StatisticsRepository statisticsRepository, MakeDailyStatisticsTasklet makeDailyStatisticsTasklet, MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.statisticsRepository = statisticsRepository;
        this.makeDailyStatisticsTasklet = makeDailyStatisticsTasklet;
        this.makeWeeklyStatisticsTasklet = makeWeeklyStatisticsTasklet;
    }
    @Bean
    public Job makeStatisticsJob() {
        /* Statistics 생성 */
        Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
                .start(addStatisticsStep())
                .build();
        /* 일간 Statistics 통계 */
        Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
                .start(makeDailyStatisticsStep())
                .build();
        /* 주간 Statistics 통계 */
        Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
                .start(makeWeeklyStatisticsStep())
                .build();
        /* 병렬 작업에 대한 Flow - 일간/주간 통계를 병렬로 묶는다. */
        Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow) // Daily Flow와 Weekly Flow를 병렬로 등록한다.
                .build();

        return this.jobBuilderFactory.get("makeStatisticsJob")
                .start(addStatisticsFlow)
                .next(parallelMakeStatisticsFlow)
                .build().build();
    }

    /**
     * Booking 데이터를 읽어온 뒤 Statistics를 생성하는 Step
     * @return
     */
    @Bean
    public Step addStatisticsStep() {
        return stepBuilderFactory.get("addStatisticsStep")
                .<BookingEntity, BookingEntity>chunk(CHUNK_SIZE)
                .reader(addStatisticsItemReader(null, null)) // Reader에서 JobParameter를 사용했으므로 null을 넣는다.
                .writer(addStatisticsItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(@Value("#{jobParameters[from]}") String fromString, @Value("#{jobParameters[to]}") String toString) {
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("addStatisticsItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b where b.endedAt between :from and :to")
                .parameterValues(Map.of("from", from, "to", to))
                .build();
    }

    @Bean
    public ItemWriter<BookingEntity> addStatisticsItemWriter() {
        return bookingEntities -> {
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();
            for (BookingEntity bookingEntity : bookingEntities) {
                final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt(); // Key값으로 쓴다.
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);
                if (statisticsEntity == null) { //Statistics 값이 없으면 Statistics를 새로생성해서 넣고, 값이 있으면 카운트를 업데이트한다.
                    statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(bookingEntity));
                } else {
                    statisticsEntity.add(bookingEntity); // 전체 count를 증가시키고 예약의 출석여부/취소상태에 대해 확인하여 조건에 부합하는 count를 증가시킨다.
                }

            }
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            statisticsRepository.saveAll(statisticsEntities); // Statistics 모두 저장
        };
    }

    /**
     * Statistics 값을 읽고 일간으로 CSV파일을 만들어내는 작업을 하는 <br/>
     * MakeDailyStatisticsTasklet을 Tasklet으로 등록한다
     * @return
     */
    @Bean
    public Step makeDailyStatisticsStep() {
        return this.stepBuilderFactory.get("makeDailyStatisticsStep")
                .tasklet(makeDailyStatisticsTasklet)
                .build();
    }

    /**
     * Statistics 값을 읽고 주간으로 CSV파일을 만들어내는 작업을 하는 <br/>
     * MakeWeeklyStatisticsStep Tasklet으로 등록한다
     * @return
     */
    @Bean
    public Step makeWeeklyStatisticsStep() {
        return this.stepBuilderFactory.get("makeWeeklyStatisticsStep")
                .tasklet(makeWeeklyStatisticsTasklet)
                .build();

    }

}
