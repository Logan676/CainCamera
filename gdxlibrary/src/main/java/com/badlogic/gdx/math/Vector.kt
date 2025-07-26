package com.badlogic.gdx.math

/** Encapsulates a general vector. Allows chaining operations by returning a reference to itself in all modification methods. */
interface Vector<T : Vector<T>> {
    /** @return a copy of this vector */
    fun cpy(): T

    /** @return The euclidean length */
    fun len(): Float

    /** Faster than [len] as it avoids calculating a square root. */
    fun len2(): Float

    /** Limits the length of this vector. */
    fun limit(limit: Float): T

    /** Limits the length squared of this vector. */
    fun limit2(limit2: Float): T

    /** Sets the length of this vector. */
    fun setLength(len: Float): T

    /** Sets the length squared of this vector. */
    fun setLength2(len2: Float): T

    /** Clamps this vector's length to the given range. */
    fun clamp(min: Float, max: Float): T

    /** Sets this vector from the given vector. */
    fun set(v: T): T

    /** Subtracts the given vector from this vector. */
    fun sub(v: T): T

    /** Normalizes this vector. */
    fun nor(): T

    /** Adds the given vector to this vector. */
    fun add(v: T): T

    /** @return The dot product between this and the other vector. */
    fun dot(v: T): Float

    /** Scales this vector by a scalar. */
    fun scl(scalar: Float): T

    /** Scales this vector by another vector. */
    fun scl(v: T): T

    /** @return the distance between this and the other vector */
    fun dst(v: T): Float

    /** Faster than [dst] as it avoids calculating a square root. */
    fun dst2(v: T): Float

    /** Linearly interpolates between this vector and the target vector. */
    fun lerp(target: T, alpha: Float): T

    /** Sets this vector to the unit vector with a random direction. */
    fun setToRandomDirection(): T

    /** @return Whether this vector is a unit length vector */
    fun isUnit(): Boolean

    /** @return Whether this vector is a unit length vector within the given margin. */
    fun isUnit(margin: Float): Boolean

    /** @return Whether this vector is a zero vector */
    fun isZero(): Boolean

    /** @return Whether the length of this vector is smaller than the given margin */
    fun isZero(margin: Float): Boolean

    /** @return true if this vector is in line with the other vector */
    fun isOnLine(other: T, epsilon: Float): Boolean
    fun isOnLine(other: T): Boolean

    /** @return true if this vector is collinear with the other vector */
    fun isCollinear(other: T, epsilon: Float): Boolean
    fun isCollinear(other: T): Boolean

    /** @return true if this vector is opposite collinear with the other vector */
    fun isCollinearOpposite(other: T, epsilon: Float): Boolean
    fun isCollinearOpposite(other: T): Boolean

    /** @return Whether this vector is perpendicular with the other vector. */
    fun isPerpendicular(other: T): Boolean
    fun isPerpendicular(other: T, epsilon: Float): Boolean

    /** @return Whether this vector has similar direction compared to the other vector. */
    fun hasSameDirection(other: T): Boolean

    /** @return Whether this vector has opposite direction compared to the other vector. */
    fun hasOppositeDirection(other: T): Boolean

    /** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing. */
    fun epsilonEquals(other: T, epsilon: Float): Boolean

    /** First scale a supplied vector, then add it to this vector. */
    fun mulAdd(v: T, scalar: Float): T
    fun mulAdd(v: T, mulVec: T): T

    /** Sets the components of this vector to 0 */
    fun setZero(): T
}
