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
import java.util.Random;
import javax.swing.Timer;

class Blockies
{
	public static void main(String[] args)
	{
		new MainFrame().setVisible(true);
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
		view=new MainView(()->view.stop());
		getContentPane().setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
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

	private int height,width;
	MainView(Runnable stop)
	{
		this(new MapData(stop));
	}
	MainView(MapData map)
	{
		playing=false;
		this.map=map;
		height=map.getHeight();
		width=map.getWidth();
		timer=new Timer(20,
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(counter>=50)
						counter=0;
					if(counter++==0)
						map.pushBlockies();
					if(counter%2==0) map.fall();
					paintMap();
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
					else
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

	public void start()
	{
		playing=true;
		counter=0;
		map.clear();
		timer.start();
	}

	public void stop()
	{
		playing=false;
		timer.stop();
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
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				selected[i][j]=false;
				used[i][j]=false;
			}
		dfs(r,c);
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
				for(int j=0;j<height;j++)
					for(int k=0;k<width;k++)
						map[j][k]='G';
				stop.run();
				return;
			}
		for(int i=0;i<width;i++)
			map[0][i]=colors[rand.nextInt(4)];
	}

	public void click(int r,int c)
	{
		System.out.println("selectCount:"+selectCount);
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
		if(r<0||c<0||r>=height||c>=width) return Color.WHITE;
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
		if(r<0||c<0||r>=height||c>=width) return false;
		return selected[r][c];
	}

	private static final int[] dfsa={-1,-1,-1, 0,0, 1,1,1};
	private static final int[] dfsb={-1, 0, 1,-1,1,-1,0,1};
	private void dfs(int r,int c)
	{
		if(!isvalid(r,c)) return;
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