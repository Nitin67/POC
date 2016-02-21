package com.game;

import java.util.Scanner;


public class Game {
	
	final private int GRIDSIZE;
	int bestScore;
	int currentScore;
	int previousGrid[][];
	int grid[][];
	boolean isUndoDone=false;
	
	public Game() {
		GRIDSIZE=4;
		currentScore=0;
		initializeGridAndStartGame();
		getUserInput();
	}

	private void getUserInput() {
		Scanner scanner=new Scanner(System.in);
		while(true)
		{
			char ch=scanner.next(".").charAt(0);
			
			if(ch=='w' || ch=='W')
			{
				copy(previousGrid,grid);
				up();
				isUndoDone=false;
			}
			else if(ch=='a' || ch=='A')
			{
				copy(previousGrid,grid);
				left();
				isUndoDone=false;
			}
			else if(ch=='s' || ch=='S')
			{
				copy(previousGrid,grid);
				down();
				isUndoDone=false;
			}
			else if(ch=='D' || ch=='d')
			{
				copy(previousGrid,grid);
				right();
				isUndoDone=false;
			}
			else if(ch=='R' || ch=='r')
			{
				refresh();
			}
			else if((ch=='U' || ch=='u')&&!isUndoDone)
			{
				undoLastEvent();
			}
			else  
			{
				System.out.println("Wrong input");
			}
			
			
			if(isGridFull())
			{
				System.out.println("Game Over!!!");
				break;
			}
		}
	}

	private void copy(int[][] previousGrid2, int[][] grid2) {
	
		for (int i = 0; i < grid2.length; i++) {
		
			for (int j = 0; j < grid2.length; j++) {
				previousGrid2[i][j]=grid2[i][j];
			}
		}
		
	}

	private void undoLastEvent() {
		grid=previousGrid.clone();
		isUndoDone=true;
		displayGrid();
	}

	private void refresh() {
		grid=new int[GRIDSIZE][GRIDSIZE];
		previousGrid=new int[GRIDSIZE][GRIDSIZE];
		int inty[]={2,4};
		int x=-1,y=-1;
		while(x<0 || x>3)
		x=getRandomNumber(GRIDSIZE);
		while(y<0 || y>3)
			y=getRandomNumber(GRIDSIZE);

		grid[x][y]=inty[getRandomNumber(1)];
		int x1=-1,y1=-1;
		while(x1<0 || x1>3 || y1<0 || y1>3 || (y1==y && x1==x))
		{
			x1=getRandomNumber(GRIDSIZE);
			y1=getRandomNumber(GRIDSIZE);
		}
		grid[x1][y1]=inty[getRandomNumber(1)];
		displayGrid();
	}

	private void right() {
		
		for (int i = 0; i < grid.length; i++) {
			
			int prev=-1;
			int init=grid.length-1;
			int intr=grid.length-1;
			for (int j = grid.length-1; j>=0; j--) {
				
				if(prev==-1 && grid[i][j]!=0)
				{
					prev=j;
				}
				else if(grid[i][j]!=0 && prev!=-1 && grid[i][j]==grid[i][prev])
				{
					grid[i][prev]=2*grid[i][prev];		
					grid[i][j]=0;
					prev=-1;
				}
				else if(grid[i][j]!=0 && prev!=-1 && grid[i][j]!=grid[i][prev])
				{
					prev=j;
				}		
				
			}
			
			while (init>=0) {
				
				if(grid[i][init]!=0)
				{
					if(init!=intr){
					grid[i][intr]=grid[i][init];
					grid[i][init]=0;
					}
					intr--;
				}
					init--;
			}
		}
		putNumberAtRandomLocation();
		
	}
	
	public void putNumberAtRandomLocation()
	{
		int inty[]={2,4};
		int x=-1,y=-1;
		while(!((x>=0 && x<=3 && y>=0 && y<=3) && grid[x][y]==0)){
		x=getRandomNumber(GRIDSIZE-1);
		y=getRandomNumber(GRIDSIZE-1);
		}
		System.out.println("Random loc" + x+"--" +y);
		grid[x][y]=inty[getRandomNumber(1)];
		displayGrid();
	}

	private void down() {
	
		for (int i = 0; i < grid.length; i++) {
			int prev=-1;
			int init=grid.length-1;
			int intr=grid.length-1;
			for (int j = grid.length-1; j >=0; j--) {
				
				if(prev==-1 && grid[j][i]!=0)
				{
					prev=j;
				}
				else if(grid[j][i]!=0 && prev!=-1 && grid[j][i]==grid[prev][i])
				{
					grid[prev][i]=2*grid[prev][i];		
					grid[j][i]=0;
					prev=-1;
				}
				else if(grid[j][i]!=0 && prev!=-1 && grid[j][i]!=grid[prev][i])
				{
					prev=j;
				}			
			}
			
			while (init>=0) {
				
				if(grid[init][i]!=0)
				{
				if(intr!=init){
				grid[intr][i]=grid[init][i];
				grid[init][i]=0;
				}
				intr--;
				}
				init--;
			}
		}
		putNumberAtRandomLocation();
	}

	private void left() {
		
		for (int i = 0; i < grid.length; i++) {
		
			int prev=-1;
			int init=0;
			int intr=0;
			for (int j = 0; j < grid.length; j++) {
				
				if(prev==-1 && grid[i][j]!=0)
				{
					prev=j;
				}
				else if(grid[i][j]!=0 && prev!=-1 && grid[i][j]==grid[i][prev])
				{
					grid[i][prev]=2*grid[i][prev];		
					grid[i][j]=0;
					prev=-1;
				}
				else if(grid[i][j]!=0 && prev!=-1 && grid[i][j]!=grid[i][prev])
				{
					prev=j;
				}		
				
			}
			
			while (init<grid.length) {
				
				if(grid[i][init]!=0)
				{
					if(intr!=init){
						grid[i][intr]=grid[i][init];
						grid[i][init]=0;
					}
				intr++;
				}
				init++;
			}
		}
		putNumberAtRandomLocation();
	}

	private void up() {

		for (int i = 0; i < grid.length; i++) {
			int prev=-1;
			int init=0;
			int intr=0;
			for (int j = 0; j < grid.length; j++) {
				if(prev==-1 && grid[j][i]!=0)
				{
					prev=j;
				}
				else if(grid[j][i]!=0 && prev!=-1 && grid[j][i]==grid[prev][i])
				{
					grid[prev][i]=2*grid[prev][i];		
					grid[j][i]=0;
					prev=-1;
				}
				else if(grid[j][i]!=0 && prev!=-1 && grid[j][i]!=grid[prev][i])
				{
					prev=j;
				}		
			}
			while (init<grid.length) {
				
				if(grid[init][i]!=0)
				{
					if(init!=intr){
					grid[intr][i]=grid[init][i];
					grid[init][i]=0;
					}
					intr++;
				}
					init++;
			}
	}
		putNumberAtRandomLocation();
	}
	
	private boolean isGridFull()
	{
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				if(grid[i][j]==0)
					return false;
			}
		}
		return true;
	}

	public void initializeGridAndStartGame()
	{
		refresh();
		
	}

	public int getRandomNumber(int val)
	{
		return (int) Math.round(Math.random()*val);
	}
	
	public void displayGrid()
	{
		System.out.println( "--------");
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				if(grid[i][j]!=0)
					System.out.print(grid[i][j]+"|");
				else
					System.out.print(" |");
			}
			System.out.println();	
		}
		System.out.println( "--------");
	}
	
	public static void main(String[] args) {

		new Game();
	
	}
	

}
