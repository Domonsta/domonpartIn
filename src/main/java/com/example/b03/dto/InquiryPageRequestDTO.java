// InquiryPageRequestDTO.java
package com.example.b03.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryPageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    private String searchType; // t, c, w (title, content, writer)
    private String searchKeyword;

    // ⭐️ 이 메서드를 추가해야 해!
    public Pageable getPageable() {
        // 정렬은 inquiryId를 기준으로 내림차순으로 기본 설정
        return PageRequest.of(this.page - 1, this.size, Sort.by("inquiryId").descending());
    }
}