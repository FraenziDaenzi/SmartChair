package com.hawhamburg.sg.mwrp.gui;

import java.awt.Canvas;
import static com.hawhamburg.sg.mwrp.gui.MwrpGuiConstants.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.print.Printable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.hawhamburg.sg.data.AbstractValue;
import com.hawhamburg.sg.mwrp.DataProvider;
import com.hawhamburg.sg.mwrp.gui.view.IView;
import com.hawhamburg.sg.mwrp.gui.view.MainView;

public final class MwrpCanvas extends Canvas implements MouseListener {
	private static final Color bgColor=new Color(0xffe9e5fc);
	private static final Color fontColor=Color.black;
	private final DataProvider dataProvider;

	private int fps;
	private int currentFrames;
	
	private Deque<IView> views=new LinkedList<IView>();
	
	public MwrpCanvas(DataProvider dataProvider) {
		views.push(new MainView(dataProvider, this));
		this.dataProvider=dataProvider;
	}
	public void startRenderer()
	{
		addMouseListener(this);
		new Thread(this::rLoop).start();
	}
	

	private final void rLoop()
	{
        createBufferStrategy(2);
	    BufferStrategy bs = getBufferStrategy();
	    long pTime=System.currentTimeMillis();
	    long uTime=pTime;
	    long sTime=0;
	    
	    int width=getWidth();
	    int height=getHeight();

	    Graphics2D g = (Graphics2D)bs.getDrawGraphics();
	    System.out.println("Size: "+width+"x"+height);
	    FontMetrics fm=g.getFontMetrics();
		while(true)
		{	
			
		    g = (Graphics2D)bs.getDrawGraphics();
		    g.setColor(Color.white);
		    g.fillRect(0, 0, width, height);
		    views.peek().draw(g, fm, width, height);
		    
		    g.setColor(fontColor);
		    g.drawString("FPS: "+fps, width-70, 17);
		    
		    g.setColor(Color.red);
		    g.drawRect(0, 0, width-1, height-1);
		    currentFrames++;
		    g.dispose();
		    bs.show();
		    
		    if(pTime-uTime>1000)
		    {
		    	uTime=pTime;
		    	fps=currentFrames;
		    	currentFrames=0;
		    }
		    pTime+=1000/FPS_LIMIT;
		    sTime=pTime-System.currentTimeMillis();
		    if(sTime>0)
				try {
					Thread.sleep(sTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void popView()
	{
		if(views.size()>1)
			views.pop();
	}
	
	public void pushView(IView view)
	{
		views.push(view);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		//views.peek().mouseClick(e);
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		views.peek().mouseClick(e);
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
