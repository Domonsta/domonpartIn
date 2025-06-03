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
public class MembershipTypeDTO {

    @NotNull(message = "타입 ID는 필수 항목입니다.") // 새로운 타입 등록 시 필요, 혹은 기존 타입 조회 시
    private Integer typeId; // int 대신 Integer (null 체크 가능)

    @NotBlank(message = "타입 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "타입 이름은 최대 50자까지 입력 가능합니다.")
    private String typeName;
}