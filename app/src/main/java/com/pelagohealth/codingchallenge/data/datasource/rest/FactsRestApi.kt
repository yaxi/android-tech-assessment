package com.pelagohealth.codingchallenge.data.datasource.rest

import com.pelagohealth.codingchallenge.data.Resource
import retrofit2.Response
import retrofit2.http.GET

/**
 * REST API for fetching random facts.
 */
interface FactsRestApi {

    @GET("facts/random")
    suspend fun getFact(): Response<APIFact>
}