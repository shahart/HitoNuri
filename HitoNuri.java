import palmos.*;
import palmos.Event;
import palmos.Rectangle;
import waba.io.*;
import waba.sys.Vm;

// TODO undo one step

//format of the Memo
//
//example
//
// 0 -2 3
// o 1 3
// # 1 2
// n1
//
//means nurikabe, game id = 1
//
//hitori
//
// 1-9
// minus1-9 = black
//
//nurikabe
//
// 1-9
// 0 = space
// o = 'o'
// # = black
// you can use \n to denote empty row.
//


class HitoNuri {
    static final int idfMain = 1000;
    static final int idfAbout = 1100;
    static final int idfError = 1101;

    static final int idcRestart = 1010;
    static final int idcNextGame = 1011;
    static final int idcGameType = 1012;
    static final int idcSolve = 1013;

    static final int idcAbout = 1019;

    int GAME_ID = -1;

    final int MAX_SIZE = 20;

    int GRID_SIZE = 5;
    int CELLSIZE;

    boolean isHitori = true;

    int Board[][] = new int[MAX_SIZE][];
    int solution[][] = new int[MAX_SIZE][];

    int prevX = -1;
    int prevY = -1;

    Rectangle r = new Rectangle();

//    public static void main(String s[]) {
//        new HitoNuri().run();
//    }

    public static int PilotMain(int cmd, int cmdBPB, int launchFlags) {
        if (cmd == 0) {
            new HitoNuri().run();
        }
        return 0;
    }

    public HitoNuri() {
        Board = new int[MAX_SIZE][];
        for (int i = 0; i < MAX_SIZE; i++)
            Board[i] = new int[MAX_SIZE];

        copyBoards = new int[MAX_SIZE][];
        for (int i = 0; i < MAX_SIZE; i++)
            copyBoards[i] = new int[MAX_SIZE];

        solution = new int[MAX_SIZE][];
        for (int i = 0; i < MAX_SIZE; i++)
            solution[i] = new int[MAX_SIZE];


    }

    public void run() {
        Palm.FrmGotoForm(idfMain);
        Event e = new Event();
        ShortHolder err = new ShortHolder((short) 0);
        while (e.eType != Event.appStopEvent) {
            Palm.EvtGetEvent(e, -1);
            if (!Palm.SysHandleEvent(e)) {
                if (!Palm.MenuHandleEvent(0, e, err)) {
                    if (!appHandleEvent(e)) {
                        Palm.FrmHandleEvent(Palm.FrmGetActiveForm(), e);
                    }
                }
            }
        }
        write(toMemo());
        Palm.FrmCloseAllForms();
    }

