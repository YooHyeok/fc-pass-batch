package com.fc.pass.repository.pass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BulkPassRepository extends JpaRepository<BulkPassEntity, Integer> {

    /**
     * 이용권 일괄 지급을 위한 벌크 연산 대상 목록 조회
     * WHERE status = :status AND startedAt > :startedAt
     * 위 조건을 기반으로 발급할 회원 그룹 정보가 담긴 패스권 목록을 가져온다. (상태와 시작날짜 기준)
     * @param status
     * @param startedAt
     * @return
     */
    List<BulkPassEntity> findByStatusAndStartedAtGreaterThan(BulkPassStatus status, LocalDateTime startedAt);

}