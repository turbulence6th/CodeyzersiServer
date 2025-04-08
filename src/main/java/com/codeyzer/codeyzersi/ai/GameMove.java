package com.codeyzer.codeyzersi.ai;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMove implements Comparable<GameMove> {

    private final int[] position;

    private final List<int[]> flips;

    private final List<int[]> origins;
    
    private double score;

    public GameMove(int[] position) {
        this.position = position;
        this.flips = new LinkedList<>();
        this.origins = new LinkedList<>();
        this.score = 0;
    }

    @Override
    public int compareTo(GameMove move) {
        return move.flips.size() - flips.size();
    }

    @Override
    public String toString() {
        return String.format("[x: %d, y: %d]", position[0], position[1]);
    }
} 