package io.dwerle.dawclipse.rcp;

import static io.dwerle.dawclipse.rcp.SWTHelper.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.beadsproject.beads.core.io.JavaSoundAudioIO;

public class MixerDialog extends Dialog {

	private JavaSoundAudioIO audioIO;

	public MixerDialog(Shell parentShell, JavaSoundAudioIO audioIO) {
		super(parentShell);
		this.audioIO = audioIO;
	}
	
	public MixerDialog(JavaSoundAudioIO audioIO) {
		super(Display.getCurrent().getActiveShell());
		this.audioIO = audioIO;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = parent;
		
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerinfo.length; i++) {
			String buttonInfo = "";
			String name = mixerinfo[i].getName();
			if (name.equals(""))
				name = "No name";
			buttonInfo += name + " - " + mixerinfo[i].getDescription();

			Button currentButton = new Button(container, SWT.PUSH);
			currentButton.setText(buttonInfo);
			final int currentId = i;
			currentButton.addSelectionListener(onSelection(it -> {
				audioIO.selectMixer(currentId);
				this.close();
			}));
		}
		
		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}
}