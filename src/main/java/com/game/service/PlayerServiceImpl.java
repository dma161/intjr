package com.game.service;

import com.game.entity.Player;
import com.game.exception_handling.BadRequeException;
import com.game.exception_handling.NoSuchPlayerException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;


@Service

public class PlayerServiceImpl implements PlayerService{

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void delete(long id) {


            playerRepository.deleteById(id);


    }

    @Override
    public List<Player> getAll() {
        return playerRepository.findAll();

    }

    @Override
    public Player update(Player player1) {

    return playerRepository.saveAndFlush(player1);
    }

    @Override
    public Player create(Player player) {


        return playerRepository.saveAndFlush(player);
    }

    @Override
    public Optional <Player> get(long id) {

    return playerRepository.findById(id);

    }

    @Override
    public boolean findById(long id){
        return playerRepository.existsById(id);
    }

}
