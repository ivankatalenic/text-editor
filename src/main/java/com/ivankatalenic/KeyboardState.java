package com.ivankatalenic;

import java.awt.event.KeyEvent;

public interface KeyboardState {
	
	public void pressed(KeyEvent e);
	
	public void released(KeyEvent e);
	
}
