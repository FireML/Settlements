package de.themoep.settlements.utils.dialog.claim;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.dialog.InfoDialogBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders a 17x17 grid of chunks to allow easy claim management.
 */
public class ClaimMap {

    private static final int xy = 17;

    private static final Component NOT_A_RESIDENT = Component.text(
        "You are not a resident? Please report this to admins.",
        NamedTextColor.RED
    );

    public static void open(@NonNull Player player) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident == null) {
            player.sendMessage(NOT_A_RESIDENT);
            return;
        }
        int searchRange = (xy - 1) / 2;

        List<Chunk> chunks = getChunks(player.getChunk(), searchRange);
        Component content = buildContent(player, chunks, xy);

        new InfoDialogBuilder()
            .withTitle("Manage Claims")
            .addContent(content)
            .open(player);
    }

    private static Component buildContent(@NonNull Player player, @NonNull List<Chunk> chunks, int xy) {
        TextComponent.Builder builder = Component.text();
        int index = 0;
        for (int row = 0; row < xy; row++) {
            for (int col = 0; col < xy; col++) {
                Chunk chunk = chunks.get(index++);
                builder.append(ChunkState.get(player, chunk).getSquare());
            }
            builder.appendNewline();
        }
        return builder.build();
    }

    private static List<Chunk> getChunks(Chunk middle, int range) {
        int maxX = middle.getX() + range;
        int minX = middle.getX() - range;
        int maxZ = middle.getZ() + range;
        int minZ = middle.getZ() - range;

        List<Chunk> chunks = new ArrayList<>();
        for (int currentX = minX; currentX <= maxX; currentX++) {
            for (int currentZ = minZ; currentZ <= maxZ; currentZ++) {
                chunks.add(middle.getWorld().getChunkAt(currentX, currentZ));
            }
        }
        chunks.sort(Comparator.comparingInt(Chunk::getZ).thenComparingInt(Chunk::getX));
        return chunks;
    }

}
