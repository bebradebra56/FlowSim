package com.flowsim.flosasms.domain

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

data class BallState(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 2f,
    val active: Boolean = true,
    val bucketIndex: Int = -1,
    val colorLong: Long = 0xFF8B5CF6
)

data class PinState(val x: Float, val y: Float)

class PhysicsEngine(
    val boardWidth: Float = 400f,
    val boardHeight: Float = 600f,
    val numBuckets: Int = 4,
    var gravity: Float = 0.35f,
    var bounce: Float = 0.5f,
    var spread: Float = 0.5f,
    private val rows: Int = 10
) {
    companion object {
        const val PIN_RADIUS = 5f
        const val BALL_RADIUS = 10f
        const val COLLISION_DIST = PIN_RADIUS + BALL_RADIUS
    }

    val pins: List<PinState> = buildPins()
    private val _balls = mutableListOf<BallState>()
    val balls: List<BallState> get() = _balls.toList()
    val activeBallCount: Int get() = _balls.count { it.active }

    private fun buildPins(): List<PinState> {
        val result = mutableListOf<PinState>()
        val topMargin = boardHeight * 0.08f
        val vertSpacing = (boardHeight * 0.72f) / rows
        val leftMargin = boardWidth * 0.10f
        val rightMargin = boardWidth * 0.10f
        val usableWidth = boardWidth - leftMargin - rightMargin

        for (row in 0 until rows) {
            val numPins = row + 2
            val spacing = if (numPins > 1) usableWidth / (numPins - 1) else 0f
            val rowOffset = if (row % 2 == 1) spacing / 2f else 0f
            val y = topMargin + row * vertSpacing

            for (col in 0 until numPins) {
                val x = leftMargin - rowOffset + col * spacing
                if (x in (leftMargin * 0.5f)..(boardWidth - leftMargin * 0.5f)) {
                    result.add(PinState(x, y))
                }
            }
        }
        return result
    }

    fun launchBall(colorLong: Long = 0xFF8B5CF6L) {
        val startX = boardWidth / 2f + (Random.nextFloat() - 0.5f) * boardWidth * 0.15f
        _balls.add(
            BallState(
                id = _balls.size,
                x = startX,
                y = BALL_RADIUS + 5f,
                vx = (Random.nextFloat() - 0.5f) * 1.5f,
                vy = 1f,
                colorLong = colorLong
            )
        )
    }

    fun tick() {
        for (i in _balls.indices) {
            val ball = _balls[i]
            if (!ball.active) continue

            var newVy = ball.vy + gravity
            var newVx = ball.vx * 0.995f
            var newX = ball.x + newVx
            var newY = ball.y + newVy

            // Wall collisions
            if (newX - BALL_RADIUS < 0f) {
                newX = BALL_RADIUS
                newVx = abs(newVx) * bounce + 0.5f
            }
            if (newX + BALL_RADIUS > boardWidth) {
                newX = boardWidth - BALL_RADIUS
                newVx = -(abs(newVx) * bounce + 0.5f)
            }

            // Pin collisions — only first hit per tick to avoid tunnelling
            for (pin in pins) {
                val dx = newX - pin.x
                val dy = newY - pin.y
                val distSq = dx * dx + dy * dy
                if (distSq < COLLISION_DIST * COLLISION_DIST && distSq > 0.01f) {
                    val dist = sqrt(distSq)
                    val nx = dx / dist
                    val ny = dy / dist
                    val overlap = COLLISION_DIST - dist
                    newX += nx * overlap * 1.1f
                    newY += ny * overlap * 1.1f

                    val speedY = abs(newVy)
                    val deflect = if (Random.nextFloat() < 0.5f + (spread - 0.5f) * 0.4f) 1f else -1f
                    newVx = deflect * (speedY * bounce * 0.7f + 0.8f)
                    newVy = speedY * 0.35f + gravity * 2f
                    break
                }
            }

            // Bottom reached
            var newActive = true
            var newBucketIndex = ball.bucketIndex
            if (newY + BALL_RADIUS >= boardHeight - 5f) {
                newActive = false
                newY = boardHeight - BALL_RADIUS - 5f
                val bw = boardWidth / numBuckets
                newBucketIndex = (newX / bw).toInt().coerceIn(0, numBuckets - 1)
            }

            // Replace with a new immutable copy so Compose detects the change
            _balls[i] = ball.copy(
                x = newX, y = newY,
                vx = newVx, vy = newVy,
                active = newActive, bucketIndex = newBucketIndex
            )
        }
    }

    fun getResults(): Map<Int, Int> =
        _balls.filter { !it.active && it.bucketIndex >= 0 }
            .groupBy { it.bucketIndex }
            .mapValues { it.value.size }

    fun reset() = _balls.clear()

    fun simulateInstant(ballCount: Int): Map<Int, Int> {
        reset()
        repeat(ballCount) { launchBall(0xFF8B5CF6L) }
        var iterations = 0
        while (_balls.any { it.active } && iterations < 10000) {
            tick()
            iterations++
        }
        return getResults()
    }
}
