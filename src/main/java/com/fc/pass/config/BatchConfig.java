package com.fc.pass.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing // Batch를 위한 Processing 허용 설정
@Configuration
public class BatchConfig {
}