    private void redraw() {
        for (int i = 0; i < GRID_SIZE; i++)
            for (int j = 0; j < GRID_SIZE; j++)
                drawCell(i , j );
        Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(0,255, 0))); //GREEN
        Palm.WinDrawChars("id",2, 149,10);
        Palm.WinDrawChars(Integer.toString(GAME_ID+1), Integer.toString(GAME_ID+1).length(), 149, 20);
        Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.WHITE));

    }

    boolean appHandleEvent(Event e) {
        if (e.eType == Event.frmLoadEvent) {
            int form = Palm.FrmInitForm(e.formID());
            Palm.FrmSetActiveForm(form);
            return true;
        } else if (e.eType == Event.frmOpenEvent) {
            init();
            String s = read();
            if (s.length() > HEADER.length()) {
                fromMemo(s.substring(1), Board);
                fromMemo(getSolution(), solution); // TODO BUG- doesn't handle new boards without solutions, as we reset GAME_ID to 0
            }
            else {
                fromMemo(getGame(), Board);
                fromMemo(getSolution(), solution);
            }
            Palm.FrmDrawForm(Palm.FrmGetActiveForm());
            int ControlPtr = Palm.FrmGetObjectPtr(Palm.FrmGetActiveForm(),
                    Palm.FrmGetObjectIndex(Palm.FrmGetActiveForm(), idcGameType));
            Palm.CtlSetLabel(ControlPtr, ! isHitori? "Nurikabe": " Hitori ");
            redraw();

            return true;
        } else if (e.eType == Event.penDownEvent || e.eType == Event.penMoveEvent) {
            if (e.screenX == prevX && e.screenY == prevY)
                return true;
            prevX = e.screenX;
            prevY = e.screenY;

            int x = e.screenX / CELLSIZE + 1;
            int y = e.screenY / CELLSIZE + 1;
            if (x >= 1 && x <= GRID_SIZE && y >= 1 && y <= GRID_SIZE) {
                x--;
                y--;
                if (isHitori && validHitori(x , y )) {
                    Board[y ][x ] *= -1;
                    redraw(); // TODO redraw only diff cells
                } else if (!isHitori  && (Board[y ][x ] == 0 || Board[y ][x ] >= 100)) {
                    if (Board[y ][x ] == 100) {
                        Board[y ][x ] = 101; // black
                        boolean validNuriBlack = true;
                        if (x>0 && y>0) {
                            if (Board[y-1][x-1] == 101 && Board[y-1][x] == 101 && Board[y][x-1] == 101 && Board[y][x] == 101)
                                validNuriBlack = false;
                        }
                        if (x<GRID_SIZE && y<GRID_SIZE) {
                            if (Board[y][x] == 101 && Board[y+1][x] == 101 && Board[y][x+1] == 101 && Board[y+1][x+1] == 101)
                                validNuriBlack = false;
                        }
                        if (x>=0 && y>0) {
                            if (Board[y-1][x] == 101 && Board[y-1][x+1] == 101 && Board[y][x] == 101 && Board[y][x+1] == 101)
                                validNuriBlack = false;
                        }
                        if (x>0 && y>=0) {
                            if (Board[y][x-1] == 101 && Board[y][x] == 101 && Board[y+1][x-1] == 101 && Board[y+1][x] == 101)
                                validNuriBlack = false;
                        }

                        if (! validNuriBlack) {
                            r.topLeft_x = (short) ((x ) * CELLSIZE + 2);
                            r.topLeft_y = (short) ((y ) * CELLSIZE + 2);
                            Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255, 0, 0))); //RED
                            for (int i = 0; i < 4; i++) {
                                Palm.WinDrawChars(i % 2 == 0 ? "X  " : "   ", 3,
                                        r.topLeft_x - 2 + CELLSIZE / 2 - Palm.FntCharsWidth("0", 1) / 2,
                                        r.topLeft_y - 2 + CELLSIZE / 2 - Palm.FntCharHeight() / 2);
                                Vm.sleep(200);
                            }
                        }
                    }
                    else if (Board[y ][x ] == 101)
                        Board[y ][x ] = 0; // empty
                    else if (Board[y ][x ] == 0)
                        Board[y ][x ] = 100; // dot

                    redraw();
                }
                boolean solved = true;
                for (int i = 0; i < GRID_SIZE && solved; i++)
                    for (int j = 0; j < GRID_SIZE && solved; j++) {
                        int prev = Board[i][j];
                        if (Board[i][j] == 100)
                            Board[i][j] = 0;
                        if (Board[i][j] != solution[i][j])
                            solved = false;
                        Board[i][j] = prev;
                    }
                Palm.WinSetTextColor(Palm.WinRGBToIndex(solved ? new RGBColor(255, 0,0): RGBColor.BLACK)); //RED
                Palm.WinDrawChars(solved ? ":)" : ":(",2, 120,146);
                Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.WHITE));
                return true;
            }
        } else if (e.eType == Event.ctlSelectEvent) {
            if (e.controlID() == idcRestart) {
                for (int i = 0; i < GRID_SIZE; i++)
                    for (int j = 0; j < GRID_SIZE; j++) {
                        Board[i][j] = Math.abs(Board[i][j]);
                        if (Board[i][j] >= 100)
                            Board[i][j] = 0;
                    }
                redraw();
                Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.BLACK));
                Palm.WinDrawChars( ":(" ,2, 120,146);
                return true;
            } else if (e.controlID() == idcAbout) {
                Palm.FrmAlert(idfAbout);
            } else if (e.controlID() == idcSolve) {
                for (int i = 0; i < GRID_SIZE; i++)
                    for (int j = 0; j < GRID_SIZE; j++)
                        Board[i][j] = solution[i][j];
                redraw();
                Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255, 0,0)));
                Palm.WinDrawChars( ":)" ,2, 120,146);
                Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.WHITE));
                return true;
            } else if (e.controlID() == idcNextGame) {
                newGame();
                return true;
            } else if (e.controlID() == idcGameType) {
                int ControlPtr = Palm.FrmGetObjectPtr(Palm.FrmGetActiveForm(),
                        Palm.FrmGetObjectIndex(Palm.FrmGetActiveForm(), idcGameType));
                Palm.CtlSetLabel(ControlPtr, isHitori? "Nurikabe": " Hitori ");
                isHitori = ! isHitori;
                GAME_ID=-1;
                newGame();
                return true;
            }

        }
        if (GAME_ID >= 0) {
            Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255,215,0))); // gold
            Palm.WinDrawChars("N",1, 147, 65);
            Palm.WinDrawChars("O",1, 147, 75);
            Palm.WinDrawChars("W",1, 146, 85);
            waba.sys.Time t = new waba.sys.Time();
            String helper = Integer.toString(t.hour);
            Palm.WinDrawChars(helper, helper.length(), helper.length() == 1 ? 147 : 145, 105);
            Palm.WinDrawChars(":", 1, 149, 115);
            helper = Integer.toString(t.minute);
            if (helper.length() == 1)
                helper = "0" + helper;
            Palm.WinDrawChars(helper, helper.length(), 145, 125);
            Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.WHITE));
        }
        
        return false;
    }

    private void newGame() {
        GAME_ID++;

        Palm.FrmEraseForm(Palm.FrmGetActiveForm());
        Palm.FrmDrawForm(Palm.FrmGetActiveForm());

        fromMemo(getGame(), Board);
        fromMemo(getSolution(), solution);

        redraw();

    }

    private String getSolution() {
        if (isHitori)
        switch (GAME_ID) {
            // hito
            case 0: return "-1 1 -1 6 -5 \n1 6 2 4 5 \n2 -2 5 -2 4 \n6 4 -2 1 -6 \n-1 5 1 3 2 \nh0"; // 5x5_01.pdf
            case 1: return "2 5 -5 4 -5 \n-2 4 1 3 6 \n3 -3 4 2 -6 \n5 2 -1 1 3 \n-2 6 2 5 1 \nh1"; // 5x5_04.pdf
            case 2: return "-1 3 -3 1 -3 2 \n1 6 7 2 3 4 \n5 4 -7 3 -5 6 \n-1 1 3 4 6 5 \n2 5 -2 6 -3 1 \n-1 2 1 5 4 7 \nh2"; // 6x6_01.pdf
            case 3: return "-3 3 -6 1 -3 4 \n3 2 6 7 1 5 \n-1 1 -1 3 -1 2 \n1 6 3 5 4 -1 \n4 -4 1 -1 3 6 \n-3 7 5 6 2 3 \nh3"; // 6x6_03
            case 4: return "-5 2 -1 1 6 5 3 \n5 8 1 4 -6 2 -5 \n1 -1 3 -1 8 4 2 \n-4 4 -4 8 -4 3 -3 \n2 5 8 3 1 7 4\n-5 1 -1 5 -6 8 7 \n4 7 5 2 3 1 6 \nh4"; // 7x7_01
            case 5: return "-3 2 -2 5 -7 4 -8\n3 5 2 6 7 1 8\n-6 6 -6 7 -6 5 -6\n5 4 7 2 8 3 6\n6 -6 5 -6 1 -6 7\n-3 7 -2 8 -7 6 1\n1 3 6 4 2 8 5\nh5";//02
            case 6: return "-7 7 -7 2 -7 6 -7 3\n4 6 2 5 9 7 3 8\n-4 5 9 -2 6 -6 1 -3\n1 2 7 8 5 3 4 9\n-8 8 -2 3 -9 9 -3 1\n6 4 8 1 7 2 9 5\n-4 3 -3 9 -3 8 -3 4\n5 9 4 7 3 1 2 6\nh6";//8x8
        }
        else
        switch (GAME_ID) {
            // nuri
            case 0: return "6 0 0 * 1 \n0 0 0 * * \n* * * * \n0 4 * 0 4 \n0 0 * * 0 \nn0"; // 5
            case 1:return "* * * * 0\n0 * 0 * 3\n2 * 0 * 0\n* * 3 * *\n0 2 * * 1\nn1"; 
            case 2: return "* 3 0 0 * 1 *\n * * * * * * *\n2 0 * 1 * 0 0\n* * * * * * 0\n* 1 * 0 2 * 0\n* * 2 * * * 0\n1 * 0 * 1 * 6\nn2"; //7
            case 3: return "5 * 1 * 0 0 0\n0 * * * * 0 0\n0 0 0 * 1 * 6\n* * * * * * *\n2* 4 0 0 * 0\n0 * 0 * * * 0\n* * * 0 2 * 3\nn3";
            case 4:return "* * 4 0 0 0 * 5\n* 1 * * * * * 0\n* * 2 0 * 0 * 0\n0 * * * * 2 * 0\n0 0 4 * 0 * * 0\n* * * * 0 3 * * \n0 * 0 * * * 1 *\n2 * 0 0 0 5 * *\nn4"; // 8 easy
            case 5: return "0 2 * 0 0 0 0 0\n* * * * * * * 6\n* 3 0 * 0 3 * *\n1 * 0 * 0 * 0 0\n* * * * * * * 3\n* 0 2 * 0 0 3 *\n2 * * * * * * *\n0 * 0 0 0 0 6 0\nn5";
            case 6: return "0 5 * * 3 0 * 0 0 \n0 * 8 * 0 * * 5 \n0 * 0 * * * 2 * \n0 * 0 0 0 * 0 * * \n* * * * 0 * * * 0 \n* 0 0 * 0 * 0 * 0 \n* * 3 * 0 * 0 * 0 \n* 3 * * * * 3 *\n* 0 0 * 1 * * 6 \nn6"; // 9
            case 7: return "3 0 0 * * * * * *\n* * * * 0 * 0 0 *\n1 * 0 * 2 * * 3 *\n* * 2 * * 2 * * 0\n0 3 * 0 * 0 * 3 0\n0 * * 2 * * 2 * * \n* 3 * * 1 * 0 * 1\n* 0 0 * * * * * *\n* * * * 0 0 0 0 5\nn7";
            case 8: return "0 0 0 0 * * * * * * \n5 * * * 5 0 0 0 * 2 \n* 3 * 4 * * * 0 * 0 \n* 0 * 0 0 0 * * * * \n* 0 * * * * 1 * 1 * \n* * * 2 0 * * * * 3 \n3 * 1 * * * 0 0 * 0 \n0 0 * 0 0 3 * 3 * \n* * * * * * * * * * \n4 0 0 0 * 0 0 0 0 5 \nn8";//10
            case 9: return "0 2 * 0 0 0 0 8 * *\n* * * 0 * * * * 5 *\n0 * 0 0 * 0 0 0 0 *\n0 * * * * * * * * *\n3 * 0 0 0 * 0 0 4 *\n* 5 0 * * * 0 * * 3\n* * * * 0 * * * 0 0\n* 0 0 0 0 * 0 * * *\n* 6 * * * * 0 * 0 *\n* * 7 0 0 0 0 * 2 *\nn9";
        }
        GAME_ID = 0;
        return getSolution();
	}

    private String getGame() {
        String s = getSolution();
        s = s.replace('-', ' ');
        s= s.replace('o', '0');
        s= s.replace('*', '0');
        s= s.replace('X', '0');
        return s;
	}

    int sum;
    int max;
    int copyBoards[][] = new int[MAX_SIZE][];

    private void drawCell(int x, int y) {
        r.topLeft_x = (short) ((x) * CELLSIZE + 2);
        r.topLeft_y = (short) ((y) * CELLSIZE + 2);
        if (Board[y ][x ] < 0 || Board[y ][x ] == 101) {
            Palm.WinSetForeColor(Palm.WinRGBToIndex(new RGBColor(0,0,255)));//RGBColor.BLACK));
        } else {
            Palm.WinSetForeColor(Palm.WinRGBToIndex(RGBColor.WHITE));
            // Palm.WinEraseRectangle(r, CELLSIZE/4);
        }
        Palm.WinDrawRectangle(r, CELLSIZE / 4);

        Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.BLACK));

        if (isHitori && Board[y ][x ] > 0) {
            boolean dup = false;

            for (int i = 0; i < GRID_SIZE && ! dup; i++) {
                if (y  != i)
                    if (Board[i][x ] > 0 && Board[i][x ] == Board[y ][x ])
                        dup = true;
                if (x  != i)
                    if (Board[y ][i] > 0 && Board[y ][i] == Board[y ][x ])
                        dup = true;
            }

            if (dup)
                Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255, 0, 0))); //RED

        }

        String s = Integer.toString(Math.abs(Board[y ][x ]));
        if (!isHitori) {
            if (Board[y][x ] == 0)
                s = ".";
            else if (Board[y ][x ] == 101)
                s = "";
            else if (Board[y ][x ] == 100)
                s = "o";
            else {
                for (int i=0; i<GRID_SIZE; i++)
                    for (int j=0; j<GRID_SIZE; j++)
                        copyBoards[i][j] = Board[i][j];
                sum = 0;
                max =  Board[y][x];
                validNuri(x,y);
                if (sum != Board[y][x]) {
                    Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255, 0, 0))); //RED
                }

            }
        }

        if (s.length() > 0)
            Palm.WinDrawChars(s, 1,
                    r.topLeft_x - 2 + CELLSIZE / 2 - Palm.FntCharsWidth("0", 1) / 2,
                    r.topLeft_y - 2 + CELLSIZE / 2 - Palm.FntCharHeight() / 2);

        Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.BLACK));
    }

    private boolean validHitori(int x, int y) {
        boolean valid = true;
        if (x > 0) {
            if (Board[y][x] > 0 && Board[y][x - 1] < 0)
                valid = false;
        }
        if (x < GRID_SIZE - 1) {
            if (Board[y][x] > 0 && Board[y][x + 1] < 0)
                valid = false;
        }
        if (y > 0) {
            if (Board[y][x] > 0 && Board[y - 1][x] < 0)
                valid = false;
        }
        if (y < GRID_SIZE - 1) {
            if (Board[y][x] > 0 && Board[y + 1][x] < 0)
                valid = false;
        }
        if (!valid) {
            r.topLeft_x = (short) ((x ) * CELLSIZE + 2);
            r.topLeft_y = (short) ((y ) * CELLSIZE + 2);
            Palm.WinSetTextColor(Palm.WinRGBToIndex(new RGBColor(255, 0, 0))); //RED
            for (int i = 0; i < 4; i++) {
                Palm.WinDrawChars(i % 2 == 0 ? "X  " : "   ", 3,
                        r.topLeft_x - 2 + CELLSIZE / 2 - Palm.FntCharsWidth("0", 1) / 2,
                        r.topLeft_y - 2 + CELLSIZE / 2 - Palm.FntCharHeight() / 2);
                Vm.sleep(200);
            }
            Palm.WinSetTextColor(Palm.WinRGBToIndex(RGBColor.BLACK));
            drawCell(x, y);

        }
        return valid;
    }

    private void validNuri(int x, int y) {
        if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE || sum > max)
            return;
        
        if (copyBoards[y][x] < 101 && copyBoards[y][x] >= 0) {
            sum++;
            copyBoards[y][x] = -1;
            validNuri(x-1,y);
            validNuri(x+1,y);
            validNuri(x,y-1);
            validNuri(x,y+1);
        }
    }

    ///// DOWNLOAD

    private void fromMemo(String s, int[][] Board) {
        int y = 0;
        int x = 0;
        int sign = 1;

        GRID_SIZE = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
            }
            else if (s.charAt(i) == '\n') {
                if (GRID_SIZE == 0)
                    GRID_SIZE = x;
                for (; x < GRID_SIZE; x++) // \n must be after the 1st line
                    Board[y][x] = 0;
                if (x > GRID_SIZE) {
                    Palm.FrmCustomAlert(idfError, "Too many elements at row " + (y+1) + " - Truncated.", "\nThe first row determines the puzzle size.", "");
                }
                x = 0;
                y++;
            } else if (s.charAt(i) == '-')
                sign = -1;
            else if (s.charAt(i) == 'o') {
                Board[y][x] = 100;
                x++;
            }
            else if (s.charAt(i) == '*' || s.charAt(i) == 'X' || s.charAt(i) == 'x') {
                Board[y][x] = 101;
                x++;
            }
            else if (s.charAt(i) == 'n' || s.charAt(i) == 'h' || s.charAt(i) == 'N' || s.charAt(i) == 'H') { // gameId
                isHitori = s.charAt(i) == 'h' || s.charAt(i) == 'H';
                i++;
                GAME_ID = 0;
                while (i < s.length()) {
                    GAME_ID = (GAME_ID*10) + s.charAt(i) - '0';
                    i++;
                }
                break;
            }
            else if (s.charAt(i) >= '0' && s.charAt(i) <= '9') { // 0-9
                Board[y][x] = s.charAt(i) - '0';
                Board[y][x] *= sign;
                sign = 1;
                x++;
            }
            else {
                Palm.FrmCustomAlert(idfError, "Invalid char '" + s.charAt(i) + "' at row " + (y+1) + ", column " + (x+1), "Changed to 0", "");
                Board[y][x] = 0;
                sign = 1;
            }
        }

        CELLSIZE = 140 / GRID_SIZE;

        r.extent_x = (short) (CELLSIZE - 2);
        r.extent_y = (short) (CELLSIZE - 2);

