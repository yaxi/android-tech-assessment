package com.pelagohealth.codingchallenge.data.repository

import com.pelagohealth.codingchallenge.data.mapper.FactMapper
import com.pelagohealth.codingchallenge.data.datasource.rest.FactsRestApi
import com.pelagohealth.codingchallenge.domain.model.Fact
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository providing random facts.
 */
@Singleton
class FactRepository @Inject constructor(
    private val api: FactsRestApi,
    private val mapper: FactMapper
) {
    
    suspend fun getFact(): Fact {
        return mapper.mapToFact(from = api.getFact())
    }
}