package carmelo.examples.client.environment;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import carmelo.common.SpringContext;
//小球运动的可视界面，在登录服务器成功后启动
@Component
public class World extends JPanel {
	public static BufferedImage arrow_left, arrow_up, arrow_right, arrow_down; //箭头png20*20图
	//用四个值代表小球当前面朝的方向
	private static final int dLeft = 0;
	private static final int dUp = 1;
	private static final int dRight = 2;
	private static final int dDown = 3;
	private static final int roundTime = 60;  //一次游戏限时时间

	/**
	 * 一个二维地图世界
	 */
	private static int ballSize = 20;
	private int width, heigth;
	private int ballX, ballY;//小球坐标
	private int direction;
	private int startTime;//当前游戏回合开始时间
	private Integer runningStatus = 0;
	private JFrame frame;
	private JLabel lab = new JLabel("我是一个标签");

	private static final long serialVersionUID = -4230700025874459569L;

	static {
		try {
			arrow_left = ImageIO.read(World.class.getResource("/png/arrow_left.png"));
			arrow_up = ImageIO.read(World.class.getResource("/png/arrow_up.png"));
			arrow_right = ImageIO.read(World.class.getResource("/png/arrow_right.png"));
			arrow_down = ImageIO.read(World.class.getResource("/png/arrow_down.png"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public World() {
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public void init(int width, int heigth) {
		this.width = width;
		this.heigth = heigth;
		ballX = Math.abs(new Random().nextInt()) % (width - ballSize);
		ballY = Math.abs(new Random().nextInt()) % (heigth - ballSize);
		direction = Math.abs(new Random().nextInt() % 4);

		lab.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));// 创建蚀刻式边框
		frame.add(lab,BorderLayout.SOUTH) ;    // 将组件件入到面板之中
		setKeyListener();
		reset();
		this.setFocusable(true);
	}

	//检查是否出界
	private boolean isOutOfBorder(int x, int y) {
		return (x > width - ballSize || y > heigth - ballSize || x < 0 || y < 0) ? true : false;
	}

	//在paintComponent里实现图形界面，在调用repaint()时，将调用本函数重画游戏界面
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//画出小球
		//		g.fillOval(ballX, ballY, ballSize, ballSize);

		switch(direction) {
		case dUp:
			g.drawImage(arrow_up,ballX,ballY,null); //画箭头
			break;
		case dRight:
			g.drawImage(arrow_right,ballX,ballY,null); //画箭头
			break;
		case dDown:
			g.drawImage(arrow_down,ballX,ballY,null); //画箭头
			break;
		case dLeft:
			g.drawImage(arrow_left,ballX,ballY,null); //画箭头
			break;
		default:
			break;
		}
		//		lab.setText("direction:" + direction );

	}

	//根据键盘输入移动小球或显示/关闭路径提示，使用synchronized同步修饰
	synchronized public void move(int c) {
		int tx = ballX, ty = ballY;
		// System.out.println(c);
		switch (c) {
		case KeyEvent.VK_UP :
			switch(direction) {
			case dUp:
				ty = ty - ballSize;
				break;
			case dRight:
				tx = tx + ballSize;
				break;
			case dDown:
				ty = ty + ballSize;
				break;
			case dLeft:
				tx = tx - ballSize;
				break;
			default:
			}
			break;
		case KeyEvent.VK_DOWN :
			direction = direction + 1;
			if(direction > 3) direction = direction - 4;
			break;
		default :
		}
		//移动规则，要目标单元格在迷宫范围内
		if (!isOutOfBorder(tx, ty)) {
			ballX = tx;
			ballY = ty;
		}

		repaint();
	}

	//获取键盘事件，调用处理函数，重画迷宫，检查是否成功
	private void setKeyListener() {
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int c = e.getKeyCode();
				move(c);
			}
		});
	}


	//重置游戏，开始计时，在单独线程中运行
	//这里不能使用@Async，经测试，与继承类JPanel有冲突，会报错
	private void reset() {
		new Thread() {
			public void run() {
				while(true) {

					synchronized(runningStatus) {
						
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	
	public static void main(String[] args) throws Exception {
		
		//启动界面
		int width = 600;
		int heigth = 480;
		JFrame frame = new JFrame("我的世界");
		World world = (World)SpringContext.getBean(World.class);
		world.setFrame(frame);
		world.init(width, heigth);
		frame.getContentPane().add(world);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600 , 500 );
		frame.setLocation(200, 100);
		frame.setVisible(true);

	}

}
