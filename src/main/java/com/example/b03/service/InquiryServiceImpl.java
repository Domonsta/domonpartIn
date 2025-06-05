package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.Member;
import com.example.b03.repository.MemberRepository;
import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryPageRequestDTO;
import com.example.b03.dto.InquiryPageResponseDTO;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.InquiryCommentRepository; // ⭐️ 추가: InquiryCommentRepository 임포트!
// import com.example.b03.repository.search.InquirySearch; // 이 임포트는 현재 사용하지 않으므로 주석 처리 또는 삭제
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // PageRequest는 직접 사용하지 않으므로 필요 없으면 삭제해도 됨
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final InquiryCommentRepository inquiryCommentRepository; // ⭐️ 추가: InquiryCommentRepository 주입!
    private final ModelMapper modelMapper;

    @Override
    public Integer register(InquiryListDTO inquiryListDTO) {
        Member member = memberRepository.findById(inquiryListDTO.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다: " + inquiryListDTO.getMemberNo()));

        Inquiry inquiry = modelMapper.map(inquiryListDTO, Inquiry.class);
        inquiry.setMember(member);

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        log.info("새로운 문의 등록: " + savedInquiry.getInquiryId());
        return savedInquiry.getInquiryId();
    }

    @Override
    public InquiryListDTO readOne(Integer inquiryId) {
        Optional<Inquiry> result = inquiryRepository.findById(inquiryId);
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryId));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 문의글입니다.");
        }

        return modelMapper.map(inquiry, InquiryListDTO.class);
    }

    @Override
    public void modify(InquiryListDTO inquiryListDTO) {
        Optional<Inquiry> result = inquiryRepository.findById(inquiryListDTO.getInquiryId());
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryListDTO.getInquiryId()));

        if (!inquiry.getMember().getMemberNo().equals(inquiryListDTO.getMemberNo())) {
            throw new IllegalArgumentException("문의 작성자만 수정할 수 있습니다.");
        }

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 문의글은 수정할 수 없습니다.");
        }

        inquiry.setTitle(inquiryListDTO.getTitle());
        inquiry.setContent(inquiryListDTO.getContent());

        log.info("문의 수정 완료: " + inquiry.getInquiryId());
    }

    @Override
    public void remove(Integer inquiryId) {
        Optional<Inquiry> result = inquiryRepository.findById(inquiryId);
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryId));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문의글입니다.");
        }

        inquiry.setIsDeleted(true);
        inquiryRepository.save(inquiry);

        log.info("문의 삭제 완료 (논리적 삭제): " + inquiryId);
    }

    @Override
    public InquiryPageResponseDTO<InquiryListDTO> list(InquiryPageRequestDTO inquiryPageRequestDTO) {
        Pageable pageable = inquiryPageRequestDTO.getPageable(); // Pageable 생성 시 정렬 기준이 없으면 기본 정렬 (ID 내림차순 등) 필요

        Page<Inquiry> result;

        // searchAll 메서드의 파라미터가 String[] searchTypes, String searchKeyword, Pageable pageable 이 맞다면 아래 코드
        if (inquiryPageRequestDTO.getSearchType() != null && inquiryPageRequestDTO.getSearchKeyword() != null
                && !inquiryPageRequestDTO.getSearchType().trim().isEmpty() && !inquiryPageRequestDTO.getSearchKeyword().trim().isEmpty()) {
            result = inquiryRepository.searchAll(
                    new String[]{inquiryPageRequestDTO.getSearchType()}, // ⭐ 배열 형태로 전달
                    inquiryPageRequestDTO.getSearchKeyword(),
                    pageable
            );
            log.info("검색 결과: " + result.getTotalElements() + "개");
        } else {
            // 검색 조건이 없을 경우, 삭제되지 않은 문의글을 최신순으로 가져옴 (inquiryId 내림차순으로 가정)
            result = inquiryRepository.findByIsDeletedFalseOrderByInquiryIdDesc(pageable);
            log.info("전체 목록 (검색 조건 없음): " + result.getTotalElements() + "개");
        }

        List<InquiryListDTO> dtoList = result.getContent().stream()
                .map(inquiry -> {
                    InquiryListDTO dto = modelMapper.map(inquiry, InquiryListDTO.class);
                    // ⭐️⭐️⭐️ 각 문의글에 대한 댓글 개수 추가! ⭐️⭐️⭐️
                    int commentCount = inquiryCommentRepository.countByInquiryAndIsDeletedFalse(inquiry);
                    dto.setCommentCount(commentCount); // InquiryListDTO에 setCommentCount() 메서드가 있어야 함
                    return dto;
                })
                .collect(Collectors.toList());

        return InquiryPageResponseDTO.<InquiryListDTO>withAll()
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .page(inquiryPageRequestDTO.getPage())
                .size(inquiryPageRequestDTO.getSize())
                .build();
    }
}