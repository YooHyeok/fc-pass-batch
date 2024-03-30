package com.fc.pass.repository.pass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PassRepository extends JpaRepository<PassEntity, Integer> {

    /**
     * [수업 종료 후 이용권 차감] <br/>
     * 예약된 수업의 이용권의 잔여 횟수를 수정한다.
     * @param passSeq
     * @param remainingCount
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = """
        UPDATE PassEntity p
           SET p.remainingCount = :remainingCount,
               p.modifiedAt = CURRENT_TIMESTAMP 
         WHERE p.passSeq = :passSeq
         """)
    int updateRemainingCount(Integer passSeq, Integer remainingCount);
}