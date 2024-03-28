package com.fc.pass.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupMappingRepository extends JpaRepository<UserGroupMappingEntity, Integer> {

    /**
     * UserGroupID를 기준으로 UserGroupMappingEntity 객체 목록을 조회한다.
     * (userId를 알기위해 엔티티기반으로 조회함)
     * @param userGroupId
     * @return
     */
    List<UserGroupMappingEntity> findByUserGroupId(String userGroupId);
}