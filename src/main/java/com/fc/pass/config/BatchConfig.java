package com.fc.pass.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing // Batch를 위한 Processing 허용 설정 - JobBuilderFactory, StepBuilderFactory가 Bean으로 등록된다.
@Configuration
public class BatchConfig {
}
