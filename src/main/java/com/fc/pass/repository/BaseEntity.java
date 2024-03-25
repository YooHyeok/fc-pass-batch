package com.fc.pass.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @EntityListeners
 * JPA 엔티티의 이벤트가 발생할 때 콜백 처리와 코드를 실행하는 방법이라고 Hibernate공식문서에 정의 <br/>
 * 애노테이션 인자로 CustomCallback을 요청할 클래스를 지정한다.<br/>
 * Auditing을 수행하므로 AuditingEntityListener.class를 인자로 넘긴다.<br/>
 * AuditingEntityListener 클래스의 touchForCreate, touchForUpdate 함수를 통해 생성/수정에 대한 시간을 만들어 준다.
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
}