package com.dev.Attraction.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientScrollRes {
	private List<RecipientItemRes> items;
	private Long nextCursor; // 다음 요청 시 cursor로 보낼 값(마지막 id)
	private boolean hasMore;
}