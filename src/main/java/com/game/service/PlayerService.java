package com.game.service;

import com.game.entity.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerService {
    List<Player> getAll();
    void delete(long id);
    Player update(Player player);
    Player create(Player player);
    Optional <Player> get(long id);
    boolean findById(long id);

}
