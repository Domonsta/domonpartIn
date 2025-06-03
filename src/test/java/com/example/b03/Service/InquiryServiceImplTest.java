package com.example.b03.Service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.Member;
import com.example.b03.domain.MembershipType;
import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryRegisterRequestDTO;
import com.example.b03.dto.InquiryResponseDTO;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import com.example.b03.repository.MembershipTypeRepository;
import com.example.b03.service.InquiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class InquiryServiceImplTest {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    private Member testMember;
    private Inquiry savedInquiry;
    private MembershipType defaultMembershipType;

    @BeforeEach
    void setUp() {
        // 모든 Repository 초기화: 테스트 간 데이터 독립성 보장
        inquiryRepository.deleteAll();
        memberRepository.deleteAll();
        membershipTypeRepository.deleteAll();

        // 1. 테스트용 기본 멤버십 타입 생성 및 저장 (MembershipTypeDTO 필드 매칭)
        defaultMembershipType = MembershipType.builder()
                .typeId((byte) 1)
                .typeName("기본회원")
                .build();
        defaultMembershipType = membershipTypeRepository.save(defaultMembershipType);


        // 2. 테스트용 회원 생성 및 저장 (MemberDTO 필드 매칭)
        testMember = Member.builder()
                .loginId("testuser123")
                .password("testpassword123!")
                .name("테스터회원")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("서울시 강남구 테헤란로 123")
                .phone("010-1234-5678")
                .membershipType(defaultMembershipType) // 생성한 멤버십 타입 연결
//                .createdAt(LocalDateTime.now().minusDays(30)) // 생성일자 추가
//                .updatedAt(LocalDateTime.now().minusDays(30)) // 수정일자 추가
//                .isDeleted(false) // 삭제 여부 추가
                .build();
        testMember = memberRepository.save(testMember);

        // 3. 테스트용 문의글 생성 및 저장 (Inquiry 엔티티 필드 매칭)
        Inquiry inquiry = Inquiry.builder()
                .title("초기 테스트 문의 제목입니다.")
                .content("초기 테스트 문의 내용입니다. 상세한 내용을 포함합니다.")
                .member(testMember) // 위에서 생성한 회원과 연결
//                .isDeleted(false)
//                .createdAt(LocalDateTime.now().minusHours(1))
//                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        savedInquiry = inquiryRepository.save(inquiry);
    }

// **1:1 문의글 전체보기 (목록 조회) 테스트** 📝

    @Test
    @DisplayName("문의글 전체 목록 조회 성공 - InquiryListDTO 매칭 확인")
    void testGetAllInquiries_success() {
        // given
        // 추가 문의글 저장 (InquiryListDTO 필드 고려하여 추가)
        Inquiry inquiry2 = Inquiry.builder()
                .title("두 번째 문의 제목")
                .content("두 번째 문의 내용입니다.")
                .member(testMember)
//                .isDeleted(false)
//                .createdAt(LocalDateTime.now()) // savedInquiry보다 최신이 되도록 설정
                .build();
        inquiryRepository.save(inquiry2);

        // when
        List<InquiryListDTO> result = inquiryService.getAllInquiries();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        // InquiryListDTO 필드 매칭 및 최신순 정렬 확인
        assertThat(result.get(0).getInquiryId()).isEqualTo(inquiry2.getInquiryId());
        assertThat(result.get(0).getTitle()).isEqualTo(inquiry2.getTitle());
        assertThat(result.get(0).getMemberNo()).isEqualTo(inquiry2.getMember().getMemberNo());
        assertThat(result.get(0).getCreatedAt()).isNotNull();

        assertThat(result.get(1).getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(result.get(1).getTitle()).isEqualTo(savedInquiry.getTitle());
        assertThat(result.get(1).getMemberNo()).isEqualTo(savedInquiry.getMember().getMemberNo());
        assertThat(result.get(1).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제된 문의글은 목록 조회에서 제외")
    void testGetAllInquiries_excludeDeleted() {
        // given
        Inquiry deletedInquiry = Inquiry.builder()
                .title("삭제된 문의글입니다.")
                .content("이 문의글은 삭제되어야 합니다.")
                .member(testMember)
//                .isDeleted(true) // 삭제된 상태로 저장
//                .createdAt(LocalDateTime.now().plusDays(1))
                .build();
        inquiryRepository.save(deletedInquiry);

        // when
        List<InquiryListDTO> result = inquiryService.getAllInquiries();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1); // savedInquiry (삭제되지 않은 것) 1개만 조회돼야 함
        assertThat(result.get(0).getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
    }

// **1:1 문의글 상세보기 테스트** 🔎

    @Test
    @DisplayName("문의글 상세 보기 성공 - InquiryResponseDTO 매칭 확인")
    void testGetInquiryDetail_success() {
        // given (savedInquiry가 이미 DB에 저장되어 있음)

        // when
        InquiryResponseDTO result = inquiryService.getInquiryDetail(savedInquiry.getInquiryId());

        // then
        assertThat(result).isNotNull();
        // InquiryResponseDTO 필드 매칭 및 검증
        assertThat(result.getInquiryId()).isEqualTo(savedInquiry.getInquiryId());
        assertThat(result.getTitle()).isEqualTo(savedInquiry.getTitle());
        assertThat(result.getContent()).isEqualTo(savedInquiry.getContent());
        assertThat(result.getMemberNO()).isEqualTo(testMember.getMemberNo());
        assertThat(result.getCreatedAt()).isEqualTo(savedInquiry.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(savedInquiry.getUpdatedAt());
        // isDeleted는 InquiryResponseDTO에 없으므로 검증하지 않음
    }

    @Test
    @DisplayName("존재하지 않는 문의글 상세 보기 실패")
    void testGetInquiryDetail_notFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.getInquiryDetail(999); // 존재하지 않는 ID
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 문의글 상세 보기 실패")
    void testGetInquiryDetail_isDeleted() {
        // given
        savedInquiry.setIsDeleted(true); // 테스트 문의를 삭제된 상태로 변경
        inquiryRepository.save(savedInquiry); // DB에 변경사항 반영

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.getInquiryDetail(savedInquiry.getInquiryId());
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의입니다.");
    }

// **1:1 문의글 작성 테스트** ✍️

    @Test
    @DisplayName("문의글 작성 성공 - InquiryRegisterRequestDTO 매칭 확인")
    void testRegisterInquiry_success() {
        // given
        InquiryRegisterRequestDTO requestDTO = InquiryRegisterRequestDTO.builder()
                .title("새로 작성할 문의 제목입니다.")
                .content("새로운 문의 내용입니다. 길게 작성해봅니다.")
                .memberNo(testMember.getMemberNo()) // 기존에 저장된 회원의 번호 사용
                .build();

        // when
        InquiryResponseDTO result = inquiryService.registerInquiry(requestDTO);

        // then
        assertThat(result).isNotNull();
        // InquiryResponseDTO 필드 매칭 및 검증
        assertThat(result.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(result.getMemberNO()).isEqualTo(requestDTO.getMemberNo());
        assertThat(result.getInquiryId()).isNotNull(); // ID가 잘 생성되었는지 확인
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // DB에서 실제로 저장되었는지 확인
        Inquiry foundInquiry = inquiryRepository.findById(result.getInquiryId()).orElse(null);
        assertThat(foundInquiry).isNotNull();
        assertThat(foundInquiry.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(foundInquiry.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(foundInquiry.getMember().getMemberNo()).isEqualTo(requestDTO.getMemberNo());
        assertThat(foundInquiry.getIsDeleted()).isFalse(); // 등록 시 isDeleted는 false여야 함
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 문의글 작성 실패")
    void testRegisterInquiry_memberNotFound() {
        // given
        InquiryRegisterRequestDTO requestDTO = InquiryRegisterRequestDTO.builder()
                .title("새로 작성할 문의")
                .content("새로운 문의 내용입니다.")
                .memberNo(999) // 존재하지 않는 회원 번호
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.registerInquiry(requestDTO);
        });
        assertThat(exception.getMessage()).contains("해당 회원을 찾을 수 없습니다.");
    }

//    **1:1 문의글 수정 테스트** ✏️

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
        // InquiryResponseDTO 필드 매칭 및 검증
        assertThat(result.getTitle()).isEqualTo(updateRequestDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(updateRequestDTO.getContent());
        assertThat(result.getInquiryId()).isEqualTo(savedInquiry.getInquiryId()); // ID는 동일해야 함
        assertThat(result.getMemberNO()).isEqualTo(testMember.getMemberNo());
        assertThat(result.getCreatedAt()).isEqualTo(savedInquiry.getCreatedAt()); // 등록일은 변하지 않아야 함
        assertThat(result.getUpdatedAt()).isAfter(savedInquiry.getUpdatedAt()); // 수정일은 변경되어야 함

        // DB에서 실제로 수정되었는지 확인
        Inquiry updatedInquiry = inquiryRepository.findById(savedInquiry.getInquiryId()).orElse(null);
        assertThat(updatedInquiry).isNotNull();
        assertThat(updatedInquiry.getTitle()).isEqualTo(updateRequestDTO.getTitle());
        assertThat(updatedInquiry.getContent()).isEqualTo(updateRequestDTO.getContent());
        assertThat(updatedInquiry.getUpdatedAt()).isAfter(savedInquiry.getUpdatedAt());
    }

    @Test
    @DisplayName("존재하지 않는 문의글 수정 시도 실패")
    void testUpdateInquiry_inquiryNotFound() {
        // given
        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(testMember.getMemberNo())
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(999, updateRequestDTO); // 존재하지 않는 ID
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 문의글 수정 시도 실패")
    void testUpdateInquiry_isDeleted() {
        // given
        savedInquiry.setIsDeleted(true);
        inquiryRepository.save(savedInquiry); // DB에 변경사항 반영

        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(testMember.getMemberNo())
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(savedInquiry.getInquiryId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의는 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 문의글 수정 시도 실패")
    void testUpdateInquiry_notAuthor() {
        // given
        // 다른 회원 생성 시 MembershipType도 연결
        MembershipType newMembershipType = MembershipType.builder()
                .typeId((byte) 2)
                .typeName("우수회원")
                .build();
        newMembershipType = membershipTypeRepository.save(newMembershipType);

        Member anotherMember = Member.builder()
                .loginId("otheruser456")
                .password("otherpassword456!")
                .name("다른테스터회원")
                .birthDate(LocalDate.of(1995, 10, 20))
                .address("부산시 해운대구 마린시티")
                .phone("010-9876-5432")
                .membershipType(newMembershipType)
//                .createdAt(LocalDateTime.now().minusDays(10))
//                .updatedAt(LocalDateTime.now().minusDays(10))
//                .isDeleted(false)
                .build();
        anotherMember = memberRepository.save(anotherMember);

        InquiryRegisterRequestDTO updateRequestDTO = InquiryRegisterRequestDTO.builder()
                .title("수정될 제목")
                .content("수정될 내용")
                .memberNo(anotherMember.getMemberNo()) // 다른 회원의 번호로 요청
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.updateInquiry(savedInquiry.getInquiryId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("문의 작성자만 수정할 수 있습니다.");
    }

// **1:1 문의글 삭제 (논리적 삭제) 테스트** 🗑️

    @Test
    @DisplayName("문의글 삭제 성공")
    void testDeleteInquiry_success() {
        // given (savedInquiry가 이미 DB에 있어)

        // when
        inquiryService.deleteInquiry(savedInquiry.getInquiryId(), testMember.getMemberNo());

        // then
        // DB에서 실제로 isDeleted가 true로 변경되었는지 확인
        Inquiry deletedInquiry = inquiryRepository.findById(savedInquiry.getInquiryId()).orElse(null);
        assertThat(deletedInquiry).isNotNull();
        assertThat(deletedInquiry.getIsDeleted()).isTrue();
        assertThat(deletedInquiry.getUpdatedAt()).isAfter(savedInquiry.getUpdatedAt()); // 수정일자도 업데이트되어야 함
    }

    @Test
    @DisplayName("존재하지 않는 문의글 삭제 시도 실패")
    void testDeleteInquiry_inquiryNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.deleteInquiry(999, testMember.getMemberNo()); // 존재하지 않는 ID
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 삭제된 문의글 다시 삭제 시도 실패")
    void testDeleteInquiry_alreadyDeleted() {
        // given
        savedInquiry.setIsDeleted(true);
        inquiryRepository.save(savedInquiry); // DB에 변경사항 반영

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryService.deleteInquiry(savedInquiry.getInquiryId(), testMember.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 문의입니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 문의글 삭제 시도 실패")
    void testDeleteInquiry_notAuthor() {
        // given
        // 다른 회원 생성 시 MembershipType도 연결
        MembershipType newMembershipType = MembershipType.builder()
                .typeId((byte) 3)
                .typeName("프리미엄회원")
                .build();
        newMembershipType = membershipTypeRepository.save(newMembershipType);

        // ✨✨✨ 여기에 수정! `anotherMember`를 선언과 동시에 저장 결과를 할당! ✨✨✨
        Member anotherMember = memberRepository.save(Member.builder() // save 결과를 바로 변수에 할당
                .loginId("super_other789")
                .password("superpassword789!")
                .name("슈퍼테스터회원")
                .birthDate(LocalDate.of(2000, 1, 1))
                .address("대구시 북구 동대구로")
                .phone("010-1111-2222")
                .membershipType(newMembershipType)
//                .createdAt(LocalDateTime.now().minusDays(5))
//                .updatedAt(LocalDateTime.now().minusDays(5))
//                .isDeleted(false)
                .build()); // 💡 여기 닫는 괄호 하나 더 추가!

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // 이제 anotherMember는 effectively final이므로 람다에서 사용 가능
            inquiryService.deleteInquiry(savedInquiry.getInquiryId(), anotherMember.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("문의 작성자만 삭제할 수 있습니다.");
    }
}