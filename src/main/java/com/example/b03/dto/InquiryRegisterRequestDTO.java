package com.example.b03.dto;

import jakarta.validation.constraints.NotBlank; // 추가
import jakarta.validation.constraints.NotNull;  // 추가
import jakarta.validation.constraints.Size;    // 추가
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryRegisterRequestDTO {
    //문의글 등록 DTO

    //문의내용 쓸 때 필요한 문의번호 (등록 시에는 클라이언트에서 보내지 않으므로 NotNull 제거)
    // private Integer inquiryId; // 등록 시에는 inquiryId는 서버에서 생성되므로 유효성 검사 불필요

    @NotBlank(message = "문의 제목은 필수 입력 항목입니다.") // 제목이 비어있으면 안 됨
    @Size(max = 100, message = "문의 제목은 최대 100자까지 입력 가능합니다.") // 제목 길이 제한
    private String title;

    @NotBlank(message = "문의 내용은 필수 입력 항목입니다.") // 내용이 비어있으면 안 됨
    @Size(max = 2000, message = "문의 내용은 최대 2000자까지 입력 가능합니다.") // 내용 길이 제한 (예시)
    private String content;

    @NotNull(message = "회원 번호는 필수 입력 항목입니다.") // 작성한 회원 번호는 필수
    private Integer memberNo; // 누가 작성했는지 알게 (member board에서 가져옴)
}