package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsGUI;

public abstract class PlotGUI extends SettlementsGUI {
    public PlotGUI(String name) {
        super("plot." + name);
    }
}
