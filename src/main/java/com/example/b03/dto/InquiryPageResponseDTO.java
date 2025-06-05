package com.example.b03.dto;

import lombok.Builder;
import lombok.Data; // @Data 어노테이션 안에 @Getter가 포함되어 있어!

import java.util.List;

@Data // @Data 어노테이션이 @Getter, @Setter 등을 포함하고 있어서 필드에 접근 가능해!
public class InquiryPageResponseDTO<E> {

    private List<E> dtoList;
    private int totalCount;
    private int page;
    private int size;

    // ⭐⭐⭐ 필드 이름을 startPage와 endPage로 변경! ⭐⭐⭐
    private int startPage;
    private int endPage;

    private boolean prev;
    private boolean next;

    @Builder(builderMethodName = "withAll")
    public InquiryPageResponseDTO(List<E> dtoList, int totalCount, int page, int size) {
        this.dtoList = dtoList;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;

        // ⭐⭐⭐ 계산 로직에서도 필드 이름을 변경! ⭐⭐⭐
        this.endPage = (int)(Math.ceil(this.page / 10.0)) * 10;
        this.startPage = this.endPage - 9;

        int realEnd = (int)(Math.ceil(totalCount / (double)size));
        if (realEnd < this.endPage) {
            this.endPage = realEnd;
        }

        this.prev = this.startPage > 1;
        this.next = this.endPage < realEnd;

        // totalCount가 0인 경우를 고려 (prev, next, startPage, endPage가 이상해지는 것 방지)
        if (totalCount == 0) {
            this.startPage = 1;
            this.endPage = 1;
            this.prev = false;
            this.next = false;
        }
    }
}