package com.lsh.crazycat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by LSH on 2016/7/28.
 */
public class Playground extends SurfaceView implements OnTouchListener {

    //每行、列存储10个元素
    private static int WIDTH = 40;
    private static final int ROW = 10;
    private static final int COL = 10;
    //默认添加的路障
    private static final int BLOCKS = 15;


    private Dot[][] matrix;
    private Dot cat;


    public Playground(Context context) {
        super(context);
        getHolder().addCallback(callback);
        //初始化二维数组
        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {//逐行
            for (int j = 0; j < COL; j++) {//逐列
                matrix[i][j] = new Dot(j, i);
            }
        }
        //触摸监听
        setOnTouchListener(this);
        //调用游戏
        initGame();
    }

    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        //判断是否处于游戏的边界
        if (d.getX() * d.getY() == 0
                || d.getX() + 1 == COL
                || d.getY() + 1 == ROW) {
            return true;
        }
        return false;
    }

    //判断附近的方法
    private Dot getNeighbour(Dot one, int dir) {
        switch (dir) {

            case 1:
                return getDot(one.getX() - 1, one.getY());

            case 2:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX()-1, one.getY() - 1);
                } else {
                    return getDot(one.getX(), one.getY() - 1);
                }
            case 3:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX(), one.getY() - 1);
                } else {
                    return getDot(one.getX() + 1, one.getY() - 1);
                }
            case 4:
                return getDot(one.getX() + 1, one.getY());
            case 5:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX(), one.getY() + 1);
                } else {
                    return getDot(one.getX() + 1, one.getY() + 1);
                }

            case 6:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX() - 1, one.getY() + 1);
                } else {
                    return getDot(one.getX(), one.getY() + 1);
                }
            default:
                break;
        }
        return null;
    }

    private int getDistance(Dot one, int dir) {
        int distance = 0;
        if (isAtEdge(one)) {
            return 1;
        }

        Dot ori = one, next;
        while (true) {
            next = getNeighbour(ori, dir);
            if (next.getStatus() == Dot.STATUS_ON) {
                return distance * -1;
            }
            if (isAtEdge(next)) {
                distance++;
                return distance;
            }

            distance++;
            ori = next;
        }
    }

    private void MoveTo(Dot one) {
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(), one.getY());
    }
    
    private void move()
    {
        if (isAtEdge(cat)) {
            lose();
            return;
        }

        Vector<Dot> avaliable=new Vector<Dot>();
        Vector<Dot> positive=new Vector<Dot>();
        HashMap<Dot,Integer> al=new HashMap<Dot,Integer>();

        for (int i = 1; i <7 ; i++) {
            Dot n=getNeighbour(cat,i);
            if (n.getStatus()==Dot.STATUS_OFF) {
                avaliable.add(n);
                al.put(n,i);
                if (getDistance(n,i)>0) {
                    positive.add(n);
                }
            }
        }
        if (avaliable.size()==0) {
            win();
        }else if (avaliable.size()==1){
            MoveTo(avaliable.get(0));
        }else{
            Dot best=null;
            if (positive.size()!=0) {
                int min=999;
                for (int i = 0; i <positive.size() ; i++) {
                    int a=getDistance(positive.get(i),al.get(positive.get(i)));
                    if (a<min) {
                        min=a;
                        best=positive.get(i);
                    }
                }
                MoveTo(best);
            }else {
                int max=0;
                for (int i = 0; i <avaliable.size(); i++) {
                    int k=getDistance(avaliable.get(i),al.get(avaliable.get(i)));

                    if (k <= max) {
                        max=k;
                        best=avaliable.get(i);
                    }
                }
                MoveTo(best);
            }
        }
    }

    private void win() {
        Toast.makeText(getContext(), "WIN", Toast.LENGTH_SHORT).show();
    }

    private void lose() {
        Toast.makeText(getContext(), "LOSE", Toast.LENGTH_SHORT).show();
    }


    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            int offset = 0;
            if (i % 2 != 0) {
                offset = WIDTH / 2;
            }
            for (int j = 0; j < COL; j++) {
                Dot one = getDot(j, i);
                //位置的三种状态
                switch (one.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;
                    default:
                        break;
                }
                c.drawOval(new RectF(one.getX() * WIDTH + offset, one.getY() * WIDTH,
                        (one.getX() + 1) * WIDTH + offset, (one.getY() + 1) * WIDTH), paint);
            }
        }

        getHolder().unlockCanvasAndPost(c);
    }




     Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

            WIDTH = arg2 / (COL + 1);
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };


    //游戏的初始化
    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        //初始化猫的位置
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);

        //随机的坐标
        for (int i = 0; i < BLOCKS; ) {
            int x = (int) ((Math.random() * 1000) % COL);
            int y = (int) ((Math.random() * 1000) % ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
                Log.i("TAG", "BlockS" + i);
            }
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent e) {

        if (e.getAction() == MotionEvent.ACTION_UP) {
            //Toast.makeText(getContext(), e.getX()+":"+e.getY(), Toast.LENGTH_SHORT).show();

            int x, y;
            y = (int) (e.getY() / WIDTH);
            if (y % 2 == 0) {
                x = (int) (e.getX() / WIDTH);
            } else {
                x = (int) ((e.getX() - WIDTH / 2) / WIDTH);
            }
            //如果点击的图标超过边界
            if (x + 1 > COL || y + 1 > ROW) {
                initGame();
            } else if (getDot(x,y).getStatus()==Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
            }
            redraw();
        }
        return true;
    }
}
