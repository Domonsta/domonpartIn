package com.example.b03.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private Integer memberNo;
    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;
    private String address;
    private String phone;

    // MembershipType 정보 포함
    private Byte membershipTypeId;
    private String membershipTypeName;

    // BaseEntity 공통 필드
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    // static 변환 메서드
    public static MemberDTO fromEntity(com.example.b03.domain.Member member) {
        return MemberDTO.builder()
                .memberNo(member.getMemberNo())
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .name(member.getName())
                .birthDate(member.getBirthDate())
                .address(member.getAddress())
                .phone(member.getPhone())
                .membershipTypeId(member.getMembershipType().getTypeId())
                .membershipTypeName(member.getMembershipType().getTypeName())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .isDeleted(member.getIsDeleted())
                .build();
    }
}