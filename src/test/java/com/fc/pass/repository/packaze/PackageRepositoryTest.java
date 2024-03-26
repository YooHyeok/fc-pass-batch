package com.fc.pass.repository.packaze;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;

    @Test
    @DisplayName("바디챌린지 PT 12주 84일 패키지 추가 테스트")
    void test_save() throws Exception {

        //given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 12주");
        packageEntity.setPeriod(84);

        //when
        packageRepository.save(packageEntity);

        //then
        assertNotNull(packageEntity.getPackageSeq());
    }

    @Test
    @DisplayName("학생전용 3개월, 6개월 패키지 추가 테스트 - 페이지네이션 처리로 1개의 데이터만 조회")
    void test_findByCreatedAtAfter () throws Exception {
        //given
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity0 = new PackageEntity();
        packageEntity0.setPackageName("학생 전용 3개월");
        packageEntity0.setPeriod(90);
        packageRepository.save(packageEntity0);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        //when - 리스트 조회이지만, 페이지네이션 처리로 1개의 데이터만 조회
        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        //then
        assertEquals(1, packageEntities.size());
        assertEquals(packageEntity1.getPackageSeq(), packageEntities.get(0).getPackageSeq());
    }

    @Test
    @DisplayName("바디프로필 이벤트 4개월 90일 패키지 생성 후 120일로 수정 테스트 - JPQL로 업데이트")
    void test_updateCountAndPeriod () throws Exception {
        //given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        //when
        int updatedCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        final PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        //then
        assertEquals(1, updatedCount);
        assertEquals(30, updatedPackageEntity.getCount());
        assertEquals(120, updatedPackageEntity.getPeriod());

    }

   @Test
   @DisplayName("이용권 삭제 테스트")
   void  test_delete () throws Exception {
       //given
       PackageEntity packageEntity = new PackageEntity();
       packageEntity.setPackageName("제거할 이용권");
       packageEntity.setCount(1);
       PackageEntity newPackageEntity = packageRepository.save(packageEntity);

       //when
        packageRepository.deleteById(newPackageEntity.getPackageSeq());

       //then
       assertTrue(packageRepository.findById(newPackageEntity.getPackageSeq()).isEmpty());

   }
}