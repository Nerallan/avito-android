package com.avito.utils.gradle

/**
 * Refer to official rules: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/
 */
fun String.toValidKubernetesName(): String = replace("_", "-").toLowerCase()
