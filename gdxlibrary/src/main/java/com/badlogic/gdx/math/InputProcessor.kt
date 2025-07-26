package com.badlogic.gdx.math

interface InputProcessor {
    /** Called when a key was pressed */
    fun keyDown(keycode: Int): Boolean

    /** Called when a key was released */
    fun keyUp(keycode: Int): Boolean

    /** Called when a key was typed */
    fun keyTyped(character: Char): Boolean

    /** Called when the screen was touched or a mouse button was pressed. */
    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean

    /** Called when a finger was lifted or a mouse button was released. */
    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean

    /** Called when a finger or the mouse was dragged. */
    fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean

    /** Called when the mouse was moved without any buttons being pressed. */
    fun mouseMoved(screenX: Int, screenY: Int): Boolean

    /** Called when the mouse wheel was scrolled. */
    fun scrolled(amount: Int): Boolean
}
