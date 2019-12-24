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

import com.battlesnake.data.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {

    @RequestMapping(value="/start", method=RequestMethod.POST, produces="application/json")
    public StartResponse start(@RequestBody StartRequest request) {
        return new StartResponse()
                .setName("Crimson Snake")
                .setColor("#990000")
                .setHeadType(HeadType.FANG)
                .setTailType(TailType.FATRATTLE)
                .setTaunt("Crimson Snakeee");
    }

    @RequestMapping(value="/move", method=RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        MoveResponse moveResponse = new MoveResponse();
        
        Snake mySnake = findOurSnake(request); // kind of handy to have our snake at this level
        
        List<Move> towardsFoodMoves = moveTowardsFood(request, mySnake.getCoords()[0]);
        
        if (towardsFoodMoves != null && !towardsFoodMoves.isEmpty()) {
            return moveResponse.setMove(towardsFoodMoves.get(0)).setTaunt("I'm hungry");
        } else {
            return moveResponse.setMove(Move.DOWN).setTaunt("Oh Drat");
        }
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
        String myUuid = request.getYou();
        List<Snake> snakes = request.getSnakes();
        return snakes.stream().filter(thisSnake -> thisSnake.getId().equals(myUuid)).findFirst().orElse(null);
    }


    /*
     *  Simple algorithm to find food
     *  
     *  @param  request The MoveRequest from the server
     *  @param  request An integer array with the X,Y coordinates of your snake's head
     *  @return         A Move that gets you closer to food
     */    
    public ArrayList<Move> moveTowardsFood(MoveRequest request, int[] mySnakeHead) {
        ArrayList<Move> towardsFoodMoves = new ArrayList<>();

        int[] firstFoodLocation = request.getFood()[0];

        if (firstFoodLocation[0] < mySnakeHead[0]) {
            towardsFoodMoves.add(Move.LEFT);
        }

        if (firstFoodLocation[0] > mySnakeHead[0]) {
            towardsFoodMoves.add(Move.RIGHT);
        }

        if (firstFoodLocation[1] < mySnakeHead[1]) {
            towardsFoodMoves.add(Move.UP);
        }

        if (firstFoodLocation[1] > mySnakeHead[1]) {
            towardsFoodMoves.add(Move.DOWN);
        }

        return towardsFoodMoves;
    }

}
