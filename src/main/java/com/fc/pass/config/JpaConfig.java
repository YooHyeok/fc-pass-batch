package com.fc.pass.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 생성일시, 수정일시, 작성자, 수정자 <br/>
 * Audit: 감시하다 <br/>
 * Spring에서 해당 정보들을 감시하여 자동으로 값을 DB에 저장해준다. <br/>
 * Entity를 Insert 혹은 Update하는 경우 매번 시간 데이터를 입력하는 작업을 Audits를 사용하면 자동으로 시간을 매핑해서 DB에 저장해준다.
 */
@EnableJpaAuditing
@Configuration
public class JpaConfig {
}
