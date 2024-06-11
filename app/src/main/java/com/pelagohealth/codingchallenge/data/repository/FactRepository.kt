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
    
    suspend fun getFact(): Resource<Fact> {
        val response = api.getFact()
        if (response.isSuccessful && response.body() != null) {
            val fact = mapper.mapToFact(from = response.body()!!)
            return Resource.Success(data = fact)
        } else {
            return Resource.Error(error = Throwable(message = response.code().toString()))
        }
    }
}