package me.crz.minecraft.kingdoms.land;

import java.util.Iterator;
import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.KingdomRunnable;

class LandManager$1 implements KingdomRunnable {
    final /* synthetic */ LandManager this$0;
    
    LandManager$1(final LandManager this$0) {
        this$0 = this$0;
        super();
    }
    
    @Override
    public void run() {
        for (final Document doc : DatabaseManager.getLandCollection().find()) {
            final OwnedLand land = new OwnedLand(doc);
            land0.put((Object)land.getWorldName(), (Object)this$0.getData(land.getX(), land.getZ()), (Object)land);
            mobData.put((Object)land.getWorldName(), (Object)this$0.getData(land.getX(), land.getZ()), (Object)land.isMobSpawning());
        }
    }
}