// 11. Inquiry
package com.example.b03.repository;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    List<Inquiry> findByMember(Member member);

    List<Inquiry> findAllByIsDeletedFalseOrderByCreatedAtDesc();
    //특정 문의에 대한 삭제되지 않은 답변을 생성일자 기준 오름차순으로 가져옴
}
