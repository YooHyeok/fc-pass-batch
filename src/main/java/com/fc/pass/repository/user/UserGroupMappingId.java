package com.fc.pass.repository.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * UserGroupMappingEntity의 Composite key 구성을 위한 클래스
 */
@Getter
@Setter
@ToString
public class UserGroupMappingId implements Serializable {
    private String userGroupId;
    private String userId;
}