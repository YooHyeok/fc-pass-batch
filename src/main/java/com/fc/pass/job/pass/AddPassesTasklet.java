package com.fc.pass.job.pass;

import com.fc.pass.repository.pass.*;
import com.fc.pass.repository.user.UserGroupMappingEntity;
import com.fc.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AddPassesTasklet implements Tasklet {
    private final PassRepository passRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;
    private final BulkPassRepository bulkPassRepository;

    public AddPassesTasklet(BulkPassRepository bulkPassRepository,
                            UserGroupMappingRepository userGroupMappingRepository,
                            PassRepository passRepository) {
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
        this.passRepository = passRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        /* 이용권 시작일시 1일전 user_group의 각 사용자에게 이용권을 추가한다. */
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1); // 시작일시: 오늘 - 하루전 (어제)
        final List<BulkPassEntity> bulkPassesEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;

        // 대량 이용권 정보들에 대한 Loop - user_group에 속한 userId를 조회하고 해당 userId로 이용권을 추가한다.
        for (BulkPassEntity bulkPassEntity : bulkPassesEntities) {
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).toList();
            count += addPasses(bulkPassEntity, userIds);

            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED); //Complete로 변경
        }
        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt={}", count, startedAt);
        return RepeatStatus.FINISHED;
    }

    /**
     * [BulkPass, 회원]-정보 기준 Pass데이터 생성 메소드
     * @param userIds
     * @return
     */
    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>();
        for (String userId : userIds) {
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity);
        }
        return passRepository.saveAll(passEntities).size();
    }
}