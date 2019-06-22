package com.ivankatalenic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JComponent;

public class TextEditor extends JComponent implements CursorObserver, TextObserver {
	
	private static final long serialVersionUID = 1L;
	
	private TextEditorModel model;
	
	private KeyboardState keyboardState;
	private KeyboardState shiftedKeyboard;
	private KeyboardState normalKeyboard;
	
	private Font font;
	private Color fontColor;
	
	private int textStartX;
	private int textStartY;
	
	private int cursorX;
	private int cursorY;
	
	private LocationRange selectedText;
	
	public TextEditor(TextEditorModel model) {
		this.model = model;
		model.attachCursorObserver(this);
		model.attachTextObserver(this);
		
		setFocusable(true);
		
		font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
		fontColor = new Color(0.0f, 0.0f, 0.0f);
		
		textStartX = 10;
		textStartY = 10;

		cursorX = 0;
		cursorY = 0;
		
		selectedText = null;
		
		normalKeyboard = new KeyboardState() {
			@Override
			public void pressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					model.moveCursorUp();
					selectedText = null;
					break;
				case KeyEvent.VK_DOWN:
					model.moveCursorDown();
					selectedText = null;
					break;
				case KeyEvent.VK_LEFT:
					model.moveCursorLeft();
					selectedText = null;
					break;
				case KeyEvent.VK_RIGHT:
					model.moveCursorRight();
					selectedText = null;
					break;
				case KeyEvent.VK_DELETE:
					if (selectedText != null) {
						model.deleteRange(selectedText);
						selectedText = null;
					} else {
						model.deleteAfter();
					}
					break;
				case KeyEvent.VK_BACK_SPACE:
					if (selectedText != null) {
						model.deleteRange(selectedText);
						selectedText = null;
					} else {
						model.deleteBefore();
					}
					break;
				case KeyEvent.VK_SHIFT:
					keyboardState = shiftedKeyboard;
					break;
				}
			}

			@Override
			public void released(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		
		shiftedKeyboard = new KeyboardState() {
			@Override
			public void pressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (selectedText == null) {
						selectedText = new LocationRange(
								new Location(model.getCursorLocation()), 
								new Location(model.getCursorLocation())
							);
					}
					model.moveCursorUp();
					selectedText.getEnd().setLocation(model.getCursorLocation());
					break;
				case KeyEvent.VK_DOWN:
					if (selectedText == null) {
						selectedText = new LocationRange(
								new Location(model.getCursorLocation()), 
								new Location(model.getCursorLocation())
							);
					}
					model.moveCursorDown();
					selectedText.getEnd().setLocation(model.getCursorLocation());
					break;
				case KeyEvent.VK_LEFT:
					if (selectedText == null) {
						selectedText = new LocationRange(
								new Location(model.getCursorLocation()), 
								new Location(model.getCursorLocation())
							);
					}
					model.moveCursorLeft();
					selectedText.getEnd().setLocation(model.getCursorLocation());
					break;
				case KeyEvent.VK_RIGHT:
					if (selectedText == null) {
						selectedText = new LocationRange(
								new Location(model.getCursorLocation()), 
								new Location(model.getCursorLocation())
							);
					}
					model.moveCursorRight();
					selectedText.getEnd().setLocation(model.getCursorLocation());
					break;
				case KeyEvent.VK_DELETE:
					model.deleteRange(selectedText);
					selectedText = new LocationRange(
							new Location(model.getCursorLocation()), 
							new Location(model.getCursorLocation())
						);
					break;
				case KeyEvent.VK_BACK_SPACE:
					model.deleteRange(selectedText);
					selectedText = new LocationRange(
							new Location(model.getCursorLocation()), 
							new Location(model.getCursorLocation())
						);
					break;
				case KeyEvent.VK_SHIFT:
					break;
				}
			}

			@Override
			public void released(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:
					keyboardState = normalKeyboard;
					break;
				}
			}
		};
		
		keyboardState = normalKeyboard;
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				keyboardState.pressed(e);
			}
			@Override
			public void keyReleased(KeyEvent e) {
				keyboardState.released(e);
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 600);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		int lineHeight = fm.getHeight();
		int startX = textStartX;
		int startY = textStartY + lineHeight;
		g.setColor(fontColor);
		for (String line : model) {
			g.drawString(line, startX, startY);
			startY += lineHeight;
		}
		g.drawLine(cursorX, cursorY, cursorX, cursorY - lineHeight);
		
		g.setColor(new Color(0.0f, 0.0f, 1.0f, 0.2f));
		if (selectedText != null) {
			List<String> lines = model.getLines();
			if (selectedText.getStart().getY() == selectedText.getEnd().getY()) {
				// Single-line operation
				int startX1 = selectedText.getStart().getX();
				int endX = selectedText.getEnd().getX();
				if (startX1 > endX) {
					int temp = endX;
					endX = startX1;
					startX1 = temp;
				}
				int x = textStartX  
						+ fm.stringWidth(lines.get(selectedText.getStart().getY()).substring(0, selectedText.getStart().getX()));
				int y = textStartY + selectedText.getStart().getY() * lineHeight;
				String line = lines.get(selectedText.getStart().getY());
				int len = 0;
				if (endX < line.length()) {
					len = fm.stringWidth(line.substring(startX1, endX));
				} else {
					len = fm.stringWidth(line);
				}
				g.fillRect(x, y, len, lineHeight);
			} else {
				// Multi-line operation
				int startY1 = selectedText.getStart().getY();
				int endY = selectedText.getEnd().getY();
				if (startY1 > endY) {
					int temp = endY;
					endY = startY1;
					startY1 = temp;
				}
				String top = lines.get(startY);
				for (int i = startY1 + 1; i < endY; i++) {
					g.drawRect(textStartX, textStartY + i * lineHeight, 
							textStartX + fm.stringWidth(lines.get(i)), textStartY + i * lineHeight + lineHeight);
				}
				Location topStart = cursorCoords(selectedText.getStart());
				int topLen = top.length();
				g.drawRect(topStart.getX(), topStart.getY(), topLen, lineHeight);
				Location botEnd = cursorCoords(selectedText.getEnd());
				g.drawRect(textStartX, botEnd.getY() + lineHeight, botEnd.getX() - textStartX, lineHeight);
			}
		}
	}
	
	@Override
	public void updateCursorLocation(Location loc) {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		int lineHeight = fm.getHeight();
		cursorY = textStartY + lineHeight + loc.getY() * lineHeight;
		String line = model.getLines().get(loc.getY());
		cursorX = textStartX + fm.stringWidth(line.substring(0, loc.getX()));
		repaint();
	}

	@Override
	public void updateText() {
		repaint();
	}
	
	private Location cursorCoords(Location cursorLocation) {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		int lineHeight = fm.getHeight();
		cursorY = textStartY + lineHeight + cursorLocation.getY() * lineHeight;
		String line = model.getLines().get(cursorLocation.getY());
		cursorX = textStartX + fm.stringWidth(line.substring(0, cursorLocation.getX()));
		return new Location(cursorX, cursorY);
	}

}
