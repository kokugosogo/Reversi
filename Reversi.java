// Reversi kokugosogo
//=====================================================================================
// メモ
// コンピューターの戦略の変更はReversiClassのReversi()内のplayer[]の初期化3つ目の要素を0,1,2に指定することで実装しました
//
// コンパイルコマンド : javac -encoding UTF-8 Reversi.java
// 実行コマンド : java Reversi
//=====================================================================================

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class Stone {
    public final static int black = 1; // 黒 = 1
    public final static int white = 2; // 白 = 2
    private int obverse; // 1:表(黒),2:裏(白)

    Stone() {
        obverse = 0; // 初期値として黒でも白でもない
    }

    void setObverse(int color) {
        if (color == black || color == white) {
            obverse = color;
        } else {
            System.out.println("Error");
        }
    }

    void paint(Graphics g, Point p, int rad) {
        if (obverse == black) {
            g.setColor(Color.black);
            g.fillOval(p.x, p.y, rad, rad);
        } else if (obverse == white) {
            g.setColor(Color.white);
            g.fillOval(p.x, p.y, rad, rad);
        }
    }

    int getObverse() {
        return this.obverse;
    }

    void doReverse() {
        if (this.obverse == 1) {
            this.obverse = 2;
        } else if (this.obverse == 2) {
            this.obverse = 1;
        } else {
            System.exit(0);
        }
    }
}

class Board {
    public static final int FIELD_SIZE = 8; // 縦横のマス数
    private Stone[][] stone = new Stone[FIELD_SIZE][FIELD_SIZE];
    private Point[] direction = new Point[8];
    public int num_grid_black;
    public int num_grid_white;
    public int[][] eval_black = new int[FIELD_SIZE][FIELD_SIZE];
    public int[][] eval_white = new int[FIELD_SIZE][FIELD_SIZE];

