/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlesnake;

import com.battlesnake.board.Board;
import com.battlesnake.board.BoardGame;
import com.battlesnake.data.*;
import java.util.*;

import com.battlesnake.math.Point;
import com.battlesnake.minimax.Minimax;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {

    @RequestMapping(value="/", method=RequestMethod.GET, produces="application/json")
    public StartResponse get() {
        return new StartResponse()
                .setApiVersion("1")
                .setName("Crimson Snake")
                .setColor("#990000")
                .setHeadType(HeadType.PIXEL)
                .setTailType(TailType.BOLT)
                .setTaunt("Crimson Snakeee");
    }

    @RequestMapping(value="/move", method=RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        MoveResponse moveResponse = new MoveResponse();
        
        Snake mySnake = findOurSnake(request); // kind of handy to have our snake at this level
        Minimax board = request.getBoard();
        int turn = request.getTurn();
        board.init(mySnake, turn);

        //Move move = mySnake.move(board, findEnemySnake(request, mySnake));
        Move move = mySnake.move(board, findEnemySnake(request, mySnake));

        return moveResponse.setMove(move);
    }

    @RequestMapping(value="/end", method=RequestMethod.POST)
    public Object end() {
        // No response required
        Map<String, Object> responseObject = new HashMap<String, Object>();
        return responseObject;
    }

    /*
     *  Go through the snakes and find your team's snake
     *  
     *  @param  request The MoveRequest from the server
     *  @return         Your team's snake
     */
    private Snake findOurSnake(MoveRequest request) {
        String myUuid = request.getYou().getId();
        List<Snake> snakes = request.getBoard().getSnakes();
        return snakes.stream().filter(thisSnake -> thisSnake.getId().equals(myUuid)).findFirst().orElse(null);
    }

    private Snake findEnemySnake(MoveRequest request, Snake mySnake){
        List<Snake> snakes = request.getBoard().getSnakes();

        Snake enemy = null;
        //get max distance
        double distance = 1000;
        for (Snake s : snakes) {
            if (!s.equals(mySnake)) {
                double dist = Point.distance(mySnake.getHead(), s.getHead());
                if(dist < distance){
                    distance = dist;
                    enemy = s;
                }
            }
        }
        return enemy;
    }


    /*
     *  Simple algorithm to find food
     *  
     *  @param  request The MoveRequest from the server
     *  @param  request An integer array with the X,Y coordinates of your snake's head
     *  @return         A Move that gets you closer to food
     */    
    public ArrayList<Move> moveTowardsFood(MoveRequest request, Point mySnakeHead) {
        ArrayList<Move> towardsFoodMoves = new ArrayList<>();

        //if no food return
        if(request.getBoard().getFood().isEmpty()) return null;

        Point firstFoodLocation = request.getBoard().getFood().get(0);

        if (firstFoodLocation.getX() < mySnakeHead.getX()) {
            towardsFoodMoves.add(Move.LEFT);
        }

        if (firstFoodLocation.getX() > mySnakeHead.getX()) {
            towardsFoodMoves.add(Move.RIGHT);
        }

        if (firstFoodLocation.getY() < mySnakeHead.getY()) {
            towardsFoodMoves.add(Move.UP);
        }

        if (firstFoodLocation.getY() > mySnakeHead.getY()) {
            towardsFoodMoves.add(Move.DOWN);
        }

        return towardsFoodMoves;
    }

}
