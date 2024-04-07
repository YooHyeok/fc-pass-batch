package com.fc.pass.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing // Batch를 위한 Processing 허용 설정 - JobBuilderFactory, StepBuilderFactory가 Bean으로 등록된다.
@Configuration
public class BatchConfig {
    /**
     * JobRegistry는 context에서 Job을 추적할 때 유용하다.
     * JobRegistryProcessor는 Application Context가 올라가면서 bean등록시, 자동으로 JobRegistry에 Job을 등록시켜준다.
     *
     * @param jobRegistry
     * @return
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
}
