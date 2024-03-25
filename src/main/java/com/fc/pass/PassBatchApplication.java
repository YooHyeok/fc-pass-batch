package com.fc.pass;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PassBatchApplication {

	/**
	 * Job을 만들기 위해서는 Step을 만든 후 Step을 기반으로 Job을 구성한다.
	 */
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	public PassBatchApplication(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	/**
	 * Step 선언
	 * @return
	 */
	@Bean
	public Step passStep() {
		return this.stepBuilderFactory.get("passStep") // Step의 이름을 선언한다.
				.tasklet((contribution, chunkContext) -> { //Tasklet 방식으로 Tasklet 인터페이스의 execute() 메소드를 람다로 구현
					System.out.println("Execute PassStep");
					System.out.println("contribution = " + contribution + ", chunkContext = " + chunkContext);
					return RepeatStatus.FINISHED; // 종료
				}).build();
	}

	/**
	 * Job 선언
	 * passStep을 구성해준다.
	 * @return
	 */
	@Bean
	public Job passJob() {
		return this.jobBuilderFactory.get("passJob")
				.start(passStep())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(PassBatchApplication.class, args);
	}

}
