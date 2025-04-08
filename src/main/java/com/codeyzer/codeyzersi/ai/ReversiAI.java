package com.codeyzer.codeyzersi.ai;

import lombok.Getter;

import java.util.*;

public class ReversiAI {

    private static final int[] START = new int[]{3, 3};

    private static final int[][] DIRECTIONS = {
            new int[]{-1, -1}, new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, -1}, new int[]{0, 1},
            new int[]{1, -1}, new int[]{1, 0}, new int[]{1, 1}
    };
    
    private static final int[][] CORNERS = {
            {0, 0}, {0, 7}, {7, 0}, {7, 7}
    };
    
    private static final int[][] CORNER_ADJACENTS = {
            {0, 1}, {1, 0}, {1, 1},           // Top-left corner adjacents
            {0, 6}, {1, 6}, {1, 7},           // Top-right corner adjacents
            {6, 0}, {6, 1}, {7, 1},           // Bottom-left corner adjacents
            {6, 6}, {6, 7}, {7, 6}            // Bottom-right corner adjacents
    };
    
    private static final int[][] EDGE_POSITIONS = {
            {0, 2}, {0, 3}, {0, 4}, {0, 5},   // Top edge
            {2, 0}, {3, 0}, {4, 0}, {5, 0},   // Left edge
            {2, 7}, {3, 7}, {4, 7}, {5, 7},   // Right edge
            {7, 2}, {7, 3}, {7, 4}, {7, 5}    // Bottom edge
    };

    @Getter
    private int color;
    
    @Getter
    private String difficulty = "normal"; // Varsayılan zorluk seviyesi
    
    private Map<String, TranspositionEntry> transpositionTable = new HashMap<>();
    
    private static class TranspositionEntry {
        int depth;
        double score;
        GameMove bestMove;
        
        TranspositionEntry(int depth, double score, GameMove bestMove) {
            this.depth = depth;
            this.score = score;
            this.bestMove = bestMove;
        }
    }
    
    private String getBoardKey(int[][] board) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                key.append(board[i][j]);
            }
        }
        return key.toString();
    }

    private int countPieces(int[][] board, int color) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == color) {
                    count++;
                }
            }
        }
        return count;
    }

    private int getOpponentColor(int color) {
        return -color;
    }

    private int getGamePhase(int[][] board) {
        int totalPieces = countPieces(board, 1) + countPieces(board, -1);
        if (totalPieces < 20) {
            return 0; // Early game
        } else if (totalPieces < 50) {
            return 1; // Mid game
        } else {
            return 2; // End game
        }
    }

    public int[][] getInitialBoard() {
        int[][] board = new int[8][8];
        board[START[0]][START[1]] = -1;
        board[START[0]][START[1] + 1] = 1;
        board[START[0] + 1][START[1]] = 1;
        board[START[0] + 1][START[1] + 1] = -1;
        return board;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setDifficulty(String difficulty) {
        if (difficulty != null && (difficulty.equals("easy") || difficulty.equals("normal") || difficulty.equals("hard"))) {
            this.difficulty = difficulty;
        } else {
            this.difficulty = "normal"; // Geçersiz değer için varsayılan kullan
        }
    }

    public List<GameMove> getPossibleMoves(int[][] board, int color) {
        List<GameMove> moves = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                GameMove move = getFlips(board, i, j, color);
            if (!move.getFlips().isEmpty()) {
                moves.add(move);
                }
            }
        }
        return moves;
    }

    private GameMove getFlips(int[][] board, int x, int y, int color) {
        GameMove move = new GameMove(new int[]{x, y});
        if (board[x][y] != 0) {
            return move;
        }

        int opponent = getOpponentColor(color);
        
        for (int[] direction : DIRECTIONS) {
            int i = x;
            int j = y;

            boolean hasOpponentAdjacent = false;
            List<int[]> candidates = new LinkedList<>();
            while (true) {
                i += direction[0];
                j += direction[1];

                if (i < 0 || i >= 8 || j < 0 || j >= 8) {
                    break;
                }

                if (board[i][j] == 0) {
                    break;
                }
                
                if (board[i][j] == opponent) {
                    hasOpponentAdjacent = true;
                    candidates.add(new int[]{i, j});
                } else if (hasOpponentAdjacent) {
                    move.getFlips().addAll(candidates);
                    move.getOrigins().add(new int[]{i, j});
                    break;
                } else {
                    break;
                }
            }
        }

        return move;
    }

    public int[][] applyMove(int[][] board, GameMove move, int color) {
        int[][] newBoard = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, 8);
        }

        for (int[] flip : move.getFlips()) {
            newBoard[flip[0]][flip[1]] = color;
        }

        int x = move.getPosition()[0];
        int y = move.getPosition()[1];
        newBoard[x][y] = color;
        return newBoard;
    }

    public GameMove getBestMove(int[][] board) {
        List<GameMove> possibleMoves = getPossibleMoves(board, color);
        if (possibleMoves.isEmpty()) {
            return null;
        }

        int gamePhase = getGamePhase(board);
        int depth = determineSearchDepth(gamePhase, possibleMoves.size());
        
        // Search with minimax and alpha-beta pruning
        double bestScore = Double.NEGATIVE_INFINITY;
        GameMove bestMove = null;
        
        // First sort moves for better alpha-beta pruning
        sortMovesForSearch(board, possibleMoves, color);
        
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        for (GameMove move : possibleMoves) {
            int[][] newBoard = applyMove(board, move, color);
            double score = minimax(newBoard, depth - 1, alpha, beta, false, getOpponentColor(color));
            move.setScore(score);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            
            alpha = Math.max(alpha, bestScore);
        }
        
        return bestMove;
    }
    
    private int determineSearchDepth(int gamePhase, int moveCount) {
        // Zorluk seviyesine göre derinlik belirleme
        int baseDepth;
        
        switch (difficulty) {
            case "easy":
                baseDepth = 2;
                break;
            case "hard":
                baseDepth = 6;
                break;
            case "normal":
            default:
                baseDepth = 4;
                break;
        }
        
        // Oyunun aşamasına göre ayarla
        if (gamePhase == 0) { // Early game
            return baseDepth;
        } else if (gamePhase == 1) { // Mid game
            return baseDepth + 1;
        } else { // End game
            // Eğer az sayıda hamle varsa derinliği artır
            if (moveCount < 5) {
                return baseDepth + 3;
            } else {
                return baseDepth + 2;
            }
        }
    }
    
    private void sortMovesForSearch(int[][] board, List<GameMove> moves, int color) {
        // Heuristic-based move ordering for better alpha-beta pruning
        for (GameMove move : moves) {
            int x = move.getPosition()[0];
            int y = move.getPosition()[1];
            double heuristicValue = 0;
            
            // Corner moves get highest priority
            boolean isCorner = false;
            for (int[] corner : CORNERS) {
                if (x == corner[0] && y == corner[1]) {
                    heuristicValue += 100;
                    isCorner = true;
                    break;
                }
            }
            
            if (!isCorner) {
                // Moves adjacent to corners get lowest priority
                boolean isAdjacentToCorner = false;
                for (int[] adjacent : CORNER_ADJACENTS) {
                    if (x == adjacent[0] && y == adjacent[1]) {
                        heuristicValue -= 50;
                        isAdjacentToCorner = true;
                        break;
                    }
                }
                
                if (!isAdjacentToCorner) {
                    // Edge moves get second highest priority
                    boolean isEdge = false;
                    for (int[] edge : EDGE_POSITIONS) {
                        if (x == edge[0] && y == edge[1]) {
                            heuristicValue += 30;
                            isEdge = true;
                            break;
                        }
                    }
                    
                    if (!isEdge) {
                        // Middle positions get medium priority
                        heuristicValue += 10;
                    }
                }
            }
            
            // More flips is generally better
            heuristicValue += move.getFlips().size() * 2;
            
            move.setScore(heuristicValue);
        }
        
        // Sort moves by heuristic value (descending)
        moves.sort((m1, m2) -> Double.compare(m2.getScore(), m1.getScore()));
    }
    
    private double minimax(int[][] board, int depth, double alpha, double beta, boolean isMaximizing, int currentColor) {
        // Check if the game is over or we've reached the maximum depth
        List<GameMove> possibleMoves = getPossibleMoves(board, currentColor);
        
        // Terminal node: no valid moves for current player
        if (possibleMoves.isEmpty()) {
            // Check if the opponent also has no moves (game over)
            List<GameMove> opponentMoves = getPossibleMoves(board, getOpponentColor(currentColor));
            if (opponentMoves.isEmpty()) {
                // Game is over, count the final score
                int playerPieces = countPieces(board, color);
                int opponentPieces = countPieces(board, getOpponentColor(color));
                
                if (playerPieces > opponentPieces) {
                    return 10000; // Win
                } else if (playerPieces < opponentPieces) {
                    return -10000; // Loss
                } else {
                    return 0; // Draw
                }
            }
            
            // Current player has no moves but opponent does
            // Pass turn to opponent
            return minimax(board, depth, alpha, beta, !isMaximizing, getOpponentColor(currentColor));
        }
        
        // Check transposition table first
        String boardKey = getBoardKey(board);
        TranspositionEntry entry = transpositionTable.get(boardKey);
        if (entry != null && entry.depth >= depth) {
            return entry.score;
        }
        
        // Leaf node: evaluate the board
        if (depth == 0) {
            double score = evaluateBoard(board, color);
            // Store in transposition table
            transpositionTable.put(boardKey, new TranspositionEntry(depth, score, null));
            return score;
        }
        
        GameMove bestMove = null;
        double bestScore;
        
        if (isMaximizing) {
            bestScore = Double.NEGATIVE_INFINITY;
            
            for (GameMove move : possibleMoves) {
                int[][] newBoard = applyMove(board, move, currentColor);
                double score = minimax(newBoard, depth - 1, alpha, beta, false, getOpponentColor(currentColor));
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
        } else {
            bestScore = Double.POSITIVE_INFINITY;
            
            for (GameMove move : possibleMoves) {
                int[][] newBoard = applyMove(board, move, currentColor);
                double score = minimax(newBoard, depth - 1, alpha, beta, true, getOpponentColor(currentColor));
                
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
        }
        
        // Store in transposition table
        transpositionTable.put(boardKey, new TranspositionEntry(depth, bestScore, bestMove));
        
        return bestScore;
    }
    
    private double evaluateBoard(int[][] board, int playerColor) {
        int gamePhase = getGamePhase(board);
        
        double score = 0;
        
        // Piece differential (more important in endgame)
        double pieceDiff = evaluatePieceDifferential(board, playerColor);
        
        // Corner control (important throughout the game)
        double cornerControl = evaluateCornerControl(board, playerColor);
        
        // Edge control (important in early and mid game)
        double edgeControl = evaluateEdgeControl(board, playerColor);
        
        // Mobility/stability (important in early and mid game)
        double stability = evaluateStability(board, playerColor);
        
        // Weight factors based on game phase
        if (gamePhase == 0) { // Early game
            score += pieceDiff * 0.1;       // Least important in early game
            score += cornerControl * 5.0;    // Very important
            score += edgeControl * 3.0;      // Important
            score += stability * 4.0;        // Very important
        } else if (gamePhase == 1) { // Mid game
            score += pieceDiff * 1.0;        // More important in mid game
            score += cornerControl * 4.0;    // Very important
            score += edgeControl * 2.0;      // Less important
            score += stability * 3.0;        // Important
        } else { // End game
            score += pieceDiff * 4.0;        // Most important in end game
            score += cornerControl * 3.0;    // Important
            score += edgeControl * 1.0;      // Less important
            score += stability * 2.0;        // Less important
        }
        
        return score;
    }
    
    private double evaluatePieceDifferential(int[][] board, int playerColor) {
        int playerPieces = countPieces(board, playerColor);
        int opponentPieces = countPieces(board, getOpponentColor(playerColor));
        
        // Avoid division by zero
        if (playerPieces + opponentPieces == 0) {
            return 0;
        }
        
        return 100.0 * (playerPieces - opponentPieces) / (playerPieces + opponentPieces);
    }
    
    private double evaluateCornerControl(int[][] board, int playerColor) {
        int playerCorners = 0;
        int opponentCorners = 0;
        
        for (int[] corner : CORNERS) {
            if (board[corner[0]][corner[1]] == playerColor) {
                playerCorners++;
            } else if (board[corner[0]][corner[1]] == getOpponentColor(playerColor)) {
                opponentCorners++;
            }
        }
        
        // Avoid division by zero
        if (playerCorners + opponentCorners == 0) {
            return 0;
        }
        
        return 25.0 * (playerCorners - opponentCorners);
    }
    
    private double evaluateEdgeControl(int[][] board, int playerColor) {
        int playerEdges = 0;
        int opponentEdges = 0;
        
        for (int[] edge : EDGE_POSITIONS) {
            if (board[edge[0]][edge[1]] == playerColor) {
                playerEdges++;
            } else if (board[edge[0]][edge[1]] == getOpponentColor(playerColor)) {
                opponentEdges++;
            }
        }
        
        // Avoid division by zero
        if (playerEdges + opponentEdges == 0) {
            return 0;
        }
        
        return 10.0 * (playerEdges - opponentEdges);
    }
    
    private double evaluateStability(int[][] board, int playerColor) {
        // Count mobility (number of moves)
        List<GameMove> playerMoves = getPossibleMoves(board, playerColor);
        List<GameMove> opponentMoves = getPossibleMoves(board, getOpponentColor(playerColor));
        
        int playerMobility = playerMoves.size();
        int opponentMobility = opponentMoves.size();
        
        // Avoid division by zero
        if (playerMobility + opponentMobility == 0) {
            return 0;
        }
        
        // Penalize moves next to corners if we don't control the corner
        double playerPenalty = 0;
        double opponentPenalty = 0;
        
        // Check each corner and its adjacent positions
        for (int cornerIndex = 0; cornerIndex < CORNERS.length; cornerIndex++) {
            int[] corner = CORNERS[cornerIndex];
            
            // If corner is empty
            if (board[corner[0]][corner[1]] == 0) {
                // Check adjacents and penalize if occupied
                int startIdx = cornerIndex * 3; // 3 adjacents per corner
                for (int i = 0; i < 3; i++) {
                    int[] adjacent = CORNER_ADJACENTS[startIdx + i];
                    if (board[adjacent[0]][adjacent[1]] == playerColor) {
                        playerPenalty += 4.0;
                    } else if (board[adjacent[0]][adjacent[1]] == getOpponentColor(playerColor)) {
                        opponentPenalty += 4.0;
                    }
                }
            }
        }
        
        // Factor in both mobility and stability
        double mobilityScore = 8.0 * (playerMobility - opponentMobility) / (playerMobility + opponentMobility);
        double stabilityScore = (opponentPenalty - playerPenalty) / 2.0;
        
        return mobilityScore + stabilityScore;
    }
} 