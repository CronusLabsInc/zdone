package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TimeProgress(
        @JsonProperty("minutes_allocated")
        val timeAllocatedToday: Int,
        @JsonProperty("minutes_completed_today")
        val timeCompletedToday: Int)