    Board() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                stone[i][j] = new Stone();
            }
        }
        // 中心4つの石の初期化
        stone[3][3].setObverse(1);
        stone[4][4].setObverse(1);
        stone[3][4].setObverse(2);
        stone[4][3].setObverse(2);
        // 方向ベクトル
        direction[0] = new Point(1, 0);
        direction[1] = new Point(1, 1);
        direction[2] = new Point(0, 1);
        direction[3] = new Point(-1, 1);
        direction[4] = new Point(-1, 0);
        direction[5] = new Point(-1, -1);
        direction[6] = new Point(0, -1);
        direction[7] = new Point(1, -1);

        // =============================================
        // デバッグ用
        // for (int i = 0; i < FIELD_SIZE; i++) {
        // for (int j = 0; j < FIELD_SIZE - 1; j++) {
        // if (i % 2 == 0) {
        // stone[i][j].setObverse(1);
        // } else {
        // stone[i][j].setObverse(2);
        // }
        // }
        // }
        // =============================================
    }

    void setStone(int x, int y, int s) {
        if (countReverseStone(x, y, s) > 0) {
            stone[x][y].setObverse(s);
        }
    }

    void paint(Graphics g, int unit_size) {
        // 背景
        g.setColor(Color.black);
        g.fillRect(0, 0, unit_size * Reversi.WINDOW_DIV, unit_size * Reversi.WINDOW_DIV);
        // 盤面
        g.setColor(new Color(0, 85, 0));
        g.fillRect(unit_size, unit_size, unit_size * FIELD_SIZE, unit_size * FIELD_SIZE);
        // 横線
        g.setColor(Color.black);
        for (int i = 0; i < 9; i++) {
            g.drawLine(unit_size, unit_size * i, unit_size * (FIELD_SIZE + 1), unit_size * i);
        }
        // 縦線
        for (int i = 0; i < 9; i++) {
            g.drawLine(unit_size * i, unit_size, unit_size * i, unit_size * (FIELD_SIZE + 1));
        }
        // 目印
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                g.fillRect(unit_size * (4 * i + 3) - (unit_size / 10), unit_size * (4 * j + 3) - (unit_size / 10),
                        unit_size / 5, unit_size / 5);
            }
        }
        // 石の描画
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                Point p = new Point(unit_size * (i + 1) + (unit_size / 10), unit_size * (j + 1) + (unit_size / 10));
                stone[i][j].paint(g, p, unit_size * 8 / 10);
            }
        }
        evaluateBoard();
    }

    // 盤面(x,y)から方向dに向かって石を順に取得
    ArrayList<Integer> getLine(int x, int y, Point d) {
        ArrayList<Integer> line = new ArrayList<Integer>();
        int cx = x + d.x;
        int cy = y + d.y;
        while (isOnBoard(cx, cy) && stone[cx][cy].getObverse() != 0) {
            line.add(stone[cx][cy].getObverse());
            cx += d.x;
            cy += d.y;
        }
        return line;
    }

    void setStoneAndReverse(int x, int y, int s) {
        if (countReverseStone(x, y, s) > 0 && stone[x][y].getObverse() == 0) {
            stone[x][y].setObverse(s);
            for (int i = 0; i < direction.length; i++) {
                ArrayList<Integer> line = new ArrayList<Integer>();
                line = getLine(x, y, direction[i]);
                int n = 0;
                while (n < line.size()) {
                    if (line.get(n) == s) {
                        int cx = x + direction[i].x;
                        int cy = y + direction[i].y;
                        for (int j = 0; j < n; j++) {
                            stone[cx][cy].doReverse();
                            cx += direction[i].x;
                            cy += direction[i].y;
                        }
                        break;
                    }
                    n++;
                }
            }
        }
    }

    // 盤面(x,y)に石sを置いた場合に反転できる石の数を数える
    int countReverseStone(int x, int y, int s) {
        // 既に石が置かれていたら置けない
        if (stone[x][y].getObverse() != 0) {
            return -1;
        }
        int cnt = 0;
        for (int i = 0; i < 8; i++) {
            ArrayList<Integer> line = new ArrayList<Integer>();
            line = getLine(x, y, direction[i]);
            int n = 0;
            while (n < line.size() && line.get(n) != s) {
                n++;
            }
            if (1 <= n && n < line.size()) {
                cnt += n;
            }
        }
        return cnt;
    }

    void evaluateBoard() {
        this.num_grid_black = 0;
        this.num_grid_white = 0;
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                eval_black[i][j] = countReverseStone(i, j, Stone.black);
                if (eval_black[i][j] > 0) {
                    this.num_grid_black++;
                }
                eval_white[i][j] = countReverseStone(i, j, Stone.white);
                if (eval_white[i][j] > 0) {
                    this.num_grid_white++;
                }
            }
        }
    }

    void printBoard() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.printf("%2d", stone[j][i].getObverse());
            }
            System.out.println("");
        }
    }

    void printEval() {
        System.out.println("Black(1) : ");
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.printf("%2d", eval_black[j][i]);
            }
            System.out.println("");
        }
        System.out.println("White(2) : ");
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.printf("%2d", eval_white[j][i]);
            }
            System.out.println("");
        }
    }

    boolean isOnBoard(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            return false;
        } else {
            return true;
        }
    }

    int countStone(int s) {
        int cnt = 0;
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                if (stone[i][j].getObverse() == s) {
                    cnt++;
                }
            }
        }
        return cnt;
    }
}

class Player {
    public final static int type_human = 1;
    public final static int type_computer = 2;
    private int color;
    private int type;
    private int tactics_type; // 0:配置できるマスからランダム 1:最も裏返せるマス 2:1の戦術で盤面も考慮する

    Player(int c, int t, int tac_type) {
        if (c == Stone.black || c == Stone.white) {
            color = c;
        } else {
            System.out.println("Error : color");
            System.exit(0);
        }
        if (t == type_human || t == type_computer) {
            type = t;
        } else {
            System.out.println("Error : player_type");
            System.exit(0);
        }
        if (-1 < tac_type && tac_type < 5) {
            tactics_type = tac_type;
        } else {
            tac_type = -1;
        }
    }

