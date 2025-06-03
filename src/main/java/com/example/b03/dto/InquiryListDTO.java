package com.example.b03.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListDTO {
    //문의글 목록 조회

    //문의글 번호
    private Integer inquiryId;

    //문의글 제목
    private String title;

    //문의글 작성회원
    private Integer memberNo;

    //문의글 등록일
    private LocalDateTime createdAt;

}
//조회용이라 유효성 필요 없음