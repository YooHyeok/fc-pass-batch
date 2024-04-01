package com.fc.pass.repository.statistics;

import com.fc.pass.repository.booking.BookingEntity;
import com.fc.pass.repository.booking.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name="statistics")
public class StatisticsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statisticsSeq;
    private LocalDateTime statisticsAt;
    private int allCount;
    private int attendedCount;
    private int cancelledCount;

    /**
     * StatisticsEnitity를 생성한다.
     * 넘겨받은 예약 수업 데이터의 종료일자를 기준으로 통계데이터 생성 일자를 지정한다.
     * 예약수업에 대한 출석이 true일 경우 출석횟수를 1로 초기화하고, 예약상태가 취소일 경우 취소횟수를 1로 초기화한다.
     * @param bookingEntity
     * @return
     */
    public static StatisticsEntity create(final BookingEntity bookingEntity) {
        StatisticsEntity statisticsEntity = new StatisticsEntity();
        statisticsEntity.setStatisticsAt(bookingEntity.getStatisticsAt());
        statisticsEntity.setAllCount(1);
        if (bookingEntity.isAttended()) {
            statisticsEntity.setAttendedCount(1);
        }
        if (BookingStatus.CANCELLED.equals(bookingEntity.getStatus())) {
            statisticsEntity.setCancelledCount(1);
        }
        return statisticsEntity;
    }

    /**
     * 기본적으로 전체 횟수를 증가시킨다. <br/>
     * 예약된 수업에 출석했을경우 출석횟수를 증가시킨다.
     * 예약 상태가 취소일 경우 취소횟수를 증가시킨다.
     * @param bookingEntity
     */
    public void add(final BookingEntity bookingEntity) {
        this.allCount++;
        if (bookingEntity.isAttended()) {
            this.attendedCount++;
        }
        if (BookingStatus.CANCELLED.equals(bookingEntity.getStatus())) {
            this.cancelledCount++;
        }
    }
}
