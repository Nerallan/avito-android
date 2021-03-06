package com.avito.truth

import com.google.common.truth.ThrowableSubject

fun ThrowableSubject.checkCausesDeeply(check: ThrowableSubject.() -> Unit) {
    try {
        check(this)
    } catch (e: java.lang.AssertionError) {
        hasCauseThat().checkCausesDeeply(check)
    }
}
