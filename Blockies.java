import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.ActionEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.Random;

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
		setPreferredSize(new Dimension(300,400));
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		view=new MainView(()->view.stop());
		getContentPane().setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
	}
}

class MainView extends JPanel
{
	private MapData map;
	MainView(Runnable stop)
	{
		MainView(new MapData(stop));
	}
	MainView(MapData map)
	{
		this.map=map;
	}

	public void pushTimerEvent()
	{
		map.fall();
	}

	public void stop()
	{
		//stop
	}

	@Override
	public void paint(Graphics g)
	{
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				g.setColor(map.getColor(i,j));
				g.fillRoundRect(j*20,i*20,20,20,3,3);
			}
		g.setColor(Color.WHITE);
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
				if(map.isSelected(i,j))
				{
					g.drawRoundRect(j*20,i*20,20,20,3,3);
					g.fillOval(j*20+7,i*20+7,6,6);
				}
	}
}

class MapData
{
	private static final int WIDTH=15;
	private static final int HEIGHT=20;
	private char[][] map;
	private boolean[][] selected;
	private int height,width;
	private boolean[][] used;
	private Runnable stop;
	private Random rand=new Random();
	MapData(Runnable stop)
	{
		MapData(HEIGHT,WIDTH);
	}
	MapData(int height,int width)
	{
		this.height=height;
		this.width=width;
		map=new char[height][width];
		selected=new boolean[height][width];
		used=new boolean[height][width];
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				map[i][j]=0;
				selected[i][j]=false;
			}
	}

	public void update(int r,int c)
	{
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++)
			{
				selected[i][j]=0;
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

	public Color getColor(int r,int c)
	{
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
		}
	}

	public boolean isSelected(int r,int c)
	{
		return selected[r][c];
	}

	private static final int[] dfsa={-1,-1,-1, 0,0, 1,1,1};
	private static final int[] dfsb={-1, 0, 1,-1,1,-1,0,1};
	private void dfs(int r,int c)
	{
		used[r][c]=true;
		if(map[r][c]=='w') return;
		selected[r][c]=true;
		for(int i=0;i<8;i++)
			if((!used[r+dfsa[i]][c+dfsb[i]])&&map[r+dfsa[i]][c+dfsb[i]]==map[r][c])
				dfs(r+dfsa[i],c+dfsb[i]);
	}
}