package com.pelagohealth.codingchallenge.data.datasource.rest

import retrofit2.http.GET

/**
 * REST API for fetching random facts.
 */
interface FactsRestApi {

    @GET("facts/random")
    suspend fun getFact(): APIFact
}