//        Palm.WinDrawChars(Integer.toString(GRID_SIZE),1, 10,10);
//        Vm.sleep(1000);

    }

    private String toMemo() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (isHitori)
                    s.append(Board[i][j]); // + " ";
                else if (Board[i][j] == 100)
                    s.append("o");// += "o ";
                else if (Board[i][j] == 101)
                    s.append("*");// += "* ";
                else
                    s.append(Board[i][j]);
                s.append( " ");
            }
            s.append("\n");
        }
        return s.toString() + (isHitori ? "h" : "n") + GAME_ID;
    }

    // thanks to meditor source code
    
    private int record;

    static final String HEADER = "hitoNuri";
    static final String CATALOG = "MemoDB.memo.DATA";

    void init() {
        record = -1;
        Catalog c = new Catalog(CATALOG, Catalog.READ_ONLY);
        if (!c.isOpen())
            return;
        byte b[] = new byte[HEADER.length()];
        for (int i = 0; i < c.getRecordCount(); i++) {
            c.setRecordPos(i);
            if (c.getRecordSize() < HEADER.length())
                continue;
            c.readBytes(b, 0, b.length);
            if (new String(b).equalsIgnoreCase(HEADER)) {
                record = i;
                break;
            }
        }
        c.close();
    }

    String read() {
        if (record < 0) {
            // tals
            Catalog c = new Catalog(CATALOG, Catalog.WRITE_ONLY);
            if (c.addRecord(HEADER.length()) < 0) {
                c.close();
                return "";
            }
            byte b[] = HEADER.getBytes();
            c.resizeRecord(b.length);
            c.writeBytes(b, 0, b.length);
            c.close();
            init();
            // tals end
            return "";
        }
        Catalog c = new Catalog(CATALOG, Catalog.READ_ONLY);
        if (!c.isOpen())
            return "";
        c.setRecordPos(record);
        byte b[] = new byte[c.getRecordSize()];
        c.readBytes(b, 0, b.length);
        c.close();
        return new String(b).substring(HEADER.length(), b.length - 1);
    }

    void write(String s) {
        if (record < 0)
            return;
        Catalog c = new Catalog(CATALOG, Catalog.WRITE_ONLY);
        if (!c.isOpen())
            return;
        c.setRecordPos(record);
        byte b[] = new String(HEADER + "\n" + s + "\0").getBytes();
        c.resizeRecord(b.length);
        c.writeBytes(b, 0, b.length);
        c.close();
    }


}
