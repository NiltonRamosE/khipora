package com.nramosds.khipora.domain.exception


class ClosingSaveException(
    val requiresReview: Boolean,
    cause: Exception,
) : Exception(cause)
