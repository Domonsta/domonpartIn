package com.example.b03.repository;

import com.example.b03.domain.Inquiry;
import com.example.b03.repository.search.InquirySearch; // ⭐ InquirySearch import! ⭐
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// ⭐️ InquiryRepository가 JpaRepository와 InquirySearch 인터페이스를 모두 상속받도록 변경!
public interface InquiryRepository extends JpaRepository<Inquiry, Integer>, InquirySearch { // ⭐⭐⭐ 여기에 InquirySearch 상속 추가! ⭐⭐⭐

    // 검색 조건(searchType, keyword)이 없을 때 사용될 메서드
    // 삭제되지 않은 문의글을 최신순(inquiryId)으로 정렬하여 페이징 처리
    Page<Inquiry> findByIsDeletedFalseOrderByInquiryIdDesc(Pageable pageable);

    // ⭐⭐⭐ findByKeywordContainingAndIsDeletedFalse 메서드는 이제 InquirySearchImpl에서 구현되므로, ⭐⭐⭐
    // ⭐⭐⭐ 여기 InquiryRepository에서는 선언할 필요가 없습니다. ⭐⭐⭐
    // (만약 InquirySearch 인터페이스에 이 메서드를 넣었다면, InquirySearch를 통해 호출될 거에요)

    // 특정 회원이 작성한 문의글 목록 조회 (필요하다면 사용)
    Page<Inquiry> findByMember_MemberNoAndIsDeletedFalseOrderByInquiryIdDesc(Integer memberNo, Pageable pageable);

    // 삭제되지 않은 특정 회원의 문의글인지 확인 (특정 용도로 사용 가능)
    boolean existsByInquiryIdAndMember_MemberNoAndIsDeletedFalse(Integer inquiryId, Integer memberNo);

}