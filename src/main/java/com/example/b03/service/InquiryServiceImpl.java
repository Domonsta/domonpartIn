package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryPageRequestDTO;
import com.example.b03.dto.InquiryPageResponseDTO;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.search.InquirySearch; // ⭐ InquirySearch 인터페이스 임포트! ⭐
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2 // 로그 사용을 위한 Lombok 애노테이션
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ModelMapper modelMapper;

    // InquiryRepository가 InquirySearch 인터페이스를 상속하므로,
    // inquiryRepository를 통해 searchAll 메서드를 직접 호출할 수 있습니다.
    // 따라서 InquirySearch 타입을 별도로 주입받을 필요는 없습니다.


    @Override
    public Integer register(InquiryListDTO inquiryListDTO) {
        // InquiryListDTO를 Inquiry 엔티티로 변환
        Inquiry inquiry = modelMapper.map(inquiryListDTO, Inquiry.class);
        // 저장
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        log.info("새로운 문의 등록: " + savedInquiry.getInquiryId());
        return savedInquiry.getInquiryId();
    }

    @Override
    public InquiryListDTO readOne(Integer inquiryId) {
        // ID로 문의 엔티티 조회
        Optional<Inquiry> result = inquiryRepository.findById(inquiryId);
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryId));

        // 삭제된 문의글인지 확인
        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 문의글입니다.");
        }

        // 엔티티를 DTO로 변환하여 반환
        return modelMapper.map(inquiry, InquiryListDTO.class);
    }

    @Override
    public void modify(InquiryListDTO inquiryListDTO) {
        // 기존 문의글 조회
        Optional<Inquiry> result = inquiryRepository.findById(inquiryListDTO.getInquiryId());
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryListDTO.getInquiryId()));

        // 삭제된 문의글인지 확인
        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 문의글은 수정할 수 없습니다.");
        }

        // ⭐⭐⭐ 여기서 수정! @Data 애노테이션 덕분에 생성된 세터 메서드 사용! ⭐⭐⭐
        inquiry.setTitle(inquiryListDTO.getTitle()); // inquiry.changeTitle() 대신
        inquiry.setContent(inquiryListDTO.getContent()); // inquiry.changeContent() 대신

        // 저장 (더티 체킹에 의해 자동 반영되지만, 명시적 저장을 선호하기도 합니다.)
        // inquiryRepository.save(inquiry);
        log.info("문의 수정 완료: " + inquiry.getInquiryId());
    }

    @Override
    public void remove(Integer inquiryId) {
        // 기존 문의글 조회
        Optional<Inquiry> result = inquiryRepository.findById(inquiryId);
        Inquiry inquiry = result.orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다: " + inquiryId));

        // 이미 삭제된 문의글인지 확인
        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 문의글입니다.");
        }

        // 논리적 삭제 처리 (isDeleted 필드를 true로 변경)
        inquiry.delete(); // BaseEntity에 delete() 메서드가 있다면 사용
        // inquiry.setIsDeleted(true); // 또는 이렇게 직접 설정
        // inquiryRepository.save(inquiry); // 더티 체킹에 의해 자동 반영될 수 있습니다.
        log.info("문의 삭제 완료 (논리적 삭제): " + inquiryId);
    }

    @Override
    public InquiryPageResponseDTO<InquiryListDTO> list(InquiryPageRequestDTO inquiryPageRequestDTO) {
        // InquiryPageRequestDTO에서 page, size, types, keyword를 가져옵니다.
        Pageable pageable = PageRequest.of(
                inquiryPageRequestDTO.getPage() - 1, // 페이지 번호는 0부터 시작 (JPA Pageable은 0-based)
                inquiryPageRequestDTO.getSize()
        );

        Page<Inquiry> result;

        // ⭐⭐⭐ searchAll 메서드 호출 부분은 이전과 동일하게 유지 ⭐⭐⭐
        if (inquiryPageRequestDTO.getTypes() != null && inquiryPageRequestDTO.getKeyword() != null
                && inquiryPageRequestDTO.getTypes().length > 0 && !inquiryPageRequestDTO.getKeyword().trim().isEmpty()) {
            // 검색 조건(types, keyword)이 있을 때, InquirySearch 인터페이스의 searchAll 메서드 사용
            // inquiryRepository가 InquirySearch를 상속했으므로, inquiryRepository로 호출 가능
            result = inquiryRepository.searchAll(
                    inquiryPageRequestDTO.getTypes(),
                    inquiryPageRequestDTO.getKeyword(),
                    pageable
            );
            log.info("검색 결과: " + result.getTotalElements() + "개");
        } else {
            // 검색 조건이 없을 때, 삭제되지 않은 모든 문의글을 최신순으로 조회
            result = inquiryRepository.findByIsDeletedFalseOrderByInquiryIdDesc(pageable);
            log.info("전체 목록 (검색 조건 없음): " + result.getTotalElements() + "개");
        }

        // Page<Inquiry>를 Page<InquiryListDTO>로 변환
        List<InquiryListDTO> dtoList = result.getContent().stream()
                .map(inquiry -> modelMapper.map(inquiry, InquiryListDTO.class))
                .collect(Collectors.toList());

        // InquiryPageResponseDTO를 빌드하여 반환
        return InquiryPageResponseDTO.<InquiryListDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(inquiryPageRequestDTO)
                .totalCount((int) result.getTotalElements())
                .build();
    }
}