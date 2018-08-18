
package io.dwerle.dawclipse.rcp;

import static io.dwerle.dawclipse.rcp.BundleHelper.*;
import static io.dwerle.dawclipse.rcp.SWTHelper.*;
import static io.dwerle.dawclipse.rcp.backend.BackendConstants.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class MainPart {
	private BundleHelper bundleHelper;

	@Inject
	public MainPart(BundleHelper bundleHelper) {
		this.bundleHelper = bundleHelper;
	}

	@PostConstruct
	public void postConstruct(Composite parent, IEclipseContext context) {
		context.getParent().set("frontend.shutdown", false);
		buildDisplay(parent);
	}

	private void buildDisplay(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		Label label2 = new Label(parent, SWT.NONE);
		Scale scale = new Scale(parent, SWT.HORIZONTAL);
		scale.setMaximum(5000);
		scale.setMinimum(1000);
		scale.setSelection(4000);

		onDisplay(EVENT_TRANSPORT_TICK, e -> {
			label.setText(Integer.toString(getBackend().getTransport().getBeatCount()));
			label2.setText(Integer.toString(getBackend().getTransport().getSubBeat()));
		});
		scale.addSelectionListener(onSelection(e -> getBackend().getTransport().setInterval(scale.getSelection())));
		parent.pack();
	}
	
	@PreDestroy
	public void preDestroy(IEclipseContext context) {
		bundleHelper.shutdown();
		getBackend().shutdown();
	}
}