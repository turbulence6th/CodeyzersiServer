package com.codeyzer.codeyzersi.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMoveRequest {
    private int[][] board;
    private String difficulty;
} 