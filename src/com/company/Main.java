package com.company;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static javax.swing.JOptionPane.showInputDialog;

public class Main {

    final static int GAME_LENGTH = 20;
    final static int GAME_WIDTH = 30;
    static String direction;
    static String crazyDirection;
    static int score = 0;
    static File file = new File("C:\\Users\\Administrator\\IdeaProjects\\BetterSnake\\highscore.txt");

    public static void main(String[] args) throws IOException, InterruptedException {
	    //Taking your name for highscore list
        String playerName = JOptionPane.showInputDialog("Hej och välkommen, vad heter du?");
        Terminal terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"));
        terminal.enterPrivateMode();
        Random rand = new Random();
        //Creating snake
        ArrayList<Snake> snake = new ArrayList<Snake>(){{
            add(new Snake(10,10));
            add(new Snake(10,11));
            add(new Snake(10,17));}};
        //Placing out som healthy and some crazy food
        Food food = new Food(rand.nextInt(GAME_WIDTH), rand.nextInt(GAME_LENGTH));
        Food crazyFood = new Food(rand.nextInt(GAME_WIDTH), rand.nextInt(GAME_LENGTH));
        while(food.x == crazyFood.x && food.y == crazyFood.y){
            crazyFood = new Food(rand.nextInt(GAME_WIDTH), rand.nextInt(GAME_LENGTH));
        }
        Main main = new Main();
        //THE GAME STARTS RUNNING
        main.gameLoop(snake, food, crazyFood, terminal);
        highScore(score, playerName, file);
        System.out.println("YOU LOOSE!!!\nYour score: " + score);

    }

    public boolean gameLoop(ArrayList<Snake> snake, Food food, Food crazyFood, Terminal terminal) throws IOException, InterruptedException {
        direction = "up";
        while(snakeLife(snake)){
            updateScreen(snake, food, crazyFood, terminal);
            Thread.sleep(100);
            direction = snakeDirection(direction, terminal);
            snakeMovement(snake, direction);
            //Putting out new healthy food if eaten
            if(eaten(snake, food)) {
                food = foodPlacing(food, snake);
                snake = snakeGrow(snake);
                score += 1;
            }
            //Putting out new crazy food if eaten and running some crazyfood scenarios
            if(eaten(snake, crazyFood)) {
                int i = 0;
                int j = 0;
                int crazyBehaviour = kindOfCrazySnake();
                ArrayList<Snake> snakeUp = new ArrayList<Snake>();
                ArrayList<Snake> snakeDown = new ArrayList<Snake>();
                for (long stop = System.nanoTime()+ TimeUnit.SECONDS.toNanos(5); stop>System.nanoTime();) {
                    switch(crazyBehaviour){
                        case 0:
                            direction = snakeDirection(direction, terminal);
                            Thread.sleep(33);
                            j = i/3;
                            i++;
                            snakeMovement(snake, direction);
                            updateCrazyScreen(snake, terminal, j);
                            if(!snakeLife(snake)){
                                return false;
                            }
                            break;
                        case 1:
                            direction = snakeCrazyDirection(direction, terminal);
                            Thread.sleep(100);
                            j++;
                            snakeMovement(snake, direction);
                            updateCrazyScreen(snake, terminal, j);
                            if(!snakeLife(snake)){
                                return false;
                            }
                            break;
                        case 2:

                            if(snakeDown.size() != 0 && snakeUp.size() != 0){
                                snakeMovement(snakeUp, direction);
                                snakeMovement(snakeDown, direction);
                            }
                            direction = snakeDirection(direction, terminal);
                            updateScreenSlicedSnake(snakeUp, snakeDown, terminal, j);
                            if (j == 0){
                                for(int k = 0; k < snake.size()/2; k++){
                                    snakeUp.add(snake.get(k));
                                }
                                for(int k = snake.size()/2; k < snake.size(); k++ ){
                                    snakeDown.add(snake.get(k));
                                }
                                snakeMovement(snakeUp, direction);
                                snakeMovement(snakeDown, direction);
                                updateScreenSlicedSnake(snakeUp, snakeDown, terminal, j);
                            }
                            Thread.sleep(100);
                            if(!doubleSnakeLife(snakeUp, snakeDown)){
                                return false;
                            }
                            j++;
                            break;
                    }
                }
                crazyFood = foodPlacing(crazyFood, snake);
                snake = snakeGrow(snake);
                snake = snakeGrow(snake);
                score += 2;
            }
        }
        return false;
    }

