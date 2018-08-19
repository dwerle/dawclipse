package io.dwerle.dawclipse.rcp;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;

import io.dwerle.dawclipse.rcp.backend.Backend;

public final class BundleHelper {
	private static ExecutorService executor = Executors.newCachedThreadPool();
	private static BundleHelper instance;
	private boolean active = false; 
	
	public BundleHelper() {
		if (instance != null)
			throw new AssertionError();
		
		active = true;
	}
	
	public static BundleHelper getInstance() {
		if (BundleHelper.instance == null)
			BundleHelper.instance = new BundleHelper();
		
		return BundleHelper.instance;
	}
	
	public static Backend getBackend() {
		return Backend.getInstance();
	}

	public static void doDisplay(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	public static void on(String property, Consumer<PropertyChangeEvent> doWhat) {
		getBackend().pcs.addPropertyChangeListener(property, doWhat::accept);
	}

	public static void on(Consumer<PropertyChangeEvent> doWhat) {
		getBackend().pcs.addPropertyChangeListener(doWhat::accept);
	}

	public static void onDisplay(String property, Consumer<PropertyChangeEvent> doWhat) {
		getBackend().pcs.addPropertyChangeListener(property,
				e -> waitAnd(getBackend().getDelay(), () -> doDisplay(() -> {
					if (getInstance().active)
						doWhat.accept(e);
				})));
	}

	private static void waitAnd(int delay, Runnable runnable) {
		executor.submit(() -> {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			runnable.run();
		});
	}
	
	public void shutdown() {
		active = false;
	}

	public static void onDisplay(Consumer<PropertyChangeEvent> doWhat) {
		getBackend().pcs.addPropertyChangeListener(e -> doDisplay(() -> doWhat.accept(e)));
	}
}
