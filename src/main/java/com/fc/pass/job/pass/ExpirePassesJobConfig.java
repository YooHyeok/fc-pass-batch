package com.fc.pass.job.pass;

import com.fc.pass.repository.pass.PassEntity;
import com.fc.pass.repository.pass.PassStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;


@Configuration
public class ExpirePassesJobConfig {
    private final int CHUNK_SIZE = 5;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public ExpirePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job expirePassesJob() {
        return this.jobBuilderFactory.get("expirePassesJob")
                .start(expirePassesStep())
                .build();
    }

    @Bean
    public Step expirePassesStep() {
        return this.stepBuilderFactory.get("expirePassesStep")
                // Chunk Oriented Step 설정 <Input, Output> 타입을 제너릭으로 지정
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE)
                // Reader, Processor, Writer 을 등록된 빈으로부터 주입한다.
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build();
    }

    /**
     * ItemReader 빈 등록 <br/>
     * JpaCursorItemReader: Spring4.3 부터 신규 추가 <br/>
     * 페이징 기법 보다 높은 성능으로, 데이터 변경에 무관한 무결성 조회 가능. <br/>
     * [Cursor 기법] <br/>
     * Status가 PROGRESSED인 데이터들만 읽은 후 Status값들을 EXPIRED로 변경되게 되는데, <br/>
     * Paging일 경우 1, 2, 3페이지 와 같은 형태이기 때문에 누락이 될 수 있다. <br/>
     * (전체 페이지를 가져오지 않을 경우?) <br/>
     * 데이터 변경에 무관한, 무결성 조회가 가능한 cursor기법 사용 <br/>
     * @return
     */
    @Bean
    @StepScope
    public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
        // status 상태가 진행중이고, 만료일자이거나 보다 지난 조건의 Pass 만료 조회 쿼리
        String sql = "SELECT p FROM PassEntity p WHERE p.status = :status and p.endedAt <= :endedAt";
        // 파라미터 Map객체 : status : PROGRESSED(진행중) / endedAt : 현재시간
        Map<String, Object> parameter = Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now());
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory) //JPA기반을 사용하므로 EntityManagerFactory 빈을 주입한다.
                .queryString(sql) 
                .parameterValues(parameter)
                .build();
    }

    /**
     * 상태가 만료되고, 만료일자가 지난 PassEntity에 대해 상태값을 만료, 만료일자를 오늘로 일괄 수정한다. <br/>
     * Generic에 선언된 동일한 PassEntity의 의미는 각각 Input,Output 의 의미이며 <br/>
     * PassEntity를 입력으로 받아 처리한 후 해당 객체를 Output으로 출력 즉, 반환하게 된다. <br/>
     */
    @Bean
    public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED);
            passEntity.setExpiredAt(LocalDateTime.now());
            return passEntity;
        };
    }

    /**
     * expirePassesItemProcessor에서 적용한 엔티티 변경감지에 대한 Process작업을
     * 실질적으로 EntityManagerFactory를 주입받아 처리한다.
     * @return
     */
    @Bean
    public JpaItemWriter<PassEntity> expirePassesItemWriter() {
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

}
