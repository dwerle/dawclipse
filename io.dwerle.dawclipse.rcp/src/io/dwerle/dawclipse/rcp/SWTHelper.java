package io.dwerle.dawclipse.rcp;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public final class SWTHelper {
	public static SelectionListener onSelection(Consumer<SelectionEvent> doWhat) {
		return new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doWhat.accept(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doWhat.accept(e);
			}
		};
	}
}
