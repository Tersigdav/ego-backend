package com.ego.backend.controller;

import com.ego.backend.dto.CommandRequest;
import com.ego.backend.service.GameService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "message",
                "Benvenuto nel progetto EGO.\n Questa è una versione dimostrativa del gioco.\nLista comandi:" +
                        "\nVai a [direzione] - ti sposti a nord, sud, est od ovest.\nPrendi [oggetto] - prendi l'oggetto da te indicato.\nSposta [oggetto] - sposti l'oggetto da te indicato,\nPesca - In zone specifiche è possibile pescare.\nGuarda - ottieni la descrizione della stanza"
        );
    }

    @PostMapping("/command")
    public Map<String, Object> command(
            @RequestBody CommandRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();

        return gameService.processCommand(
                request.getCommand(),
                username
        );
    }
}