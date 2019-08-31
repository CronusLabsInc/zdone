package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimeProgress(
        @JsonProperty("minutes_allocated")
        val timeAllocatedToday: Int,
        @JsonProperty("minutes_completed_today")
        val timeCompletedToday: Int,
        @JsonProperty("maximum_minutes_per_day")
        val maximumMinutesPerDay: Int)