    int getColor() {
        return color;
    }

    int getType() {
        return type;
    }

    int getTacticsType() {
        return tactics_type;
    }

    Point tactics(Board bd) {
        if (tactics_type == 0) {
            ArrayList<Point> can_put_idx = new ArrayList<Point>();
            if (color == Stone.black) {
                for (int i = 0; i < bd.eval_black.length; i++) {
                    for (int j = 0; j < bd.eval_black.length; j++) {
                        if (bd.eval_black[i][j] > 0) {
                            can_put_idx.add(new Point(i, j));
                        }
                    }
                }
            } else if (color == Stone.white) {
                for (int i = 0; i < bd.eval_white.length; i++) {
                    for (int j = 0; j < bd.eval_white.length; j++) {
                        if (bd.eval_white[i][j] > 0) {
                            can_put_idx.add(new Point(i, j));
                        }
                    }
                }
            }
            Random random = new Random();
            int rand = random.nextInt(can_put_idx.size());
            return (can_put_idx.get(rand));
        } else if (tactics_type == 1) {
            int eval_max = 0;
            Point p = new Point();
            if (color == Stone.black) {
                for (int i = 0; i < bd.eval_black.length; i++) {
                    for (int j = 0; j < bd.eval_black.length; j++) {
                        if (bd.eval_black[i][j] > eval_max) {
                            eval_max = bd.eval_black[i][j];
                            p = new Point(i, j);
                        }
                    }
                }
            } else if (color == Stone.white) {
                for (int i = 0; i < bd.eval_white.length; i++) {
                    for (int j = 0; j < bd.eval_white.length; j++) {
                        if (bd.eval_white[i][j] > eval_max) {
                            eval_max = bd.eval_white[i][j];
                            p = new Point(i, j);
                        }
                    }
                }
            }
            return p;
        } else if (tactics_type == 2) {
            int eval_max = 0;
            int eval_edge_max = 0;
            Point p = new Point();
            Point p_opt = new Point(-1, -1);
            if (color == Stone.black) {
                for (int i = 0; i < bd.eval_black.length; i++) {
                    for (int j = 0; j < bd.eval_black.length; j++) {
                        if (bd.eval_black[i][j] > 0) {
                            if (i == 0 || i == 7) {
                                if (j == 0 || j == 7) {
                                    return (new Point(i, j));
                                } else {
                                    if (bd.eval_black[i][j] > eval_edge_max) {
                                        eval_edge_max = bd.eval_black[i][j];
                                        p_opt = new Point(i, j);
                                    }
                                }
                            } else {
                                if (j == 0 || j == 7) {
                                    if (bd.eval_black[i][j] > eval_edge_max) {
                                        eval_edge_max = bd.eval_black[i][j];
                                        p_opt = new Point(i, j);
                                    }
                                }
                            }
                            if (bd.eval_black[i][j] > eval_max) {
                                eval_max = bd.eval_black[i][j];
                                p = new Point(i, j);
                            }
                        }
                    }
                }
            } else if (color == Stone.white) {
                for (int i = 0; i < bd.eval_white.length; i++) {
                    for (int j = 0; j < bd.eval_white.length; j++) {
                        if (bd.eval_white[i][j] > 0) {
                            if (i == 0 || i == 7) {
                                if (j == 0 || j == 7) {
                                    return (new Point(i, j));
                                } else {
                                    if (bd.eval_white[i][j] > eval_edge_max) {
                                        eval_edge_max = bd.eval_white[i][j];
                                        p_opt = new Point(i, j);
                                    }
                                }
                            } else {
                                if (j == 0 || j == 7) {
                                    if (bd.eval_white[i][j] > eval_edge_max) {
                                        eval_edge_max = bd.eval_white[i][j];
                                        p_opt = new Point(i, j);
                                    }
                                }
                            }
                            if (bd.eval_white[i][j] > eval_max) {
                                eval_max = bd.eval_white[i][j];
                                p = new Point(i, j);
                            }
                        }
                    }
                }
            }
            if (p_opt.x >= 0 && p_opt.y >= 0) {
                return p_opt;
            } else {
                return p;
            }
        } else if (tactics_type == 3) {
            int eval_min = 100;
            Point p = new Point();
            if (color == Stone.black) {
                for (int i = 0; i < bd.eval_black.length; i++) {
                    for (int j = 0; j < bd.eval_black.length; j++) {
                        if (bd.eval_black[i][j] < eval_min && bd.eval_black[i][j] > 0) {
                            eval_min = bd.eval_black[i][j];
                            p = new Point(i, j);
                        }
                    }
                }
            } else if (color == Stone.white) {
                for (int i = 0; i < bd.eval_white.length; i++) {
                    for (int j = 0; j < bd.eval_white.length; j++) {
                        if (bd.eval_white[i][j] < eval_min && bd.eval_white[i][j] > 0) {
                            eval_min = bd.eval_white[i][j];
                            p = new Point(i, j);
                        }
                    }
                }
            }
            return p;
        }
        return (new Point(-1, -1)); // 配置不可能な場合
    }

