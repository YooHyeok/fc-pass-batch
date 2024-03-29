package com.fc.pass.repository.user;

import com.fc.pass.repository.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user")
@TypeDef(name="json", typeClass = JsonType.class) // JsonType: json을 Map으로 변환하기 위해 설정 => hibernate-types 디펜던시
public class UserEntity extends BaseEntity {

    @Id
    private String userId;

    private String userName;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String phone;


    /**
     * String 으로 meta 데이터를 받으나, String으로 되어있는 json을 Map으로 타입전환을 할 수 있도록
     * 엔티티의 클래스 레벨에 선언된 @TypeDef 어노테이션에 JsonType을 적용하였다.
     * 데이터베이스 상에 저장될 문자열로 이루어진 JSON 데이터가 프로그램 내부에서는 Map 타입으로 세팅할 수 있게끔 설정함 
     */
    @Type(type="json")
    private Map<String, Object> meta;

    /**
     * json타입의 데이터에 key가 uuid가 존재한다면 해당 key의 value를 반환한다.
     * 없으면 null을 반환한다.
     * @return
     */
    public String getUuid() {
        String uuid = null;
        if (meta.containsKey("uuid")) {
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;
    }



}