package com.example.b03.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponseDTO {
    //문의 조회(상세 보기)

    //문의 번호
    private Integer inquiryId;

    //문의 제목
    private String title;

    //문의 내용
    private String content;

    //작성한 회원 번호
    private Integer memberNO;

    //문의글 등록일
    private LocalDateTime createdAt;

    //문의글 수정일
    private LocalDateTime updatedAt;

}
//조회용이라 유효성 불필요