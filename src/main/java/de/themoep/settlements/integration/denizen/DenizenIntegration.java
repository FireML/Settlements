package de.themoep.settlements.integration.denizen;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import de.themoep.settlements.integration.denizen.events.SettlementsChatScriptEvent;
import de.themoep.settlements.integration.denizen.events.SettlementsEmpireScriptEvent;
import de.themoep.settlements.integration.denizen.events.SettlementsSettlementScriptEvent;
import de.themoep.settlements.integration.denizen.objects.EmpireTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;

public class DenizenIntegration {

    private final Denizen denizen;

    public DenizenIntegration() {
        denizen = Denizen.getInstance();

        ObjectFetcher.registerWithObjectFetcher(LandTag.class, LandTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(SettlementTag.class, SettlementTag.tagProcessor);
        ObjectFetcher.registerWithObjectFetcher(EmpireTag.class, EmpireTag.tagProcessor);

        ScriptEvent.registerScriptEvent(new SettlementsSettlementScriptEvent());
        ScriptEvent.registerScriptEvent(new SettlementsChatScriptEvent());
        ScriptEvent.registerScriptEvent(new SettlementsEmpireScriptEvent());
    }

    public String getDenizenVersion() {
        return denizen.getDescription().getVersion();
    }

}
