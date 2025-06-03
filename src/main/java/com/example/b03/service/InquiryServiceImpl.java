package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.Member;
import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryRegisterRequestDTO;
import com.example.b03.dto.InquiryResponseDTO;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    //1:1문의글 전체보기 (목록 조회)
    @Override
    @Transactional(readOnly = true)
    public List<InquiryListDTO> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc();
        return inquiries.stream()
                .map(this::entityToInquiryListDTO)
                .collect(Collectors.toList());
    }

    //1:1문의글 상세보기
    @Override
    @Transactional(readOnly = true)
    public InquiryResponseDTO getInquiryDetail(Integer inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. inquiryId: " + inquiryId));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문의입니다.");
        }
        return entityToInquiryResponseDTO(inquiry);
    }

    //1:1문의글 작성
    @Override
    public InquiryResponseDTO registerInquiry(InquiryRegisterRequestDTO requestDTO) {
        Member member = memberRepository.findById(requestDTO.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. memberNo: " + requestDTO.getMemberNo()));

        Inquiry inquiry = Inquiry.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .member(member)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return entityToInquiryResponseDTO(savedInquiry);
    }

    //1:1문의글 수정
    @Override
    public InquiryResponseDTO updateInquiry(Integer inquiryId, InquiryRegisterRequestDTO requestDTO) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. inquiryId: " + inquiryId));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문의는 수정할 수 없습니다.");
        }

        // --- 작성자만 수정 가능하도록 로직 추가 ---
        // 문의를 작성한 회원의 memberNo와 요청 DTO에 포함된 memberNo가 일치하는지 확인
        if (!inquiry.getMember().getMemberNo().equals(requestDTO.getMemberNo())) {
            throw new IllegalArgumentException("문의 작성자만 수정할 수 있습니다.");
        }
        // ------------------------------------

        inquiry.setTitle(requestDTO.getTitle());
        inquiry.setContent(requestDTO.getContent());
        // createdAt, updatedAt은 BaseEntity에서 자동으로 관리됨

        Inquiry updatedInquiry = inquiryRepository.save(inquiry);
        return entityToInquiryResponseDTO(updatedInquiry);
    }

    //1:1문의글 삭제(논리적 삭제)
    @Override
    public void deleteInquiry(Integer inquiryId, Integer requestingUserMemberNo) { // 삭제 요청한 사용자 memberNo 추가
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. inquiryId: " + inquiryId));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문의입니다.");
        }

        // --- 삭제 요청한 사용자가 작성자인지 확인하는 로직 추가 ---
        // 문의를 작성한 회원의 memberNo와 삭제 요청한 사용자의 memberNo가 일치하는지 확인
        if (!inquiry.getMember().getMemberNo().equals(requestingUserMemberNo)) {
            throw new IllegalArgumentException("문의 작성자만 삭제할 수 있습니다.");
        }
        // --------------------------------------------------

        inquiry.setIsDeleted(true); // 논리적 삭제
        inquiryRepository.save(inquiry);
    }

    // --- DTO 변환 헬퍼 메서드 ---
    private InquiryResponseDTO entityToInquiryResponseDTO(Inquiry inquiry) {
        return InquiryResponseDTO.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .memberNO(inquiry.getMember().getMemberNo())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    private InquiryListDTO entityToInquiryListDTO(Inquiry inquiry) {
        return InquiryListDTO.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .memberNo(inquiry.getMember().getMemberNo())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}