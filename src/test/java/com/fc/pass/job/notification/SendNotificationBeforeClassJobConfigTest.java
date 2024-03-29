package com.fc.pass.job.notification;

import com.fc.pass.adapter.message.KakaoTalkMessageAdapter;
import com.fc.pass.config.KakaoTalkMessageConfig;
import com.fc.pass.config.TestBatchConfig;
import com.fc.pass.repository.booking.BookingEntity;
import com.fc.pass.repository.booking.BookingRepository;
import com.fc.pass.repository.booking.BookingStatus;
import com.fc.pass.repository.pass.PassEntity;
import com.fc.pass.repository.pass.PassRepository;
import com.fc.pass.repository.pass.PassStatus;
import com.fc.pass.repository.user.UserEntity;
import com.fc.pass.repository.user.UserRepository;
import com.fc.pass.repository.user.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        SendNotificationBeforeClassJobConfig.class,
        TestBatchConfig.class,
        SendNotificationItemWriter.class,
        KakaoTalkMessageConfig.class,
        KakaoTalkMessageAdapter.class
})
public class SendNotificationBeforeClassJobConfigTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PassRepository passRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void test_addNotificationStep() throws Exception{
        //given
        addBookingEntity();
        //when
        JobExecution jobExcaution = jobLauncherTestUtils.launchStep("addNotificationStep");
        JobExecution jobExcaution2 = jobLauncherTestUtils.launchStep("sendNotificationStep");
        //then
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExcaution.getExitStatus());
    }

    private void addBookingEntity() {
        final LocalDateTime now = LocalDateTime.now();
        final String userId = "A100" + RandomStringUtils.randomNumeric(4);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setUserName("유재혁");
        userEntity.setStatus(UserStatus.ACTIVE);
        userEntity.setPhone("01017547485");
        userEntity.setMeta(Map.of("uuid", "abcd1234"));
        userRepository.save(userEntity);

        PassEntity passEntity = new PassEntity();
        passEntity.setPackageSeq(1);
        passEntity.setUserId(userId);
        passEntity.setStatus(PassStatus.PROGRESSED);
        passEntity.setRemainingCount(10);
        passEntity.setStartedAt(now.minusDays(60));
        passEntity.setEndedAt(now.minusDays(1));
        passRepository.save(passEntity);

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setPassSeq(passEntity.getPassSeq());
        bookingEntity.setUserId(userId);
        bookingEntity.setStatus(BookingStatus.READY);
        bookingEntity.setStartedAt(now.plusMinutes(10));
        bookingEntity.setEndedAt(bookingEntity.getStartedAt().plusMinutes(50));
        bookingRepository.save(bookingEntity);

    }

}
