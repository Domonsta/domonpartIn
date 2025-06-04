package com.example.b03.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class InquiryPageResponseDTO<E> { // 제네릭 타입으로 받아서 어떤 DTO든 페이징 처리 가능하도록!

    private List<E> dtoList; // 페이징 처리된 데이터 리스트
    private int totalCount; // 총 항목 수
    private int page; // 현재 페이지 번호
    private int size; // 페이지 사이즈
    private int start; // 시작 페이지 번호
    private int end; // 끝 페이지 번호
    private boolean prev; // 이전 페이지 존재 여부
    private boolean next; // 다음 페이지 존재 여부

    @Builder(builderMethodName = "withAll")
    public InquiryPageResponseDTO(List<E> dtoList, int totalCount, int page, int size) {
        this.dtoList = dtoList;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;

        // 페이징 계산 로직 (10개씩 묶는다고 가정)
        this.end = (int)(Math.ceil(this.page / 10.0)) * 10;
        this.start = this.end - 9;

        int realEnd = (int)(Math.ceil(totalCount / (double)size));
        if (realEnd < this.end) {
            this.end = realEnd;
        }

        this.prev = this.start > 1;
        this.next = this.end < realEnd;
    }
}