package com.fc.pass.adapter.message;

import com.fc.pass.config.KakaoTalkMessageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class KakaoTalkMessageAdapter {
    private final WebClient webClient;

    public KakaoTalkMessageAdapter(KakaoTalkMessageConfig config) {
        log.info("config = {}", config.toString());
        webClient = WebClient.builder()
                .baseUrl(config.getHost())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setBearerAuth(config.getToken());
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .build();
    }

    public MultiValueMap<String, String> convertToFormData(KakaoTalkMessageRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        // TemplateObject의 필드를 추가
        formData.add("template_object.object_type", request.getTemplateObject().getObjectType());
        formData.add("template_object.text", request.getTemplateObject().getText());
        formData.add("template_object.link.web_url", request.getTemplateObject().getLink().getWebUrl());
        // receiver_uuids 필드를 추가
        for (String receiverUuid : request.getReceiverUuids()) {
            formData.add("receiver_uuids", receiverUuid);
        }
        return formData;
    }

    public boolean sendKakaoTalkMessage(final String uuid, final String text) {
        KakaoTalkMessageResponse response = webClient.post().uri("/v1/api/talk/friends/message/default/send")
//                .body(BodyInserters.fromValue(new KakaoTalkMessageRequest(uuid, text))) // uuid와 text를 초기화한 객체타입으로 request 객체를 지정 (파라미터가 바인딩될예정)
                .body(BodyInserters.fromFormData(convertToFormData(new KakaoTalkMessageRequest(uuid, text)))) // uuid와 text를 초기화한 객체타입으로 request 객체를 지정 (파라미터가 바인딩될예정)
                .retrieve()
                .bodyToMono(KakaoTalkMessageResponse.class) // Response 객체로 매핑
                .block();

        if (response == null || response.getSuccessfulReceiverUuids() == null) { // 통신 실패시 false 반환
            log.info("카카오 실패");
            return false;
        }
        return response.getSuccessfulReceiverUuids().size() > 0; // 통신 성공시 true 반환
    }
}
