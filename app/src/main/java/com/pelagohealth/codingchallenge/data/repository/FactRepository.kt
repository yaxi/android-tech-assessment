package com.pelagohealth.codingchallenge.data.repository

import com.pelagohealth.codingchallenge.data.FactMapper
import com.pelagohealth.codingchallenge.data.datasource.rest.FactsRestApi
import com.pelagohealth.codingchallenge.domain.model.Fact
import javax.inject.Inject

/**
 * Repository providing random facts.
 */
class FactRepository @Inject constructor(
    private val api: FactsRestApi,
    private val mapper: FactMapper
) {
    
    suspend fun get(): Fact {
        return mapper.mapToFact(from = api.getFact())
    }
}