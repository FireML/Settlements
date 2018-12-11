package me.crz.minecraft.kingdoms.util

import me.crz.minecraft.kingdoms.land.LandManager
import me.crz.minecraft.kingdoms.land.LandOwner
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation
import java.util.PriorityQueue
import kotlin.math.pow

/**
 * Performs an A* check to see if a path between two chunks (start and destination) exists.
 */
class UnclaimAStar(
    val landManager: LandManager,
    val owner: LandOwner,
    val unclaimed: SimpleChunkLocation,
    val start: SimpleChunkLocation,
    val destination: SimpleChunkLocation
) {

    /**
     * Performs the pathfinding check.
     */
    fun doesPathExist(): Boolean {
        //val startTime = System.currentTimeMillis()
        val ret = doesPathExist0()
        //val endTime = System.currentTimeMillis()
        //println("Pathfinding time took (${endTime - startTime}ms)")
        return ret
    }

    private fun doesPathExist0(): Boolean {
        val openNodes = PriorityQueue<NodeData>()
        val closedNodes = HashSet<SimpleChunkLocation>()

        openNodes.add(NodeData(start))
        closedNodes.add(start)
        closedNodes.add(unclaimed)

        while (!openNodes.isEmpty()) {
            val currentNode = openNodes.poll()

            // We found the destination, path was found (which is all we care about, not the path itself).
            if (currentNode.chunk == destination) return true

            // Visit adjacent nodes
            fun addAdjacent(dx: Int, dz: Int) {
                val dchunk = currentNode.chunk.copy().add(dx, dz)

                // Ignore closed nodes, unclaimed chunk, and chunks not owned by the specified settlement
                if (landManager.getLand(dchunk)?.landOwner == owner && closedNodes.add(dchunk)) {
                    openNodes.add(NodeData(dchunk))
                }
            }

            UnclaimHelper.ADJACENT.forEach { (dx, dz) -> addAdjacent(dx, dz) }
        }

        // Path destination could not be found
        return false
    }

    /**
     * Stores data about a node.
     * No parent is stored since we don't care about the path itself, just the fact if it exists or not.
     */
    inner class NodeData(val chunk: SimpleChunkLocation) : Comparable<NodeData> {

        /**
         * The computed cost of this node.
         */
        private val f: Float

        /**
         * The movement cost to move to this node. Always 1 for this implementation.
         */
        private val g: Float = 1.0F

        /**
         * The estimated movement cost from this node to the final destination.
         * Uses Euclidean distance as the heuristic.
         * https://en.wikipedia.org/wiki/Euclidean_distance
         */
        private val h: Float =
            (destination.x - chunk.x).toFloat().pow(2) + (destination.z - chunk.z).toFloat().pow(2)

        init {
            f = g + h
        }

        override fun compareTo(other: NodeData): Int = f.compareTo(other.f)

    }

}
