package com.fc.pass.adapter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mapstruct.ap.shaded.freemarker.core.TemplateObject;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class KakaoTalkMessageRequest {
    @JsonProperty("template_object")
    private TemplateObject templateObject;

    @JsonProperty("receiver_uuids")
    private List<String> receiverUuids; // 받는 사람

    /**
     * {
     *  "object_type": value,
     *  "text": value,
     *  "link": {
     *     "web_url":value,
     *  },
     * } 형태로 구성된 데이터
     */
    @Getter
    @Setter
    @ToString
    public static class TemplateObject {
        @JsonProperty("object_type")
        private String objectType;
        private String text;
        private Link link;

        @Getter
        @Setter
        @ToString
        public static class Link {
            @JsonProperty("web_url")
            private String webUrl;
        }

    }

    /**
     * 생성자
     * @param uuid
     * @param text
     */
    public KakaoTalkMessageRequest(String uuid, String text) {
        List<String> receiverUuids = Collections.singletonList(uuid);
        // 내부클래스로 구성한 파라미터 데이터들을 초기화한다.
        TemplateObject.Link link = new TemplateObject.Link();
        TemplateObject templateObject = new TemplateObject();
        templateObject.setObjectType("text");
        templateObject.setText(text);
        templateObject.setLink(link);

        this.receiverUuids = receiverUuids;
        this.templateObject = templateObject;
    }

}
