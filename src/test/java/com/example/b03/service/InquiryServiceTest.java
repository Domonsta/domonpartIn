package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.Member;
import com.example.b03.domain.MembershipType;
import com.example.b03.dto.*;
import com.example.b03.repository.InquiryCommentRepository;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import com.example.b03.repository.MembershipTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class InquiryServiceTest {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    @Autowired
    private InquiryCommentRepository inquiryCommentRepository;

    private Member testMember;
    private Inquiry savedInquiry; // BeforeEach에서 저장되는 첫 번째 문의글

    private static final Byte MEMBER_TYPE_ADMIN = 1;
    private static final Byte MEMBER_TYPE_BUSINESS = 2;
    private static final Byte MEMBER_TYPE_GENERAL = 3;

    @BeforeEach
    void setUp() {
        inquiryCommentRepository.deleteAllInBatch();
        inquiryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        MembershipType generalMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_GENERAL)
                .orElseThrow(() -> new RuntimeException("개인회원 멤버십 타입을 찾을 수 없습니다. 테스트를 위해 DB에 3번 '개인회원' 멤버십 타입이 필요합니다."));

        testMember = memberRepository.save(Member.builder()
                .loginId("testuser123")
                .password("testpassword123!")
                .name("테스터회원")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("서울시 강남구 테헤란로 123")
                .phone("010-1234-5678")
                .membershipType(generalMembershipType)
                .build());

        savedInquiry = inquiryRepository.save(Inquiry.builder()
                .title("초기 테스트 문의 제목입니다.")
                .content("초기 테스트 문의 내용입니다. 상세한 내용을 포함합니다.")
                .member(testMember)
                .build());
    }

    @Test
    @DisplayName("문의글 전체 목록 조회 성공 - InquiryListDTO 매칭 확인 및 페이징 적용")
    void testGetAllInquiries_success() {
        // given
        Inquiry inquiry2 = Inquiry.builder()
                .title("두 번째 문의 제목")
                .content("두 번째 문의 내용입니다.")
                .member(testMember)
                .build();
        inquiryRepository.save(inquiry2);

        // when (페이징 요청 DTO 생성 및 전달)
        InquiryPageRequestDTO pageRequestDTO = InquiryPageRequestDTO.builder()
                .page(1) // 1페이지
                .size(10) // 10개씩
                .build();

        InquiryPageResponseDTO<InquiryListDTO> response = inquiryService.getAllInquiries(pageRequestDTO);

        // then (페이징 응답 DTO 검증)
        assertThat(response).isNotNull();
        assertThat(response.getDtoList()).isNotNull();
        assertThat(response.getDtoList().size()).isEqualTo(2); // 총 2개의 문의글이 반환될 것으로 예상

        // 페이징 정보 검증
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);

        // 최신 글이 먼저 오도록 정렬되어 있다고 가정하고 첫 번째와 두 번째 항목 검증
        // inquiry2가 더 최근에 저장되었으므로, 리스트의 첫 번째 항목이어야 함 (createdAt Desc 정렬 가정)
        assertThat(response.getDtoList().get(0).getInquiryId()).isEqualTo(inquiry2.getInquiryId());
        assertThat(response.getDtoList().get(0).getTitle()).isEqualTo(inquiry2.getTitle());
        assertThat(response.getDtoList().get(0).getMemberNo()).isEqualTo(inquiry2.getMember().getMemberNo());
        assertThat(response.getDtoList().get(0).getCreatedAt()).isNotNull();

        assertThat(response.getDtoList().get(1).getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(response.getDtoList().get(1).getTitle()).isEqualTo(savedInquiry.getTitle());
        assertThat(response.getDtoList().get(1).getMemberNo()).isEqualTo(savedInquiry.getMember().getMemberNo());
        assertThat(response.getDtoList().get(1).getCreatedAt()).isNotNull();
    }


    @Test
    @DisplayName("삭제된 문의글은 목록 조회에서 제외 및 페이징 적용")
    void testGetAllInquiries_excludeDeleted() {
        // given
        // 삭제할 문의글을 추가로 하나 더 생성
        Inquiry inquiryToDelete = Inquiry.builder()
                .title("삭제될 문의글입니다.")
                .content("이 문의글은 삭제되어야 합니다.")
                .member(testMember)
                .build();
        inquiryRepository.save(inquiryToDelete); // DB에 먼저 저장

        // 서비스의 deleteInquiry 메서드를 사용하여 논리적 삭제 수행
        inquiryService.deleteInquiry(inquiryToDelete.getInquiryId(), testMember.getMemberNo());

        // when (페이징 요청 DTO 생성 및 전달)
        InquiryPageRequestDTO pageRequestDTO = InquiryPageRequestDTO.builder()
                .page(1)
                .size(10)
                .build();

        InquiryPageResponseDTO<InquiryListDTO> response = inquiryService.getAllInquiries(pageRequestDTO);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDtoList()).isNotNull();
        assertThat(response.getDtoList().size()).isEqualTo(1); // savedInquiry (삭제되지 않은 것) 1개만 조회돼야 함

        // 페이징 정보 검증
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);

        assertThat(response.getDtoList().get(0).getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(response.getDtoList().get(0).getTitle()).isEqualTo(savedInquiry.getTitle());
    }

    // --- 1:1 문의글 상세보기 테스트 🔎 ---

    @Test
    @DisplayName("문의글 상세 보기 성공 - InquiryResponseDTO 매칭 확인")
    void testGetInquiryDetail_success() {
        InquiryResponseDTO result = inquiryService.getInquiryDetail(savedInquiry.getInquiryId());

        assertThat(result).isNotNull();
        assertThat(result.getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(result.getTitle()).isEqualTo(savedInquiry.getTitle());
        assertThat(result.getContent()).isEqualTo(savedInquiry.getContent());
        assertThat(result.getMemberNO()).isEqualTo(testMember.getMemberNo());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 문의글 상세 보기 실패")
    void testGetInquiryDetail_notFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.getInquiryDetail(999);
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 문의글 상세 보기 실패")
    void testGetInquiryDetail_isDeleted() {
        savedInquiry.setIsDeleted(true);
        inquiryRepository.save(savedInquiry);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.getInquiryDetail(savedInquiry.getInquiryId());
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의입니다.");
    }

    // --- 1:1 문의글 작성 테스트 ✍️ ---

    @Test
    @DisplayName("문의글 작성 성공 - InquiryRegisterRequestDTO 매칭 확인")
    void testRegisterInquiry_success() {
        InquiryRegisterRequestDTO requestDTO = InquiryRegisterRequestDTO.builder()
                .title("새로 작성할 문의 제목입니다.")
                .content("새로운 문의 내용입니다. 길게 작성해봅니다.")
                .memberNo(testMember.getMemberNo())
                .build();

        InquiryResponseDTO result = inquiryService.registerInquiry(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(result.getMemberNO()).isEqualTo(requestDTO.getMemberNo());
        assertThat(result.getInquiryId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // ⭐️ 여기 수정! .longValue() 제거!
        Inquiry foundInquiry = inquiryRepository.findById(result.getInquiryId()).orElse(null);
        assertThat(foundInquiry).isNotNull();
        assertThat(foundInquiry.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(foundInquiry.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(foundInquiry.getMember().getMemberNo()).isEqualTo(requestDTO.getMemberNo());
        assertThat(foundInquiry.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 문의글 작성 실패")
    void testRegisterInquiry_memberNotFound() {
        InquiryRegisterRequestDTO requestDTO = InquiryRegisterRequestDTO.builder()
                .title("새로 작성할 문의")
                .content("새로운 문의 내용입니다.")
                .memberNo(999)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.registerInquiry(requestDTO);
        });
        assertThat(exception.getMessage()).contains("해당 회원을 찾을 수 없습니다.");
    }

    // --- 1:1 문의글 수정 테스트 ✏️ ---

    @Test
    @DisplayName("문의글 수정 성공")
    void testUpdateInquiry_success() {
        // given
        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정된 문의 제목입니다.")
                .content("수정된 문의 내용입니다. 이전 내용을 변경합니다.")
                .memberNo(testMember.getMemberNo()) // 작성자 본인이 수정
                .build();

        // when
        InquiryResponseDTO result = inquiryService.updateInquiry(savedInquiry.getInquiryId(), updateRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(result.getTitle()).isEqualTo(updateRequestDTO.getTitle()); // DTO와 요청 DTO 비교
        assertThat(result.getContent()).isEqualTo(updateRequestDTO.getContent()); // DTO와 요청 DTO 비교
        assertThat(result.getMemberNO()).isEqualTo(testMember.getMemberNo());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt()); // 생성 시간보다 나중이거나 같아야 함

        // DB에서 실제로 수정되었는지 확인
        // ⭐️ 여기 수정! .longValue() 제거!
        Inquiry updatedInquiry = inquiryRepository.findById(savedInquiry.getInquiryId()).orElse(null);
        assertThat(updatedInquiry).isNotNull();
        assertThat(updatedInquiry.getTitle()).isEqualTo(updateRequestDTO.getTitle());
        assertThat(updatedInquiry.getContent()).isEqualTo(updateRequestDTO.getContent());
        assertThat(updatedInquiry.getUpdatedAt()).isAfterOrEqualTo(savedInquiry.getUpdatedAt()); // 기존 savedInquiry의 시간과 비교
    }

    @Test
    @DisplayName("존재하지 않는 문의글 수정 시도 실패")
    void testUpdateInquiry_inquiryNotFound() {
        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(testMember.getMemberNo())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(999, updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 문의글 수정 시도 실패")
    void testUpdateInquiry_isDeleted() {
        savedInquiry.setIsDeleted(true);
        inquiryRepository.save(savedInquiry);

        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(testMember.getMemberNo())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(savedInquiry.getInquiryId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의는 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 문의글 수정 시도 실패")
    void testUpdateInquiry_notAuthor() {
        MembershipType businessMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_BUSINESS)
                .orElseThrow(() -> new RuntimeException("기업회원 멤버십 타입을 찾을 수 없습니다. 테스트를 위해 DB에 2번 '기업회원' 멤버십 타입이 필요합니다."));

        Member anotherMember = memberRepository.save(Member.builder()
                .loginId("otheruser456")
                .password("otherpassword456!")
                .name("다른테스터회원")
                .birthDate(LocalDate.of(1995, 10, 20))
                .address("부산시 해운대구 마린시티")
                .phone("010-9876-5432")
                .membershipType(businessMembershipType)
                .build());

        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(anotherMember.getMemberNo())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(savedInquiry.getInquiryId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("문의 작성자만 수정할 수 있습니다.");
    }

    // --- 1:1 문의글 삭제 (논리적 삭제) 테스트 🗑️ ---

    @Test
    @DisplayName("문의글 삭제 성공")
    void testDeleteInquiry_success() {
        // given (savedInquiry가 이미 DB에 있어)
        // when
        inquiryService.deleteInquiry(savedInquiry.getInquiryId(), testMember.getMemberNo());

        // then
        // DB에서 실제로 isDeleted가 true로 변경되었는지 확인
        // ⭐️ 여기 수정! .longValue() 제거!
        Inquiry deletedInquiry = inquiryRepository.findById(savedInquiry.getInquiryId()).orElse(null);
        assertThat(deletedInquiry).isNotNull();
        assertThat(deletedInquiry.getIsDeleted()).isTrue();

        // 삭제 후 업데이트 시간은 이전과 같거나 이후여야 함
        assertThat(deletedInquiry.getUpdatedAt()).isAfterOrEqualTo(savedInquiry.getUpdatedAt());
    }

    @Test
    @DisplayName("존재하지 않는 문의글 삭제 시도 실패")
    void testDeleteInquiry_inquiryNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.deleteInquiry(999, testMember.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 삭제된 문의글 다시 삭제 시도 실패")
    void testDeleteInquiry_alreadyDeleted() {
        savedInquiry.setIsDeleted(true);
        inquiryRepository.save(savedInquiry);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.deleteInquiry(savedInquiry.getInquiryId(), testMember.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의입니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 문의글 삭제 시도 실패")
    void testDeleteInquiry_notAuthor() {
        MembershipType adminMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자 멤버십 타입을 찾을 수 없습니다. 테스트를 위해 DB에 1번 '관리자' 멤버십 타입이 필요합니다."));

        Member anotherMember = memberRepository.save(Member.builder()
                .loginId("super_other789")
                .password("superpassword789!")
                .name("슈퍼테스터회원")
                .birthDate(LocalDate.of(2000, 1, 1))
                .address("대구시 북구 동대구로")
                .phone("010-1111-2222")
                .membershipType(adminMembershipType)
                .build());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.deleteInquiry(savedInquiry.getInquiryId(), anotherMember.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("문의 작성자만 삭제할 수 있습니다.");
    }
}