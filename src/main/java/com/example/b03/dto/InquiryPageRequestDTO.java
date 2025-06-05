// InquiryPageRequestDTO.java
package com.example.b03.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

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

    // ⭐⭐⭐ 이 toUriString() 메서드를 여기에 추가해야 해! ⭐⭐⭐
    public String toUriString() {
        // UriComponentsBuilder를 사용해서 URI 쿼리 파라미터를 편리하게 생성할 수 있어.
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("page", this.page) // 현재 페이지 번호 추가
                .queryParam("size", this.size); // 페이지 당 항목 수 추가

        // 검색 타입이 비어있지 않으면 쿼리 파라미터로 추가
        if (this.searchType != null && !this.searchType.trim().isEmpty()) {
            builder.queryParam("searchType", this.searchType);
        }
        // 검색 키워드가 비어있지 않으면 쿼리 파라미터로 추가
        if (this.searchKeyword != null && !this.searchKeyword.trim().isEmpty()) {
            builder.queryParam("searchKeyword", this.searchKeyword);
        }

        // 완성된 쿼리 스트링을 문자열로 반환
        return builder.toUriString();
    }


}