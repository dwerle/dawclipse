package io.dwerle.dawclipse.rcp;

import java.beans.PropertyChangeEvent;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.widgets.Display;

import io.dwerle.dawclipse.rcp.backend.Backend;

@Creatable
@Singleton
public final class BundleHelper {
	private static ExecutorService executor = Executors.newCachedThreadPool();
	private final Backend backend;
	private static BundleHelper instance;
	private boolean active = false; 
	
	@Inject
	public BundleHelper(Backend backend) {
		this.backend = backend;
		BundleHelper.instance = this;
		active = true;
	}
	
	public static Backend getBackend() {
		return instance.backend;
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
					if (instance.active)
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
