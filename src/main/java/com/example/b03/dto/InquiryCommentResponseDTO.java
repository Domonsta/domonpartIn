package com.example.b03.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryCommentResponseDTO {
    //관리자 답변 조회/목록

    private Integer commentId;
    //답변 ID (고유 식별자)

    private Integer inquiryId;
    //이 답변이 어떤 문의에 대한 것인지(문의 ID)

    private String content;
    //답변 내용

    private Integer adminNo;
    //답변을 작성한 관리자 번호

    private LocalDateTime createdAt;
    //답변 작성시간
    private LocalDateTime updatedAt;
    //답변 수정시간
    private Boolean isDeleted;
    //답변 삭제여부
}
//조회용이라 유효성 불필요