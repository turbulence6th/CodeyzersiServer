package com.codeyzer.codeyzersi.controller;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeyzer.codeyzersi.ai.GameMove;
import com.codeyzer.codeyzersi.ai.ReversiAI;
import com.codeyzer.codeyzersi.model.GameMoveRequest;
import com.codeyzer.codeyzersi.model.Position;

@RestController
@RequestMapping(path = "/move")
public class MoveController {
    
    private static final Logger logger = Logger.getLogger(MoveController.class.getName());

    @PostMapping
    public Position move(@RequestBody GameMoveRequest request) {
        int[][] board = request.getBoard();
        String difficulty = request.getDifficulty();
        
        logger.info("Zorluk seviyesi: " + difficulty);
                
        // Tahta durumunu logla
        logger.info("Tahta durumu (0=boş, 1=oyuncu, -1=rakip):");
        for (int i = 0; i < board.length; i++) {
            logger.info(Arrays.toString(board[i]));
        }
        
        // Yeni ReversiAI nesnesi oluştur
        ReversiAI ai = new ReversiAI();
        // AI oyuncu olarak 1 rengini kullan
        ai.setColor(1);
        // Zorluk seviyesini ayarla
        ai.setDifficulty(difficulty);
        
        // Olası hamleleri kontrol et
        List<GameMove> possibleMoves = ai.getPossibleMoves(board, 1);
        logger.info("Olası hamle sayısı: " + possibleMoves.size());
        
        // Olası hamleleri logla
        for (GameMove move : possibleMoves) {
            logger.info("Hamle: [" + move.getPosition()[0] + "," + move.getPosition()[1] + "], çevrilen taş sayısı: " + move.getFlips().size());
        }
        
        // En iyi hamleyi al
        GameMove bestMove = ai.getBestMove(board);

        if (bestMove == null) {
            logger.warning("Geçerli hamle bulunamadı!");
            return null;
        }

        // Pozisyon bilgisini al
        int[] pos = bestMove.getPosition();
        logger.info("Seçilen hamle: [" + pos[0] + "," + pos[1] + "]");
        
        // Position oluştur
        Position position = new Position();
        position.setX(Integer.valueOf(pos[0]));
        position.setY(Integer.valueOf(pos[1]));
        return position;
    }
} 