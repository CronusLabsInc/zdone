package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddTaskInfo(
    @JsonProperty
    val name: String,
    @JsonProperty("due_date")
    val dueDate: LocalDate,
    @JsonProperty("length_minutes")
    val lengthMins: Long)