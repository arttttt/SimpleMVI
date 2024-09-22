package com.arttttt.simplemvi.utils

/**
 * JavaScript is a single threaded language, so this is always true
 */
public actual fun isMainThread(): Boolean {
    return true
}