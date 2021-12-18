package de.themoep.settlements.land.gui;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.world.SimpleChunkLocation;

public abstract class PlotGUI extends ConfigurableGui {
    protected final SimpleChunkLocation landLocation;

    public PlotGUI(Resident resident, OwnedLand editedLand, String name) {
        super(resident, "plot." + name);
        landLocation = editedLand.getLocation().copy();
    }

    @Override
    public void open() {
        // One last check if we can really open this GUI
        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
        if (currentLand != null && (currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident))) {
            super.open();
        } else {
            getError("not-your-plot").tell(resident);
        }
    }
}
