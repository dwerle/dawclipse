package io.dwerle.dawclipse.rcp.backend;

import java.util.function.Consumer;

import net.beadsproject.beads.core.Bead;

public final class BeadHelper {
	public static Bead messageBead(Consumer<Bead> doWhat) {
		return new Bead() {
			@Override
			protected void messageReceived(Bead message) {
				doWhat.accept(message);
			}
		};
	}
}
