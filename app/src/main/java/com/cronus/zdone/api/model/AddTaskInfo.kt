package com.cronus.zdone.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddTaskInfo(
    @JsonProperty
    val name: String,
    @get:JsonProperty("due_date")
    val dueDate: String,
    @get:JsonProperty("length_minutes")
    val lengthMins: Long)