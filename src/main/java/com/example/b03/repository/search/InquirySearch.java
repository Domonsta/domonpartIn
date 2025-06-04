package com.example.b03.repository.search; // ⭐ 패키지명을 com.example.b03.repository.search 로!

import com.example.b03.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquirySearch { // ⭐ InquirySearchImpl -> InquirySearch 로 이름 변경 추천!

    // 검색 조건(searchType, keyword)에 따라 문의글을 검색하는 메서드
    // BoardSearch 예제처럼 String[] types와 String keyword를 받을 수 있도록 확장해서 정의해볼게!
    // 이렇게 하면 제목, 내용, 작성자 등으로 유연하게 검색할 수 있어.
    Page<Inquiry> searchAll(String[] types, String keyword, Pageable pageable);

    // ⭐ 도몬이 이전에 사용했던 특정 키워드 검색 메서드도 필요하다면 여기에 다시 정의할 수 있어.
    // Page<Inquiry> findByKeywordContainingAndIsDeletedFalse(String keyword, Pageable pageable);

}