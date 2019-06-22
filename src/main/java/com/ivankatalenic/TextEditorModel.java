package com.ivankatalenic;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Iterator;

public class TextEditorModel implements Iterable<String> {
	
	private List<String> lines;
	private LocationRange selectionRange;
	
	private Location cursorLocation;
	
	private List<CursorObserver> cursorObservers;
	private List<TextObserver> textObservers;
	
	public TextEditorModel(String initialText) {
		lines = new ArrayList<String>();
		Stream<String> ss = initialText.lines();
		for (Object line : ss.toArray()) {
			lines.add((String) line);
		}
		cursorLocation = new Location(0, 0);
		cursorObservers = new ArrayList<CursorObserver>();
		textObservers = new ArrayList<TextObserver>();
		selectionRange = new LocationRange(new Location(0, 0), new Location(0, 0));
	}
	
	public void moveCursorLeft() {
		int x = cursorLocation.getX();
		int y = cursorLocation.getY();
		if (x > 0) {
			cursorLocation.setX(x - 1);
		} else if (y > 0) {
			cursorLocation.setY(y - 1);
			String line = lines.get(y - 1);
			cursorLocation.setX(line.length());
		}
		notifyCursorObservers();
	}
	
	public void moveCursorRight() {
		int x = cursorLocation.getX();
		String line = lines.get(cursorLocation.getY());
		if (x < line.length()) {
			cursorLocation.setX(x + 1);
		} else {
			if (cursorLocation.getY() < lines.size() - 1) {
				cursorLocation.setY(cursorLocation.getY() + 1);
				cursorLocation.setX(0);
			}
		}
		notifyCursorObservers();
	}
	
	public void moveCursorUp() {
		int y = cursorLocation.getY();
		if (y > 0) {
			y = y - 1;
			String line = lines.get(y);
			if (cursorLocation.getX() + 1 > line.length() + 1) {
				cursorLocation.setX(line.length());
			}
			cursorLocation.setY(y);
			notifyCursorObservers();
		}
	}
	
	public void moveCursorDown() {
		int y = cursorLocation.getY();
		if (y < lines.size() - 1) {
			y = y + 1;
			String line = lines.get(y);
			if (cursorLocation.getX() + 1 > line.length() + 1) {
				cursorLocation.setX(line.length());
			}
			cursorLocation.setY(y);
			notifyCursorObservers();
		}
	}
	
	public void deleteBefore() {
		int x = cursorLocation.getX();
		int y = cursorLocation.getY();
		String line = lines.get(y);
		if (x == 0) {
			if (y > 0) {
				String prevLine = lines.get(y - 1);
				String newLine = prevLine.concat(line);
				lines.remove(y);
				lines.remove(y - 1);
				lines.add(y - 1, newLine);
				cursorLocation.setX(prevLine.length());
				cursorLocation.setY(y - 1);
			}
		} else {
			String first = line.substring(0, cursorLocation.getX() - 1);
			String second;
			if (cursorLocation.getX() >= line.length()) {
				second = "";
			} else {
				second = line.substring(cursorLocation.getX());
			}
			int index = lines.indexOf(line);
			lines.remove(line);
			lines.add(index, first + second);
			cursorLocation.setX(cursorLocation.getX() - 1);
		}
		notifyCursorObservers();
		notifyTextObservers();
	}
	
	public void deleteAfter() {
		int x = cursorLocation.getX();
		int y = cursorLocation.getY();
		String line = lines.get(y);
		if (x >= line.length()) {
			if (y < lines.size() - 1) {
				String nextLine = lines.remove(y + 1);
				String newLine = line.concat(nextLine);
				lines.remove(y);
				lines.add(y, newLine);
			}
		} else {
			String first = line.substring(0, cursorLocation.getX());
			String second;
			if (cursorLocation.getX() >= line.length()) {
				second = "";
			} else {
				second = line.substring(cursorLocation.getX() + 1);
			}
			int index = lines.indexOf(line);
			lines.remove(line);
			lines.add(index, first + second);
		}
		notifyTextObservers();
	}
	
	public void deleteRange(LocationRange r) {
		if (r.getStart().getY() == r.getEnd().getY()) {
			// Single-line operation
			int startX = r.getStart().getX();
			int endX = r.getEnd().getX();
			if (startX > endX) {
				int temp = endX;
				endX = startX;
				startX = temp;
			}
			String line = lines.get(r.getStart().getY());
			String first = line.substring(0, startX);
			String second = line.substring(endX);
			int index = lines.indexOf(line);
			lines.remove(line);
			lines.add(index, first + second);
			cursorLocation.setX(startX);
		} else {
			// Multi-line operation
			int startY = r.getStart().getY();
			int endY = r.getEnd().getY();
			if (startY > endY) {
				int temp = endY;
				endY = startY;
				startY = temp;
			}
			String top = lines.get(startY);
			String bottom = lines.get(endY);
			for (int i = startY; i <= endY; i++) {
				lines.remove(i);
			}
			String first = top.substring(0, r.getStart().getX());
			String second;
			if (r.getEnd().getX() < bottom.length()) {
				second = bottom.substring(r.getEnd().getX());
			} else {
				second = "";
			}
			lines.add(startY, first + second);
		}
		notifyTextObservers();
	}
	
	public void attachCursorObserver(CursorObserver o) {
		cursorObservers.add(o);
	}
	
	public void detachCursorObserver(CursorObserver o) {
		cursorObservers.remove(o);
	}
	
	public void attachTextObserver(TextObserver o) {
		textObservers.add(o);
	}
	
	public void detachTextObserver(TextObserver o) {
		textObservers.remove(o);
	}
	
	public void notifyCursorObservers() {
		for (CursorObserver o : cursorObservers) {
			o.updateCursorLocation(cursorLocation);
		}
	}
	
	public void notifyTextObservers() {
		for (TextObserver o : textObservers) {
			o.updateText();
		}
	}
	
	public Iterator<String> allLines() {
		return lines.iterator();
	}
	
	public Iterator<String> linesRange(int index1, int index2) {
		return lines.subList(index1, index2).iterator();
	}
	
	@Override
	public Iterator<String> iterator() {
		return allLines();
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

	public LocationRange getSelectionRange() {
		return selectionRange;
	}

	public void setSelectionRange(LocationRange selectionRange) {
		this.selectionRange = selectionRange;
	}

	public Location getCursorLocation() {
		return cursorLocation;
	}

	public void setCursorLocation(Location cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

}
