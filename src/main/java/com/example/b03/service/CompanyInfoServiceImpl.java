package com.example.b03.service;

import com.example.b03.domain.CompanyInfo;
import com.example.b03.domain.Member;
import com.example.b03.dto.CompanyInfoDTO;
import com.example.b03.dto.PageRequestDTO;
import com.example.b03.dto.PageResponseDTO;
import com.example.b03.repository.CompanyInfoRepository;
import com.example.b03.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CompanyInfoServiceImpl implements CompanyInfoService {

    private final CompanyInfoRepository companyInfoRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public CompanyInfoDTO register(CompanyInfoDTO dto) {
        Member member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        CompanyInfo companyInfo = CompanyInfo.builder()
                .member(member)
                .companyName(dto.getCompanyName())
                .foundedDate(dto.getFoundedDate())
                .employeeCount(dto.getEmployeeCount())
                .revenue(dto.getRevenue())
                .techStack(dto.getTechStack())
                .homepageUrl(dto.getHomepageUrl())
                .description(dto.getDescription())
                .build();

        CompanyInfo saved = companyInfoRepository.save(companyInfo);
        return CompanyInfoDTO.fromEntity(saved);
    }

    @Override
    public CompanyInfoDTO getByMemberNo(Integer memberNo) {
        CompanyInfo companyInfo = companyInfoRepository.findByMember_MemberNo(memberNo)
                .orElseThrow(() -> new NoSuchElementException("CompanyInfo not found"));

        return CompanyInfoDTO.fromEntity(companyInfo);
    }

    @Override
    @Transactional
    public CompanyInfoDTO update(CompanyInfoDTO dto) {
        CompanyInfo existing = companyInfoRepository.findByMember_MemberNo(dto.getMemberNo())
                .orElseThrow(() -> new NoSuchElementException("CompanyInfo not found"));

        existing.setCompanyName(dto.getCompanyName());
        existing.setFoundedDate(dto.getFoundedDate());
        existing.setEmployeeCount(dto.getEmployeeCount());
        existing.setRevenue(dto.getRevenue());
        existing.setTechStack(dto.getTechStack());
        existing.setHomepageUrl(dto.getHomepageUrl());
        existing.setDescription(dto.getDescription());

        //  Member 안의 address, phone 수정
        existing.getMember().setPhone(dto.getPhone());
        existing.getMember().setAddress(dto.getAddress());

        return CompanyInfoDTO.fromEntity(existing);
    }

    @Override
    @Transactional
    public void delete(Integer memberNo) {
        CompanyInfo companyInfo = companyInfoRepository.findByMember_MemberNo(memberNo)
                .orElseThrow(() -> new NoSuchElementException("CompanyInfo not found"));
        companyInfoRepository.delete(companyInfo);
    }

    @Override
    public PageResponseDTO<CompanyInfoDTO> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable("memberNo");
        Page<CompanyInfo> result = companyInfoRepository.findAll(pageable);

        List<CompanyInfoDTO> dtoList = result.getContent()
                .stream()
                .map(CompanyInfoDTO::fromEntity) //  ModelMapper 대신 정적 메서드 사용
                .toList();

        return PageResponseDTO.<CompanyInfoDTO>withAll()
                .pageRequestDTO(requestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }
}



