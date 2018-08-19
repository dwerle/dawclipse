
package io.dwerle.dawclipse.rcp;

import static io.dwerle.dawclipse.rcp.BundleHelper.*;
import static io.dwerle.dawclipse.rcp.SWTHelper.*;
import static io.dwerle.dawclipse.rcp.backend.BackendConstants.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class TransportPart {
	public TransportPart() {
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		buildDisplay(parent);
	}

	private void buildDisplay(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		parent.setLayout(layout);
		
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gridData.widthHint = 50;
		
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(gridData);
		Label label2 = new Label(parent, SWT.NONE);
		label2.setLayoutData(gridData);
		
		Scale scale = new Scale(parent, SWT.HORIZONTAL);
		
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		scale.setLayoutData(gridData);
		scale.pack();
		scale.setMaximum(5000);
		scale.setMinimum(1000);
		scale.setSelection(4000);

		onDisplay(EVENT_TRANSPORT_TICK, e -> {
			label.setText(Integer.toString(getBackend().getTransport().getBeatCount()));
			label2.setText(Integer.toString(getBackend().getTransport().getSubBeat()));
		});
		scale.addSelectionListener(onSelection(e -> getBackend().getTransport().setInterval(scale.getSelection())));
		parent.getParent().pack();
	}
	
	@PreDestroy
	public void preDestroy() {
		BundleHelper.getInstance().shutdown();
		getBackend().shutdown();
	}
}