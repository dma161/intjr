package com.game.controller;


import com.game.entity.Player;


import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception_handling.BadRequeException;
import com.game.exception_handling.NoSuchPlayerException;
import com.game.exception_handling.Oki;
import com.game.exception_handling.PlayerIncorrectData;
import com.game.service.PlayerService;
import com.mysql.cj.protocol.x.Ok;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController

@Transactional

public class PlayersController {


    @Autowired
    private PlayerService service;

    @RequestMapping (value = "/rest/players", method = RequestMethod.GET)
    @ResponseBody
    public List<Player> showAllPlayers(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race, @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after, @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false) Boolean banned,@RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience, @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel, @RequestParam(value = "order", required = false)  PlayerOrder order,
                                       @RequestParam(value = "pageNumber", required = false) Integer pageNumber,@RequestParam(value = "pageSize", required = false) Integer pageSize){


        if (pageSize == null)   pageSize = 3;
        if (pageNumber == null) pageNumber = 0;
        if (order == null) order = PlayerOrder.ID;
        PlayerOrder finalOrder = order;
        return service.getAll().stream()
//               .sorted(((player1, player2) -> {
//                   if (PlayerOrder.LEVEL.equals(finalOrder)) {
//                       return player1.getLevel().compareTo(player2.getLevel());
//                   }
//                   if (PlayerOrder.BIRTHDAY.equals(finalOrder)) {
//                       return player1.getBirthday().compareTo(player2.getBirthday());
//                   }
//
//                   if (PlayerOrder.EXPERIENCE.equals(finalOrder)) {
//                       return player1.getExperience().compareTo(player2.getExperience());
//                   }
//                   if (PlayerOrder.NAME.equals(finalOrder)) {
//                       return player1.getName().compareTo(player2.getName());
//                   }
//                   return player1.getId().compareTo(player2.getId());
//               }))
                .filter(player -> (name == null || player.getName().contains(name)))
                .filter(player -> (minExperience == null || player.getExperience() >= minExperience))
                .filter(player -> title == null || player.getTitle().contains(title))
                .filter(player ->( maxExperience == null || player.getExperience() <= maxExperience))
                .filter(player -> (race == null || player.getRace().equals(race)))
                .filter(player -> (profession == null || player.getProfession().equals(profession)))
                .filter(player -> (after == null || player.getBirthday().getTime() > after))
                .filter(player -> (before == null || player.getBirthday().getTime() < before))
                .filter(player -> banned == null || player.getBanned() == banned)
                .filter(player -> minLevel == null || player.getLevel() >= minLevel)
                .filter(player -> maxLevel == null || player.getLevel() <= maxLevel)
                .skip((long) pageSize * pageNumber)
                .limit(pageSize)

                .collect(Collectors.toList());
    }


    @DeleteMapping(value = "/rest/players/{id}")
    @ResponseBody
    public void delete(@PathVariable long id ) {

        if (id <= 0) throw new BadRequeException("");
      if (!service.findById(id)) throw new NoSuchPlayerException("");

        service.delete(id);



    }

    @RequestMapping (value = "/rest/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Player update(@RequestBody  Player player,  @PathVariable long id){

        if ( id <= 0) throw new BadRequeException("");
        if (!service.findById(id) ) throw new NoSuchPlayerException("");


        Player player1 = service.get(id).get();
        if (player.getName() != null) {
            if (player.getName().length() > 12 || player.getName().equals("")) throw new BadRequeException("");
            else player1.setName(player.getName());
        }

        if (player.getBanned() != null) {
            player1.setBanned(player.getBanned());
        }
        if (player.getTitle() != null) {
            if (player.getTitle().length() > 30 || player.getTitle() == null || player.getTitle().isEmpty()) throw new BadRequeException("");

            else player1.setTitle(player.getTitle());
        }

        if (player.getRace() != null) {
            if (player.getRace() == null )throw new BadRequeException("");

            else player1.setRace(player.getRace());
        }

        if (player.getExperience() != null) {
            if (player.getExperience() == null || player.getExperience() < 0 || player.getExperience() > 10000000)
                throw new BadRequeException("");
            else player1.setExperience(player.getExperience());
        }
        if (player.getProfession() != null) {
            if (player.getProfession() == null )throw new BadRequeException("");

            else player1.setProfession(player.getProfession());
        }

        if (player.getBirthday() != null) {
            if (player.getBirthday() == null || player.getBirthday().getTime() == 0  )throw new BadRequeException("");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(player.getBirthday().getTime());
            if (calendar.get(Calendar.YEAR) < 2000L || calendar.get(Calendar.YEAR) > 3000L) throw new BadRequeException("");
            else player1.setBirthday(player.getBirthday());
        }


        player1.setLevel((int) (Math.sqrt(2500 + 200 * player1.getExperience()) - 50 )/ 100);


        player1.setUntilNextLevel(50 * (player1.getLevel() + 1) * (player1.getLevel() + 2) - player1.getExperience());

    return  service.update(player1);

    }
    @RequestMapping (value = "/rest/players", method = RequestMethod.POST)
    @ResponseBody
    public Player create(@RequestBody  Player player){

        if (player.getBirthday() == null || player.getBirthday().getTime() < 0 || player.getTitle().length() > 30 || player.getName().length() > 12 ||
            player.getName().equals("") || badBodyTheRequest(player) ) throw new BadRequeException("");
        player.setLevel(getLevelPlayer(player));
        player.setUntilNextLevel(getUntilNextLevelPlayer(player));
        return  service.create(player);

    }

    @GetMapping ("/rest/players/{id}")
    @ResponseBody
    public Player  getPlayer(@PathVariable long id)  {
        if (id <= 0   ) throw new BadRequeException("");

        if (!service.findById(id)) throw new NoSuchPlayerException("");
        return  service.get(id).get();
    }
    @RequestMapping (value = "/rest/players/count", method = RequestMethod.GET)
    @ResponseBody
    public int getPlayersCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "race", required = false) Race race, @RequestParam(value = "profession", required = false) Profession profession,
                               @RequestParam(value = "after", required = false) Long after, @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "banned", required = false) Boolean banned,@RequestParam(value = "minExperience", required = false) Integer minExperience,
                               @RequestParam(value = "maxExperience", required = false) Integer maxExperience, @RequestParam(value = "minLevel", required = false) Integer minLevel,
                               @RequestParam(value = "maxLevel", required = false) Integer maxLevel){

        return (int) service.getAll().stream()
                .filter(player -> minLevel == null || player.getLevel() > minLevel)
                .filter(player -> minExperience == null || player.getExperience() > minExperience)
                .filter(player -> name == null || player.getName().contains(name))
                .filter(player -> maxLevel == null || player.getLevel() <= maxLevel)
                .filter(player -> title == null || player.getTitle().contains(title))
                .filter(player -> race == null || player.getRace().equals(race))
                .filter(player -> profession == null || player.getProfession().equals(profession))
                .filter(player -> maxExperience == null || player.getExperience() > maxExperience)
                .filter(player -> before == null || player.getBirthday().getTime() < before)
                .filter(player -> banned == null || player.getBanned() == banned)


                .count();

    }

    public boolean badBodyTheRequest(Player player){
        return (player.getExperience() < 0) || (player.getExperience() > 10000000);
    }
    public int getLevelPlayer(Player player){

        return((int)(Math.sqrt(2500 + 200 * player.getExperience() - 50) / 100)) ;
    }
    public int getUntilNextLevelPlayer(Player player){
        return(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience()) ;
    }
}
