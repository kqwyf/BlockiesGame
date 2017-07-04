import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.Timer;

class Blockies
{
	public static void main(String[] args)
	{
		MainFrame f=new MainFrame();
		f.setVisible(true);
		f.drawStartPage();
	}
}

class MainFrame extends JFrame
{
	private MainView view;
	MainFrame()
	{
		setTitle("Blockies");
		setSize(300,433);
		setResizable(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		view=new MainView(()->view.stop(),
						(Integer a)->setTitle("Time:"+a/10+"."+a%10+"s"));
		getContentPane().setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
	}
	public void drawStartPage()
	{
		view.drawStartPage(false);
	}
}

class MainView extends JPanel
{
	private static final int DIAMETER=20;
	private static final int ROUND=3;
	private MapData map;
	private Timer timer;
	private int counter;
	private boolean playing;
	private boolean dying;
	private Consumer<Integer> timeCounter;
	private int timeCount=0;

	private int height,width;
	MainView(Runnable stop,Consumer<Integer> timeCounter)
	{
		this(new MapData(stop),timeCounter);
	}
	MainView(MapData map,Consumer<Integer> timeCounter)
	{
		this.timeCounter=timeCounter;
		playing=false;
		dying=false;
		this.map=map;
		height=map.getHeight();
		width=map.getWidth();
		timer=new Timer(20,
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(playing)
					{
						if(counter%5==0) timeCounter.accept(++timeCount);
						if(counter>=50)
							counter=0;
						if(counter++==0)
							map.pushBlockies();
						if(counter%2==0) map.fall();
						paintMap();
					}
					else if(dying)
					{
						if(counter<height*2)
						{
							if(counter%2==0)
							{
								map.die(counter/2);
								paintMap();
							}
							counter++;
						}
						else
						{
							dying=false;
							timer.stop();
							drawStartPage(true);
						}
					}
				}
			});
		addMouseListener(
			new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if(playing)
						map.click(e.getY()/DIAMETER,e.getX()/DIAMETER);
					else if(!dying)
						start();
					paintMap();
				}
			});
		addMouseMotionListener(
			new MouseAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					if(playing)
					{
						map.update(e.getY()/DIAMETER,e.getX()/DIAMETER);
						paintMap();
					}
				}
			});
	}

	public void drawStartPage(boolean replay)
	{
		Graphics g=getGraphics();
		if(replay) g.setColor(Color.WHITE);
		g.setFont(new Font("Consolas",Font.PLAIN,50));
		g.drawString("Blockies",40,70);
		g.setFont(new Font("Times New Roman",Font.PLAIN,26));
		g.drawString("Want to try?",80,150);
		g.setFont(new Font("Times New Roman",Font.BOLD,26));
		g.drawString("Click to start!",70,300);
	}

	public void clearPage()
	{
		Graphics g=getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,500,500);
	}

	public void start()
	{
		clearPage();
		playing=true;
		counter=0;
		timeCount=0;
		timeCounter.accept(0);
		map.clear();
		timer.start();
	}

	public void stop()
	{
		playing=false;
		counter=0;
		dying=true;
	}

	private void paintMap()
	{
		Graphics g=getGraphics();
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				g.setColor(map.getColor(i,j));
				g.fillRoundRect(j*DIAMETER,i*DIAMETER,DIAMETER,DIAMETER,ROUND,ROUND);
			}
		g.setColor(Color.WHITE);
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
				if(map.isSelected(i,j))
				{
					g.drawRoundRect(j*DIAMETER,i*DIAMETER,DIAMETER,DIAMETER,ROUND,ROUND);
					g.fillOval(j*DIAMETER+DIAMETER/2-ROUND,i*DIAMETER+DIAMETER/2-ROUND,
							   ROUND*2,ROUND*2);
				}
	}
}

class MapData
{
	private static final int WIDTH=15;
	private static final int HEIGHT=20;
	private char[][] map;
	private boolean[][] selected;
	private int selectCount=0;
	private int height,width;
	private boolean[][] used;
	private Runnable stop;
	private Random rand=new Random();
	MapData(Runnable stop)
	{
		this(HEIGHT,WIDTH,stop);
	}
	MapData(int height,int width,Runnable stop)
	{
		this.height=height;
		this.width=width;
		this.stop=stop;
		map=new char[height][width];
		selected=new boolean[height][width];
		used=new boolean[height][width];
		clear();
	}

	public int getHeight()
	{return height;}

	public int getWidth()
	{return width;}

	public void update(int r,int c)
	{
		dfs(r,c);
	}

	public void die(int r)
	{
		for(int i=0;i<width;i++)
			map[r][i]='G';
	}

	public void fall()
	{
		for(int i=height-1;i>0;i--)
			for(int j=0;j<width;j++)
				if(map[i][j]=='w')
				{
					map[i][j]=map[i-1][j];
					map[i-1][j]='w';
				}
	}

	private static final char[] colors={'r','g','b','B','o'};
	public void pushBlockies()
	{
		for(int i=0;i<width;i++)
			if(map[0][i]!='w')
			{
				stop.run();
				return;
			}
		for(int i=0;i<width;i++)
			map[0][i]=colors[rand.nextInt(4)];
	}

	public void click(int r,int c)
	{
		dfs(r,c);
		if(selectCount>=3)
		{
			for(int i=0;i<height;i++)
				for(int j=0;j<width;j++)
					if(selected[i][j])
					{
						selected[i][j]=false;
						map[i][j]='w';
					}
			selectCount=0;
		}
	}

	public void clear()
	{
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				map[i][j]='w';
				selected[i][j]=false;
			}
	}

	public Color getColor(int r,int c)
	{
		if(!isvalid(r,c)) return Color.WHITE;
		switch(map[r][c])
		{
			case 'r':
				return Color.RED;
			case 'g':
				return Color.GREEN;
			case 'b':
				return Color.BLUE;
			case 'B':
				return Color.BLACK;
			case 'o':
				return Color.ORANGE;
			case 'w':
				return Color.WHITE;
			case 'G':
				return Color.GRAY;
			default:
				return Color.WHITE;
		}
	}

	public boolean isSelected(int r,int c)
	{
		if(!isvalid(r,c)) return false;
		return selected[r][c];
	}

	private static final int[] dfsa={-1,-1,-1, 0,0, 1,1,1};
	private static final int[] dfsb={-1, 0, 1,-1,1,-1,0,1};
	private void dfs(int r,int c)
	{
		if(!isvalid(r,c)) return;
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				selected[i][j]=false;
				used[i][j]=false;
			}
		selectCount=0;
		_dfs(r,c);
	}
	private void _dfs(int r,int c)
	{
		used[r][c]=true;
		if(map[r][c]=='w') return;
		selected[r][c]=true;
		selectCount++;
		for(int i=0;i<8;i++)
			if((isvalid(r+dfsa[i],c+dfsb[i])&&!used[r+dfsa[i]][c+dfsb[i]])&&map[r+dfsa[i]][c+dfsb[i]]==map[r][c])
				_dfs(r+dfsa[i],c+dfsb[i]);
	}
	private boolean isvalid(int r,int c)
	{return r>=0&&c>=0&&r<height&&c<width;}
}