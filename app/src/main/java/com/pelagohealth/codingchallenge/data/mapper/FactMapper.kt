package com.pelagohealth.codingchallenge.data.mapper

import com.pelagohealth.codingchallenge.data.datasource.rest.APIFact
import com.pelagohealth.codingchallenge.domain.model.Fact
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FactMapper @Inject constructor() {
    fun mapToFact(from: APIFact): Fact {
        return Fact(
            text = from.text,
            url = from.sourceUrl,
            id = from.id
        )
    }
}