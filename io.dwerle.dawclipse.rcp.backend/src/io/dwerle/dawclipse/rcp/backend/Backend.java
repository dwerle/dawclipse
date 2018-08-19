package io.dwerle.dawclipse.rcp.backend;

import static io.dwerle.dawclipse.rcp.backend.BackendConstants.*;
import static io.dwerle.dawclipse.rcp.backend.BeadHelper.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.inject.Singleton;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.ugens.Clicker;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Static;

import org.eclipse.e4.core.di.annotations.Creatable;

public class Backend {
	private static Backend instance;

	public final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public AudioContext ac;
	private Transport transport;
	private Gain g;
	public Integer mixer;

	private Backend() {
		ac = new AudioContext();
		if (Backend.instance != null)
			throw new AssertionError();
	}
	
	public static Backend getInstance() {
		if (Backend.instance == null)
			Backend.instance = new Backend();
		
		return Backend.instance;
	}
	
	public static Backend createNewInstance() {
		if (Backend.instance != null)
			throw new AssertionError();
		
		return getInstance();
	}
	
	public Transport getTransport() {
		return transport;
	}

	public void initialize() {
		pcs.firePropertyChange(EVENT_AUDIO_CONTEXT, null, ac);

		transport = new Transport(ac, 4000);
		transport.setClick(true);
		transport.setTicksPerBeat(8);

		transport.addMessageListener(messageBead(b -> notify(EVENT_TRANSPORT_TICK,
				transport.getBeatCount() - 1, transport.getCount())));

		g = new Gain(ac, 1, new Static(ac, .2f));

		ac.out.addDependent(transport);
		ac.out.addInput(g);
		
		if (this.mixer != null) {
			((JavaSoundAudioIO) ac.getAudioIO()).selectMixer(this.mixer);
		}

		ac.start();
	}

	private void notify(String propertyName, Object oldValue, Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void shutdown() {
		PropertyChangeListener[] propertyChangeListeners = pcs.getPropertyChangeListeners();
		for (int i = 0; i < propertyChangeListeners.length; i++) {
			pcs.removePropertyChangeListener(propertyChangeListeners[i]);
		}
		ac.stop();
	}

	public class Transport extends Clock {
		@Override
		protected void doClick() {
	    	if (isBeat())
	    		context.out.addInput(new Clicker(context, .3f));
	    	else
				context.out.addInput(new Clicker(context, .1f));
		}
		
		public Transport(AudioContext ac, int interval) {
			super(ac, interval);
		}

		public int getSubBeat() {
			return (int) (getCount() % getTicksPerBeat());
		}
		
		public void setInterval(float value) {
			setIntervalEnvelope(new Static(getContext(), value));
		}
	}

	/**
	 * in milliseconds
	 * @return
	 */
	public int getDelay() {
		return 90;
	}
}
