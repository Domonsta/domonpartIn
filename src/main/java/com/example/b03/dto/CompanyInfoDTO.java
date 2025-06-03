package com.example.b03.dto;

import com.example.b03.domain.CompanyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyInfoDTO {
    private Integer memberNo;
    private String companyName;
    private LocalDate foundedDate;
    private Integer employeeCount;
    private Long revenue;
    private String techStack;
    private String homepageUrl;
    private String description;

    //  Member에서 가져온 값
    private String phone;
    private String address;

    public static CompanyInfoDTO fromEntity(CompanyInfo companyInfo) {
        return CompanyInfoDTO.builder()
                .memberNo(companyInfo.getMember().getMemberNo())
                .companyName(companyInfo.getCompanyName())
                .foundedDate(companyInfo.getFoundedDate())
                .employeeCount(companyInfo.getEmployeeCount())
                .revenue(companyInfo.getRevenue())
                .techStack(companyInfo.getTechStack())
                .homepageUrl(companyInfo.getHomepageUrl())
                .description(companyInfo.getDescription())
                .address(companyInfo.getMember().getAddress())  // 주소 매핑
                .phone(companyInfo.getMember().getPhone())      // 전화번호 매핑
                .build();
    }
}
