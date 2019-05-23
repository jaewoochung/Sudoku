package com.example.sudoku;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;


public class MainActivity extends Activity {

    GridView gridView;


    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;

    private Stack<TwoPoints> stack;     // This stack is responsible for handling redo's

    private int[] numTracker = new int[9];   // Array keeps track of how many of each number are on the board

    private TextView revealer;
    private TextView timerTextView;
    private Integer count;
    private Integer minutes;

    private static int w=9, curx, cury;
    private static int curPos;
    private Random r = new Random();
    static String[] tiles = new String[w*w];            /* Tiles is the grid array 9 by 9 but it is a 1D array */
    static boolean[] validity = new boolean[w*w];       /* This array keeps track of which numbers can be edited */
    private boolean boardFilled;
    private static int MAX_COORDINATE = w * w - 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView1);
        timerTextView = (TextView) findViewById(R.id.timer);
        revealer = (TextView) findViewById(R.id.reveal);

        count = 0;      // Initialize the count for timer
        minutes = 0;

        stack = new Stack<TwoPoints>();     // Initialize the stack

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, tiles);

        gridView.setAdapter(adapter);       /* Setup gridview with array adapter */

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);

        init();     // Call initializer
        gridView.setVisibility(View.INVISIBLE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (v.getTag() != null && v.getTag().equals("ExampleName")) {
                    v.setBackgroundColor(Color.WHITE);
                    v.setTag("");
                } else {
                    v.setTag("Example name");
                    v.setBackgroundColor(Color.LTGRAY);
                    int Nums = gridView.getChildCount();
                    for (int i = 0; i <= Nums-1; i++) {
                        if (i != position) {
                            View child = gridView.getChildAt(i);
                            child.setBackgroundColor(Color.WHITE);
                        }
                    }
                }
                updatePos(position);    // Update the global variable and highlight tile
            }
        });
    }

    //**************HANDLER****************/
    public Handler threadHandler = new Handler(){
        public void handleMessage (android.os.Message message){
            String format = "%1$02d";
            timerTextView.setText(String.format(format, minutes) + ":" + String.format(format, count));
        }

    };

    //*************RUNNABLE **************/
    private Runnable countNumbers = new Runnable () {
        private static final int DELAY = 1000;
        public void run() {
            try {
                while (true) {
                    count++;
                    if (count == 60) {
                        minutes++;
                        count = 0;
                    }
                    Thread.sleep (DELAY);
                    threadHandler.sendEmptyMessage(0);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    /* Initialize the gridview with empty values */
    void init(){
        for (int i = 0; i < 9; i++) {
            numTracker[i] = 9;
        }
        for(int i=0;i<tiles.length;i++) {
            tiles[i]="";
            validity[i]=false;
        }
        boardFilled = false;

        generateSudoku(0);
        removeNums();
    }

    /* resets Board */
    public void resetBoard(View view) {
        for (int i = 0; i < tiles.length; i++) {
            ((TextView)gridView.getChildAt(i)).setTextColor(Color.BLACK);
        }

        init();
        count = 0;
        minutes = 0;
        revealer.setText("");

        timerTextView.setVisibility(View.VISIBLE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        button3.setVisibility(View.VISIBLE);
        button4.setVisibility(View.VISIBLE);
        button5.setVisibility(View.VISIBLE);
        button6.setVisibility(View.VISIBLE);
        button7.setVisibility(View.VISIBLE);
        button8.setVisibility(View.VISIBLE);
        button9.setVisibility(View.VISIBLE);
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();

        stack.empty();
        start(view);
    }

    // Fills the rest of the board
    protected void generateSudoku(int coord) {
        HashSet<Integer> checked = new HashSet<Integer>();
        int n;

        if (coord > MAX_COORDINATE)
            boardFilled = true;
        else if(!tiles[coord].equals("")) {
            generateSudoku(coord + 1);
            return;
        } else {
            while (!boardFilled && checked.size() < 9) {
                while (checked.contains(n = (r.nextInt(9) + 1))) ;

                checked.add(n);

                tiles[coord] = "";

                if (tryCell(coord, n))
                    generateSudoku(coord + 1);
            }
            if(!boardFilled) tiles[coord] = "";
        }
    }

    protected boolean tryCell(int coord, int n) {
        if (checkRow(coord, n) && checkCol(coord, n) &&
                checkSubgrid(coord, n)) {
            tiles[coord] = "" + n;

            return true;
        }

        return false;
    }

    /*
     * Returns false if there is a same number in row
     */
    protected boolean checkRow(int coord, int n) {
        String digit = "" + n;
        int x = coord % 9;
        int y = coord / 9;

        for (int i = 1; i < 9; i++)
            if (tiles[y * w + ((x + i) % 9)].equals(digit))
                return false;

        return true;
    }

    /*
     * Returns false if there is a same number in col
     */
    protected boolean checkCol(int coord, int n) {
        String digit = "" + n;
        int x = coord % 9;
        int y = coord / 9;

        for (int i = 1; i < 9; i++)
            if (tiles[((y + i) % 9) * w + x].equals(digit))
                return false;

        return true;
    }

    /*
     * Returns false if there is a same number in subgrid
     */
    protected boolean checkSubgrid(int coord, int n) {
        String digit = "" + n;
        int x = coord % 9;
        int y = coord / 9;

        for (int i = 0; i < 3; i++) {
            int tempX;

            if (x >= 6)
                tempX = x + ((x + i > 8) ? 8 - (x + i) : i);
            else if (x >= 3)
                tempX = x + ((x + i > 5) ? 5 - (x + i) : i);
            else
                tempX = x + ((x + i > 2) ? 2 - (x + i) : i);

            for(int j = 0; j < 3; j++) {
                int tempY;
                if (y >= 6)
                    tempY = y + ((y + j > 8) ? 8 - (y + j) : j);
                else if (y >= 3)
                    tempY = y + ((y + j > 5) ? 5 - (y + j) : j);
                else
                    tempY = y + ((y + j > 2) ? 2 - (y + j) : j);

                if (tiles[tempY * w + tempX].equals(digit))
                    return false;
            }
        }

        return true;
    }

    public void removeNums() {
        int count = 38;     // Remove 38 tiles -- leave 43 tiles
        while (count != 0) {
            int tilePosition = r.nextInt(81);
            if (!tiles[tilePosition].equals("")) {      // if a num is present, remove it
                // grab that string convert to integer
                // as you remove it, decrement the count of that number in correct position
                int num = Integer.parseInt(tiles[tilePosition]); // Convert string into num for indexing
                numTracker[num-1]--;
                count--;
                tiles[tilePosition] = "";
            }
        }

    }

    /*
     * Display puzzle is solved, stop the clock
     */
     public void conclude () {
        // Display how long it took to solve,
        String format = "%1$02d";
        revealer.setText("Finished in " + String.format(format, minutes) + ":" + String.format(format, count) + "!");
        timerTextView.setVisibility(View.INVISIBLE);

    }

    /*
     * This method allows you insert values into the given position
     * Ex: 80 % 9 == 8 (8th column)
     *     80 / 9 == 8 (8th row)
     */
    public void updatePos(int position) {
        curPos = position;       // Gives you the current tile number
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }

    public void clearBox(View view) {
        if (!validity[curPos]) {
            tiles[curPos] = "";
            /* Make a copy and push it on the stack */
            TwoPoints twoPoints = new TwoPoints(curPos, "");
            stack.push(twoPoints);
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }



    public void solver(View view) {
        // Solves current board
        boardFilled = false;
        /*
         * Color in new values solved by the board
         */
        for (int i = 0; i < tiles.length; i++) {
            if (!validity[i]) {  /* if not an initial num */
                tiles[i] = "";
                ((TextView)gridView.getChildAt(i)).setTextColor(Color.BLUE);
                if (i < 9) {
                    numTracker[i] = 9;
                }

            }
        }
        revealer.setText("Puzzle Solved");
        timerTextView.setVisibility(View.INVISIBLE);
        generateSudoku(0);
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }


    boolean boardCheck() {
        for (int i = 0; i < tiles.length; i++) {
            int curTile = -1;       // Dummy Variable
            if (tiles[i].equals("")) {
                curTile = 0;
            }
            else {
                curTile = Integer.parseInt(tiles[i]);
            }
            if (checkRow(i, curTile) && checkCol(i, curTile)
                    && checkSubgrid(i, curTile)) {
                return false;
            }
        }
        return true;
    }
    // Checks if it user filled board is correct or not
    public void submit(View view) {
        String correct = "Solved Correctly!";
        String wrong = "Solved Incorrectly";
        if (boardCheck()) {
            revealer.setText(correct);
        } else {
            revealer.setText(wrong);
        }
        timerTextView.setText("00:00");

    }

    // Starts the game
    public void start(View view) {
        for (int i = 0; i < 81; i++) {
            if (!tiles[i].equals("")) { // Finds the initial values before game starts
                validity[i] = true;     // This position is non-editable
            }
        }
        gridView.setVisibility(View.VISIBLE);
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();

        Thread thread = new Thread (countNumbers);      // Thread starts for counter
        thread.start();
    }

    /* If button is clicked, update gridview to previous state */
    public void redo(View view) {
        if (!stack.isEmpty()) {
            TwoPoints tp = stack.pop();
            int pos = tp.firstPoint();         // Retrieves the pos of the previous state
            String value = tp.secondPoint();   // Retrieves the string value of the previous state
            tiles[pos] = value;                // Display previous state
        }
        else {  // End of stack
            Toast.makeText(this, "Can't undo anymore", Toast.LENGTH_SHORT).show();
        }
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }


    /* Place the clicked number (one) in current position */
    public void number1(View view) {
        /* Checks to see if the curPos is not in a pre-defined number */
        if (!validity[curPos]) {
            /* Colors the text RED if incorrect number is written */
            if (!checkSubgrid(curPos, 1) || !checkCol(curPos, 1) || !checkRow(curPos, 1)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            } else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[0]++;
            }
            if (numTracker[0] == 9) {
                button1.setVisibility(View.INVISIBLE);
            }

            /* hold a copy in stack (used for redo) */
            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "1";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }

            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();   // update the board
        }

    }

    public void number2(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 2) || !checkCol(curPos, 2) || !checkRow(curPos, 2)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[1]++;
            }
            if (numTracker[1] == 9) {
                button2.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "2";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }


            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number3(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 3) || !checkCol(curPos, 3) || !checkRow(curPos, 3)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[2]++;
            }
            if (numTracker[2] == 9) {
                button3.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "3";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }


            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number4(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 4) || !checkCol(curPos, 4) || !checkRow(curPos, 4)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[3]++;
            }
            if (numTracker[3] == 9) {
                button4.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "4";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }


            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number5(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 5) || !checkCol(curPos, 5) || !checkRow(curPos, 5)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[4]++;
            }
            if (numTracker[4] == 9) {
                button5.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "5";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }


            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number6(View view) {
        if (!validity[curPos]) {    //  Checks if box is in non-initialized tile
            if (!checkSubgrid(curPos, 6) || !checkCol(curPos, 6) || !checkRow(curPos, 6)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {  /* Valid entry */
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[5]++;

            }
            if (numTracker[5] == 9) {
                button6.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "6";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }


            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number7(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 7) || !checkCol(curPos, 7) || !checkRow(curPos, 7)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[6]++;
            }
            if (numTracker[6] == 9) {
                button7.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "7";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }

            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number8(View view) {
        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 8) || !checkCol(curPos, 8) || !checkRow(curPos, 8)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                numTracker[7]++;
            }
            if (numTracker[7] == 9) {
                button8.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "8";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }

            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number9(View view) {

        if (!validity[curPos]) {
            if (!checkSubgrid(curPos, 9) || !checkCol(curPos, 9) || !checkRow(curPos, 9)) {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.RED);
            }
            else {
                ((TextView)gridView.getChildAt(curPos)).setTextColor(Color.BLUE);
                int x = numTracker[8];
                x++;
                numTracker[8] = x;
                //numTracker[8]++;
            }
            if (numTracker[8] == 9) {   // Hides button once 9 numbers have been inputted in
                button9.setVisibility(View.INVISIBLE);
            }

            TwoPoints twoPoints;
            if (!tiles[curPos].equals("")) {     /* push # if number is in tile position */
                twoPoints = new TwoPoints(curPos, tiles[curPos]);
            } else {
                twoPoints = new TwoPoints(curPos, "");
            }
            stack.push(twoPoints);
            tiles[curPos] = "9";

            /* check if board is complete, if so finish game */
            if (numTracker[0] == 9 && numTracker[1] == 9 && numTracker[2] == 9 && numTracker[3] == 9 &&
                    numTracker[4] == 9 && numTracker[5] == 9 && numTracker[6] == 9 && numTracker[7] == 9 &&
                    numTracker[8] == 9) {
                // game is complete
                conclude();
            }
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }
}
