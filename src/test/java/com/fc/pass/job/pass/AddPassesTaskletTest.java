package com.fc.pass.job.pass;

import com.fc.pass.config.TestBatchConfig;
import com.fc.pass.repository.pass.*;
import com.fc.pass.repository.user.UserGroupMappingEntity;
import com.fc.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j

@ExtendWith(MockitoExtension.class)
class AddPassesTaskletTest {
    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;
    @Mock private PassRepository passRepository;
    @Mock private BulkPassRepository bulkPassRepository;
    @Mock private UserGroupMappingRepository userGroupMappingRepository;

    @InjectMocks // 클래스의 인스턴스를 생성하고 @Mock으로 생성된 객체를 주입한다.(생성자주입)
    private AddPassesTasklet addPassesTasklet;

    @Test
    @DisplayName("")
    void test_execute() throws Exception {
        //given
        final String userGroupId = "GROUP";
        final String userId = "A1000000";
        final Integer packageSeq = 1;
        final Integer count = 10;

        final LocalDateTime now = LocalDateTime.now();

        final BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(packageSeq);
        bulkPassEntity.setUserGroupId(userGroupId);
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(count);
        bulkPassEntity.setStartedAt(now); // 오늘 시작
        bulkPassEntity.setEndedAt(now.plusDays(60)); //60일후 종료

        final UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId(userGroupId);
        userGroupMappingEntity.setUserId(userId);

        //when
        when(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any())).thenReturn(List.of(bulkPassEntity));
        when(userGroupMappingRepository.findByUserGroupId(eq("GROUP"))).thenReturn(List.of(userGroupMappingEntity));


        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext); //목으로 구현한 stepContribution, chunkContext

        //then
        
        /* execute의 return인 RepeatStatus값 확인 */
        assertEquals(RepeatStatus.FINISHED, repeatStatus);

        /* 추가된 PassEntity값 확인 */
        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture());
        final List<PassEntity> passEntities = passEntitiesCaptor.getValue();
        assertEquals(1, passEntities.size()); //1개 의 데이터

        PassEntity passEntity = passEntities.get(0);
        assertEquals(packageSeq, passEntity.getPackageSeq());
        assertEquals(userId, passEntity.getUserId());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(count, passEntity.getRemainingCount());

    }

}