    //Reads key input and sets a direction
    public static String snakeDirection(String direction, Terminal terminal) throws InterruptedException {
        Key key;
        key = terminal.readInput();
        if (key != null){
            switch(key.getKind()) {
                case ArrowUp:
                    if(direction == "down") {
                        direction = "down";
                    } else{
                        direction = "up";
                    }break;
                case ArrowDown:
                    if(direction == "up") {
                        direction = "up";
                    } else{
                        direction = "down";
                    }break;
                case ArrowLeft:
                    if(direction == "right") {
                        direction = "right";
                    }else{
                        direction = "left";
                    }break;
                case ArrowRight:
                    if(direction == "left") {
                        direction = "left";
                    }else{
                        direction = "right";
                    }break;
            }
        }
        return direction;
    }
    //Reads key input and sets a crazy direction
    public static String snakeCrazyDirection(String direction, Terminal terminal) throws InterruptedException {
        Key key;
        key = terminal.readInput();
        if (key != null){
            switch(key.getKind()) {
                case ArrowUp:
                    if(direction == "up") {
                        direction = "up";
                    } else{
                        direction = "down";
                    }break;
                case ArrowDown:
                    if(direction == "down") {
                        direction = "down";
                    } else{
                        direction = "up";
                    }break;
                case ArrowLeft:
                    if(direction == "left") {
                        direction = "left";
                    }else{
                        direction = "right";
                    }break;
                case ArrowRight:
                    if(direction == "right") {
                        direction = "right";
                    }else{
                        direction = "left";
                    }break;
            }
        }
        return direction;
    }
    //Update snake object with new coordinates for each new lap
    public static ArrayList<Snake> snakeMovement(ArrayList<Snake> snake, String direction) {
        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i-1).x;
            snake.get(i).y = snake.get(i-1).y;
        }
        switch(direction) {
            case "up":
                if(snake.get(0).y == 0) {
                    snake.get(0).y = snake.get(0).y + GAME_LENGTH;
                }else{
                    snake.get(0).y = snake.get(0).y - 1;
                }
                break;
            case "down":
                if(snake.get(0).y == GAME_LENGTH) {
                    snake.get(0).y = snake.get(0).y - GAME_LENGTH;
                }else{
                    snake.get(0).y = snake.get(0).y + 1;
                }
                break;
            case "left":
                if(snake.get(0).x == 0) {
                    snake.get(0).x = snake.get(0).x + GAME_WIDTH;
                }else{
                    snake.get(0).x = snake.get(0).x - 1;
                }
                break;
            case "right":
                if(snake.get(0).x == GAME_WIDTH) {
                    snake.get(0).x = snake.get(0).x - GAME_WIDTH;
                }else{
                    snake.get(0).x = snake.get(0).x + 1;
                }
                break;
        }
        return snake;
    }
    //Prints the snake and food on terminal
    public static void updateScreen(ArrayList<Snake> snake, Food food, Food crazyFood, Terminal terminal) {
        terminal.clearScreen();
        for(Snake snakeBit : snake) {
            terminal.moveCursor(snakeBit.x, snakeBit.y);
            terminal.putCharacter('O');
        }
        terminal.moveCursor(food.x, food.y);
        terminal.putCharacter('@');
        terminal.moveCursor(crazyFood.x, crazyFood.y);
        terminal.putCharacter('?');
        terminal.moveCursor(0,0);

    }

    //Prints the snake while eaten crazyFood
    public static void updateCrazyScreen(ArrayList<Snake> snake, Terminal terminal, int i) {
        terminal.clearScreen();
        for(Snake snakeBit : snake) {
            terminal.moveCursor(snakeBit.x, snakeBit.y);
            terminal.putCharacter('O');
        }
        countDownCrazyMode(i, terminal);
        terminal.moveCursor(0,0);
    }

    //Prints the snake if double while eating crazyFood
    public static void updateScreenSlicedSnake(ArrayList<Snake> snakeUp, ArrayList<Snake> snakeDown, Terminal terminal, int i) {
        terminal.clearScreen();
        for(Snake snakeBit : snakeUp) {
            terminal.moveCursor(snakeBit.x, snakeBit.y);
            terminal.putCharacter('O');
        }
        for(Snake snakeBit : snakeDown) {
            terminal.moveCursor(snakeBit.x, snakeBit.y);
            terminal.putCharacter('O');
        }
        countDownCrazyMode(i, terminal);
        terminal.moveCursor(0,0);
    }

    //Checks if snake has crashed
    public static boolean snakeLife(ArrayList<Snake> snake) {
        for(int i = 1; i < snake.size(); i++) {
            if(snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y){
                System.out.println("Det var snakelife");
                return false;
            }
        }return true;
    }
    //Checks if doubleSnake has crashed
    public static boolean doubleSnakeLife(ArrayList<Snake> snakeUp, ArrayList<Snake> snakeDown) {
        for(int i = 1; i < snakeUp.size(); i++) {
            if((snakeUp.get(0).x == snakeUp.get(i).x && snakeUp.get(0).y == snakeUp.get(i).y) ||
                    (snakeDown.get(0).x == snakeUp.get(i).x && snakeDown.get(0).y == snakeUp.get(i).y)){

                System.out.println("det var doubleSnakeLife snakeUp");

                return false;
            }
        }
        for(int i = 1; i < snakeDown.size(); i++) {
            if((snakeDown.get(0).x == snakeDown.get(i).x && snakeDown.get(0).y == snakeDown.get(i).y) ||
                    (snakeUp.get(0).x == snakeDown.get(i).x && snakeUp.get(0).y == snakeDown.get(i).y)){
                System.out.println("det var doubleSnakeLife snakeDown");

                return false;
            }
        }
        return true;
    }


    //Gives the snake a random crazy behaviour
    public static int kindOfCrazySnake(){
        Random rand = new Random();
        return 2;

                //rand.nextInt(3);
    }
    //Places food on the board
    public static Food foodPlacing(Food food, List<Snake> snake){
        Random rand = new Random();
        food.x = rand.nextInt(GAME_WIDTH);
        food.y = rand.nextInt(GAME_LENGTH);
        for(Snake snakeBits : snake) {
            while(snakeBits.x == food.x) {
                food.x = rand.nextInt(GAME_WIDTH);
            }
            while(snakeBits.y == food.y) {
                food.y = rand.nextInt(GAME_LENGTH);
            }
        }return food;
    }

    //Checks if snake eats food
    public static boolean eaten(List<Snake> snake, Food food) {
        boolean eaten = false;
        if(snake.get(0).y == food.y && snake.get(0).x == food.x) {
            eaten = true;
        }return eaten;
    }

    //Make the snake grow by one given it has eaten
    public static ArrayList<Snake> snakeGrow(ArrayList<Snake> snake){
        int i = snake.size();
        snake.add(new Snake(snake.get(i-1).x, snake.get(i-1).y));
        return snake;
    }
    //Saves highscore
    public static void highScore(int score, String playerName, File file) throws IOException {
        BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
        output.newLine();
        output.append(playerName + " " + score);
        output.close();
    }

    //Calls each countdown print
    public static void countDownCrazyMode(int i, Terminal terminal){
        if(i <= 9){
            putFive(terminal);
        }
        else if(i <= 18){
            putFour(terminal);
        }
        else if(i <= 27){
            putThree(terminal);
        }
        else if(i <= 36){
            putTwo(terminal);
        }
        else if(i <= 45){
            putOne(terminal);
        }
        if( i>= 46){
            putZero(terminal);
        }
    }

    //methods for printing countdown crazyMode in terminal
    private static void putZero(Terminal terminal) {
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,3);
        terminal.putCharacter('\u2620');
    }

    private static void putOne(Terminal terminal) {
        terminal.moveCursor(2,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,7);
        terminal.putCharacter('\u2620');
    }

    private static void putTwo(Terminal terminal) {
        terminal.moveCursor(2,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,7);
        terminal.putCharacter('\u2620');

    }

    private static void putThree(Terminal terminal) {
        terminal.moveCursor(2,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(5,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,7);
        terminal.putCharacter('\u2620');
    }

    private static void putFour(Terminal terminal) {
        terminal.moveCursor(2,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,7);
        terminal.putCharacter('\u2620');
    }

    private static void putFive(Terminal terminal) {
        terminal.moveCursor(2,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,3);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,2);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,4);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,5);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(4,6);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(3,7);
        terminal.putCharacter('\u2620');
        terminal.moveCursor(2,7);
        terminal.putCharacter('\u2620');
    }
}
