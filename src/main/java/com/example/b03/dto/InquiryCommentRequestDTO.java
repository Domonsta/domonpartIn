package com.example.b03.dto;

import jakarta.validation.constraints.NotBlank; // 추가
import jakarta.validation.constraints.NotNull;  // 추가
import jakarta.validation.constraints.Size;    // 추가
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryCommentRequestDTO {
    //문의글 댓글 등록/수정

    @NotNull(message = "문의 ID는 필수 입력 항목입니다.") // 어떤 문의에 대한 답변인지 (댓글이 달릴 문의의 ID)
    private Integer inquiryId;

    @NotBlank(message = "답변 내용은 필수 입력 항목입니다.") // 답변 내용(댓글)
    @Size(max = 1000, message = "답변 내용은 최대 1000자까지 입력 가능합니다.") // 답변 길이 제한 (예시)
    private String content; // **변경 제안에 따라 'content'로 통일**

    @NotNull(message = "관리자 번호는 필수 입력 항목입니다.") // 댓글 단 관리자 번호(로그인된 관리자 정보를 통해 자동으로 가져옴)
    private Integer adminNo;

    // 댓글 수정 시에만 필요하므로 @NotNull 제거. 등록 시에는 null.
    private Integer commentId; // 댓글 수정할때 필요
}