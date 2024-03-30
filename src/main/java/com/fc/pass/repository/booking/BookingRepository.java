package com.fc.pass.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BookingRepository extends JpaRepository<BookingEntity, Integer> {
    /**
     * [수업 종료 후 이용권 차감] <br/>
     * 이용권 사용 여부를 사용으로 변경한다.
     * @param passSeq
     * @param usedPass
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = """
            UPDATE BookingEntity b
               SET b.usedPass = :usedPass,
                   b.modifiedAt = CURRENT_TIMESTAMP
             WHERE b. passSeq = :passSeq 
             """)
    int updateUsedPass(Integer passSeq, boolean usedPass);
}