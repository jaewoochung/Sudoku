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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Random;

/*
 * Things to implement
 * Starting Numbers are uneditable
 * Starting Numbers have different color
 * Borders for each subgrid
 *
*/

public class MainActivity extends Activity {

    GridView gridView;

    private boolean[] uneditable = new boolean[30];

    private ImageView borderImageView;

    private TextView revealer;
    private TextView timerTextView;
    private Integer count;
    private Integer minutes;
    private long startTime = 0;

    private static int w=9, curx, cury;
    private static int curPos;
    private Random r = new Random();
    static String[] tiles = new String[w*w];            /* Tiles is the grid array 9 by 9 but it is a 1D array */
    static boolean[] validity = new boolean[w*w];       /* This array keeps track of which numbers can be edited */
    private boolean boardFilled;
    private static int MAX_COORDINATE = w * w - 1;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView1);
        timerTextView = (TextView) findViewById(R.id.timer);
        revealer = (TextView) findViewById(R.id.reveal);

        count = 0;      // Initialize the count for timer
        minutes = 0;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, tiles);

        gridView.setAdapter(adapter);       /* Setup gridview with array adapter */

        //TextView tv = (TextView) adapter.getView(1, null, gridView);

//        LayoutInflater layoutInflater;
//        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        borderImageView = (ImageView) layoutInflater.inflate(R.layout.border_image, null);
//        borderImageView.setScaleX((float) .3);
//        borderImageView.setScaleY((float) .3);
//        gridView.setBackgroundColor(Color.TRANSPARENT);

        init();     // Call initializer
//        generateSudoku(0);
//        boardFilled = false;
//        removeNums();

        // Create a thread and start it
//        Thread thread = new Thread (countNumbers);
//        thread.start();

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
                updatePos(position);        // Update the global variable and highlight tile
            }
        });
        Log.d("Before for loop", "check sttment");

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
        for(int i=0;i<tiles.length;i++) {
            tiles[i]="";
            validity[i]=false;
        }
        boardFilled = false;

        generateSudoku(0);
        removeNums();
        //((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }

    /* resets Board */
    public void resetBoard(View view) {
//        for(int i=0;i<tiles.length;i++) tiles[i]="";
//        boardFilled = false;
//        generateSudoku(0);
//        removeNums();
        init();
        count = 0;
        minutes = 0;
        String clearText = "";
        revealer.setText(clearText);

        //((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
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
        int count = 51;     // Remove 51 leave 30 random boxees
        while (count != 0) {
            int tilePosition = r.nextInt(81);
            if (!tiles[tilePosition].equals("")) {
                count--;
                tiles[tilePosition] = "";
            }
        }

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
        if (validity[curPos] == false) {
            tiles[curPos] = "";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    /* Place the clicked number (one) in current position */
    public void number1(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "1";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }

    }

    public void number2(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "2";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number3(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "3";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number4(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "4";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number5(View view) {
//        ((TextView)gridView.getChildAt(4)).setTextColor(Color.DKGRAY);
        if (validity[curPos] == false) {
            tiles[curPos] = "5";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number6(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "6";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number7(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "7";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number8(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "8";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void number9(View view) {
        if (validity[curPos] == false) {
            tiles[curPos] = "9";
            ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        }
    }

    public void solver(View view) {
        // Solves current board
        boardFilled = false;
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
            if (!tiles[i].equals("")) {
                Log.d("Entered", "Pos at: " + Integer.toString(i));
                ((TextView)gridView.getChildAt(i)).setTextColor(Color.RED);
                validity[i] = true;
            }
        }
//        gridView.getChildAt(0).setBackground();
        gridView.setVisibility(View.VISIBLE);
        ((ArrayAdapter)gridView.getAdapter()).notifyDataSetChanged();
        Thread thread = new Thread (countNumbers);
        thread.start();
    }
}
