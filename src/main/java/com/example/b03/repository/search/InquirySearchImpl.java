// src/main/java/com/example/b03/repository/search/InquirySearchImpl.java
package com.example.b03.repository.search;

import com.example.b03.domain.Inquiry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery; // TypedQuery 임포트
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport; // QuerydslRepositorySupport는 필요 없음!

import java.util.List;

// ⭐⭐ QuerydslRepositorySupport를 상속받지 않습니다! ⭐⭐S
// ⭐⭐ 클래스 이름 InquirySearchImpl 이고, InquirySearch 인터페이스를 구현합니다! ⭐⭐
public class InquirySearchImpl implements InquirySearch { // ⭐⭐ QuerydslRepositorySupport 상속 제거! ⭐⭐

    @PersistenceContext // EntityManager 주입
    private EntityManager entityManager;

    // 생성자도 QuerydslRepositorySupport를 상속받지 않으므로 필요 없습니다!
    // public InquirySearchImpl() {
    //     super(Inquiry.class);
    // }


    @Override // InquirySearch 인터페이스의 searchAll 메서드를 구현합니다.
    public Page<Inquiry> searchAll(String[] types, String keyword, Pageable pageable) {

        StringBuilder jpql = new StringBuilder("SELECT i FROM Inquiry i LEFT JOIN i.member m WHERE i.isDeleted = FALSE");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(i) FROM Inquiry i LEFT JOIN i.member m WHERE i.isDeleted = FALSE");

        // 검색 조건 처리 (types, keyword)
        if (types != null && types.length > 0 && keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (");
            countJpql.append(" AND (");

            boolean firstCondition = true;
            for (String type : types) {
                if (!firstCondition) {
                    jpql.append(" OR ");
                    countJpql.append(" OR ");
                }
                switch (type) {
                    case "t": // 제목 (title) 검색
                        jpql.append("i.title LIKE :keyword");
                        countJpql.append("i.title LIKE :keyword");
                        break;
                    case "c": // 내용 (content) 검색
                        jpql.append("i.content LIKE :keyword");
                        countJpql.append("i.content LIKE :keyword");
                        break;
                    case "w": // 작성자 (writer/member name) 검색
                        jpql.append("m.name LIKE :keyword"); // m은 member의 별칭
                        countJpql.append("m.name LIKE :keyword");
                        break;
                    // 추가적인 검색 조건이 있다면 여기에 case 추가
                }
                firstCondition = false;
            }
            jpql.append(")");
            countJpql.append(")");
        }

        // 정렬 (최신순)
        jpql.append(" ORDER BY i.inquiryId DESC");

        // ⭐ 내용(content) 쿼리
        TypedQuery<Inquiry> contentQuery = entityManager.createQuery(jpql.toString(), Inquiry.class);
        if (keyword != null && !keyword.trim().isEmpty()) {
            contentQuery.setParameter("keyword", "%" + keyword + "%"); // LIKE 검색을 위해 % 추가
        }

        contentQuery.setFirstResult((int) pageable.getOffset()); // 페이징 시작 오프셋
        contentQuery.setMaxResults(pageable.getPageSize()); // 페이지당 개수

        List<Inquiry> content = contentQuery.getResultList();

        // ⭐ 전체 개수(total) 쿼리
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        if (keyword != null && !keyword.trim().isEmpty()) {
            countQuery.setParameter("keyword", "%" + keyword + "%");
        }
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}