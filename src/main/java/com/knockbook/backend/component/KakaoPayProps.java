package com.knockbook.backend.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakaopay")
public class KakaoPayProps {
    private String cid;                 // kakaopay.cid
    private String secretKey;           // kakaopay.secret-key
    private String apiBase;             // kakaopay.api-base
    private String approvalReturnBase;  // kakaopay.approval-return-base
    private String cancelReturnBase;    // kakaopay.cancel-return-base
    private String failReturnBase;      // kakaopay.fail-return-base
}
