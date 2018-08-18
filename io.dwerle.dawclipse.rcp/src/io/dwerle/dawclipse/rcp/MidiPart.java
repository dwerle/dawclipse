
package io.dwerle.dawclipse.rcp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class MidiPart {
	private final Long start;
	private final Long end;
	private List<Note> allNotes;
	
	private static class Note {
		public Long start;
		public Long end;
		public int channel;
		public int pitch;
		public int velocity;
		public Note(Long start, Long end, int channel, int pitch, int velocity) {
			super();
			this.start = start;
			this.end = end;
			this.channel = channel;
			this.pitch = pitch;
			this.velocity = velocity;
		}
		
		public int getPitch() {
			return pitch;
		}
		
		public Long getStart() {
			return start;
		}
		
		public Long getEnd() {
			return end;
		}
	}

	@Inject
	public MidiPart() throws InvalidMidiDataException, IOException {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		URL resource = bundle.getResource("/resource/MIDI_sample.mid");

		Sequence sequence = MidiSystem.getSequence(resource.openStream());
		Track[] tracks = sequence.getTracks();
		
		try {
			Sequencer sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.setSequence(sequence);
			sequencer.start();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
		
		Long start = Long.MAX_VALUE;
		Long end = Long.MIN_VALUE;
		allNotes = new ArrayList<>();
		Map<Integer, Note> openNotes = new HashMap<>();

		for (int y = 0; y < tracks.length; y++) {
			Track data = tracks[y];
			for (int i = 0; i < data.size(); i++) {
				MidiEvent midiEvent = data.get(i);
				long tick = midiEvent.getTick();
				start = Math.min(start, tick);
				end = Math.max(end, tick);
				MidiMessage message = midiEvent.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage m = (ShortMessage) message;
					if (m.getCommand() == ShortMessage.NOTE_ON) {
						openNotes.put(m.getData1(), new Note(tick, null, m.getChannel(), m.getData1(), m.getData2()));
					}
					else if (m.getCommand() == ShortMessage.NOTE_OFF) {
						Note note = openNotes.get(m.getData1());
						note.end = tick;
						allNotes.add(note);
						openNotes.remove(m.getData1());
					}
					else
						System.out.println(m.getCommand());
				}
			}
		}
		
		this.end = end;
		this.start = start;
		
		System.out.println("start @ " + start + ", end @ " + end);
		System.out.println("still open: " + openNotes.size());
		openNotes.entrySet().forEach(it -> {
			it.getValue().end = this.end;
			allNotes.add(it.getValue());
		});
		openNotes.clear();
		System.out.println("notes: " + allNotes.size());
		System.out.println(allNotes.stream().mapToInt(Note::getPitch).min().getAsInt());
		System.out.println(allNotes.stream().mapToInt(Note::getPitch).max().getAsInt());
	}

	public static class MidiCanvas extends Canvas {
		private LocalResourceManager resourceManager;
		private List<Note> notes;
		private int max;
		private int min;
		private long start;
		private long end;

		public MidiCanvas(Composite parent, int style, List<Note> notes) {
			super(parent, style);
			//notes = notes.stream().filter((it) -> { return it.end < 80000; }).collect(Collectors.toList());
			this.notes = notes;
			this.min = notes.stream().mapToInt(Note::getPitch).min().getAsInt();
			this.max = notes.stream().mapToInt(Note::getPitch).max().getAsInt();
			this.start = notes.stream().mapToLong(Note::getStart).min().getAsLong();
			this.end = notes.stream().mapToLong(Note::getEnd).max().getAsLong();
			
			resourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
			
			addPaintListener(paintEvent -> {
				GC gc = paintEvent.gc;
				paintBorder(gc);
				paintNotes(gc);
			});
		}
		
		private final static int[] colors = {
				SWT.COLOR_BLUE, SWT.COLOR_CYAN, SWT.COLOR_GREEN,
				SWT.COLOR_RED, SWT.COLOR_MAGENTA
		};

		private void paintNotes(GC parent) {
			double gridX = (getBounds().width * 1.d) / ((this.end + 1 - this.start) * 1.d);
			double gridY = (getBounds().height * 1.d) / ((this.max + 1 - this.min) * 1.d);
			
			Image image = new Image(parent.getDevice(), getBounds());
			GC gc = new GC(image);
			for (Note n : notes) {
				gc.setBackground(getDisplay().getSystemColor(colors[n.channel % colors.length]));
				gc.setAlpha(Math.max(10, Math.min(240, n.velocity)));
				int x = (int) (gridX * (n.getStart() - start));
				int y = getBounds().height - (int) (gridY * (n.getPitch() + 1 - min));
				int w = (int) (gridX * (n.getEnd() - n.getStart()));
				int h = (int) (gridY);
				
				gc.fillRoundRectangle(x-1, y-1, w+2, h+2, 4, 4);
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				gc.setAlpha(10);
				gc.drawRoundRectangle(x-1, y-1, w+2, h+2, 4, 4);
			}
			parent.drawImage(image, 0, 0);
		}

		private void paintBorder(GC gc) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			gc.setAlpha(200);
			gc.fillRoundRectangle(1, 1, getBounds().width-1, getBounds().height-1, 2, 2);
		}

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		new MidiCanvas(parent, SWT.NONE, this.allNotes);
	}

}