    Point nextMove(Board bd, Point p) {
        if (type == type_human) {
            return p;
        } else if (type == type_computer) {
            return tactics(bd);
        }
        return (new Point(-1, -1)); // 通常はありえない
    }

}

public class Reversi extends JPanel {
    public static final int WINDOW_DIV = 10; // windowの縦横分割数
    public static final int UNIT_SIZE = 80; // マス目の縦横の長さ
    private Board board = new Board();
    private int turn; // 手番 1:Stone.black 2:Stone.white
    private Player[] player = new Player[2];
    public static boolean is_update = true;

    public Reversi() {
        setPreferredSize(new Dimension(UNIT_SIZE * 10, UNIT_SIZE * 10));
        addMouseListener(new MouseProc());
        player[0] = new Player(Stone.black, Player.type_human, -1);
        player[1] = new Player(Stone.white, Player.type_computer, 3);
        turn = Stone.black;
    }

    public void paintComponent(Graphics g) {
        board.paint(g, UNIT_SIZE);
        g.setColor(Color.white);
        if (turn == Stone.black) {
            g.drawString("プレイヤー(黒)の番です.", UNIT_SIZE / 2, UNIT_SIZE / 2);
        } else if (turn == Stone.white && player[turn - 1].getType() == Player.type_computer) {
            g.drawString("コンピューター(白)の番です. ", UNIT_SIZE / 2, UNIT_SIZE / 2);
        } else {
            g.drawString("プレイヤー(白)の番です.", UNIT_SIZE / 2, UNIT_SIZE / 2);
        }
        g.drawString("[黒:" + board.countStone(Stone.black) + ", 白:" + board.countStone(Stone.white) + "]",
                UNIT_SIZE / 2, 19 * UNIT_SIZE / 2);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new Reversi());
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    void changeTurn() {
        Graphics g = getGraphics();
        board.evaluateBoard();
        int cnt_black = 0;
        int cnt_white = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.eval_black[i][j] > 0) { // 黒の置ける数を数える
                    cnt_black++;
                }
                if (board.eval_white[i][j] > 0) { // 白の置ける数を数える
                    cnt_white++;
                }
            }
        }
        if (player[turn - 1].getType() == Player.type_human) {
            if (player[turn - 1].getColor() == Stone.black && cnt_black > 0) {
                turn = Stone.white;
            } else if (player[turn - 1].getColor() == Stone.white && cnt_white > 0) {
                turn = Stone.black;
            } else {
                MessageDialog("あなたはパスです");
                is_update = false;
            }
        } else if (player[turn - 1].getType() == Player.type_computer) {
            if (player[turn - 1].getColor() == Stone.black && cnt_white > 0) {
                turn = Stone.white;
            } else if (player[turn - 1].getColor() == Stone.white && cnt_black > 0) {
                turn = Stone.black;
            } else {
                MessageDialog("computerはパスです");
                if (turn == Stone.black && cnt_white > 0) {
                    turn = Stone.white;
                } else if (turn == Stone.white && cnt_black > 0) {
                    turn = Stone.black;
                }
                is_update = false;
            }
        } else {
            MessageDialog("Error!");
            is_update = false;
        }
    }

    void MessageDialog(String str) {
        JOptionPane.showMessageDialog(this, str, "情報", JOptionPane.INFORMATION_MESSAGE);
        is_update = false;
    }

    void ErrorMessageDialog() {
        String str = "盤面内をクリックしてください";
        JOptionPane.showMessageDialog(this, str, "メッセージ", JOptionPane.INFORMATION_MESSAGE);
        is_update = false;
    }

    void EndMessageDialog() {
        int cnt_black = 0;
        int cnt_white = 0;
        cnt_black = board.countStone(Stone.black);
        cnt_white = board.countStone(Stone.white);
        String str = "[黒 :" + cnt_black + ", 白 :" + cnt_white + "]";
        if (cnt_black > cnt_white) {
            JOptionPane.showMessageDialog(this, str + "で黒の勝利", "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        } else if (cnt_black < cnt_white) {
            JOptionPane.showMessageDialog(this, str + "で白の勝利", "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, str + "で引き分け", "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        }
        is_update = false;
        System.exit(0);
    }

    class MouseProc extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            Point point = me.getPoint();
            int btn = me.getButton();
            int x = (point.x / UNIT_SIZE) - 1;
            int y = (point.y / UNIT_SIZE) - 1;
            // 範囲外をクリックしたらエラーを出す
            if (!board.isOnBoard(x, y)) {
                ErrorMessageDialog();
                return;
            }
            removeMouseListener(this);
            if (player[turn - 1].getType() == Player.type_human) {
                int cnt_black = 0;
                cnt_black = board.countStone(player[turn - 1].getColor());
                board.setStoneAndReverse(x, y, player[turn - 1].getColor());
                if (cnt_black < board.countStone(player[turn - 1].getColor())) { // 上の行でsetできていたら
                    board.evaluateBoard();
                    changeTurn();
                    repaint();
                    if (board.num_grid_black <= 0 && board.num_grid_white <= 0) {
                        is_update = false;
                        EndMessageDialog();
                    }
                    if (player[turn - 1].getType() == Player.type_human) {
                        addMouseListener(this);
                    }
                    if (player[turn - 1].getType() == Player.type_computer) {
                        Thread th = new TacticsThread();
                        th.start();
                    }
                } else {
                    addMouseListener(new MouseProc());
                }
            } else {
                board.evaluateBoard();
                // System.out.println(turn);
                if (board.num_grid_black <= 0 && board.num_grid_white <= 0) {
                    is_update = false;
                    EndMessageDialog();
                }
                if (player[turn - 1].getType() == Player.type_computer) {
                    Thread th = new TacticsThread();
                    th.start();
                    repaint();
                }
            }
            board.evaluateBoard();
            // board.printBoard();
            // board.printEval();
        }
    }

    class TacticsThread extends Thread {
        public void run() {
            try {
                Thread.sleep(1000);
                // System.out.println(board.num_grid_black + " , " + board.num_grid_white);
                if (board.num_grid_black == 0 && board.num_grid_white == 0) {
                    EndMessageDialog();
                }
                Point nm = player[turn - 1].nextMove(board, new Point(-1, -1));
                if (nm.x == -1 && nm.y == -1) {
                    MessageDialog("相手はパスです");
                    Reversi.is_update = false;
                } else {
                    board.setStoneAndReverse(nm.x, nm.y, player[turn - 1].getColor());
                }
                board.evaluateBoard();
                changeTurn();
                repaint();
                addMouseListener(new MouseProc());
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}