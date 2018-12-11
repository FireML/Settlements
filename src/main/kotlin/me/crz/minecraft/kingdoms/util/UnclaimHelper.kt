package me.crz.minecraft.kingdoms.util

import me.crz.minecraft.kingdoms.land.LandManager
import me.crz.minecraft.kingdoms.land.OwnedLand
import me.crz.minecraft.kingdoms.village.Village
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation
import java.util.ArrayDeque

object UnclaimHelper {

    val ADJACENT: List<Pair<Int, Int>> = listOf(
        -1 to -1, -1 to 0, -1 to 1,
        0 to -1, 0 to 1,
        1 to -1, 1 to 0, 1 to 1
    )

    @JvmStatic
    fun getUnclaimConnected(manager: LandManager, land: OwnedLand): Set<OwnedLand> {
        val owner = manager.getLand(land).landOwner

        fun getAdj(dx: Int, dz: Int): SimpleChunkLocation? =
            land.copy().add(dx, dz).takeIf { manager.getLand(it)?.landOwner == owner }

        val homeBlocks = HashSet<SimpleChunkLocation>()
        homeBlocks.add(owner.home.toChunkLocation())
        (owner as? Village)?.outpostHomes?.values?.mapTo(homeBlocks) { it.toChunkLocation() }

        val adjacent = ADJACENT.mapNotNull { (dx, dz) -> getAdj(dx, dz) }

        if (adjacent.size == 1) {
            // No other land to unclaim
            return emptySet()
        }

        fun doCheck(loc: SimpleChunkLocation): Boolean = homeBlocks.any {
            UnclaimAStar(manager, owner, land, loc, it).doesPathExist()
        }

        val orphans = HashSet<SimpleChunkLocation>()
        for (adj in adjacent) {
            if (!doCheck(adj)) {
                println("found orphan: ${adj.simpleKey}")
                orphans.add(adj)
            }
        }

        if (orphans.isEmpty()) {
            // All the adjacent blocks connect to a home block, so there's nothing else to unclaim
            return emptySet()
        }

        val queue = ArrayDeque<SimpleChunkLocation>()
        queue.addAll(orphans)

        // Reuse orphans set to store chunks marked for removal
        orphans.clear()

        while (queue.isNotEmpty()) {
            val cur = queue.poll()

            // If the current chunk isn't already marked for removal, add its adjacent chunks
            if (orphans.add(cur)) {
                fun checkAdj(dx: Int, dz: Int) {
                    val found = manager.getLand(cur.copy().add(dx, dz))
                    if (found != null && found != land && found.landOwner == owner) {
                        queue.add(found)
                    }
                }

                for ((dx, dz) in ADJACENT) {
                    checkAdj(dx, dz)
                }
            }
        }

        return orphans.mapNotNullTo(HashSet(orphans.size)) { manager.getLand(it) }
    }

    @JvmStatic
    fun getConnected(manager: LandManager, land: OwnedLand): Set<OwnedLand> {
        val owner = land.landOwner
        val ret = HashSet<SimpleChunkLocation>()

        val queue = ArrayDeque<SimpleChunkLocation>()
        queue.addAll(ret)

        // Reuse orphans set to store chunks marked for removal
        ret.clear()

        while (queue.isNotEmpty()) {
            val cur = queue.poll()

            // If the current chunk isn't already marked for removal, add its adjacent chunks
            if (ret.add(cur)) {
                fun checkAdj(dx: Int, dz: Int) {
                    val found = manager.getLand(cur.copy().add(dx, dz))
                    if (found != null && found != land && found.landOwner == owner) {
                        queue.add(found)
                    }
                }

                for ((dx, dz) in ADJACENT) {
                    checkAdj(dx, dz)
                }
            }
        }

        return ret.mapNotNullTo(HashSet(ret.size)) { manager.getLand(it) }
    }

}
