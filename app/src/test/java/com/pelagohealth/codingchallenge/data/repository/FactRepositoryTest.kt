package com.pelagohealth.codingchallenge.data.repository

import com.pelagohealth.codingchallenge.data.datasource.rest.APIFact
import com.pelagohealth.codingchallenge.data.datasource.rest.FactsRestApi
import com.pelagohealth.codingchallenge.data.mapper.FactMapper
import com.pelagohealth.codingchallenge.domain.model.Fact
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class FactRepositoryTest {

    private lateinit var repository: FactRepository
    private val api: FactsRestApi = mockk()
    private val mapper = FactMapper()

    @Before
    fun setUp() {
        repository = FactRepository(
            api = api,
            mapper = mapper
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `getFact expect successful response`() {
        val expected = Fact(
            id = "evertitur",
            text = "aliquid",
            url = "https://search.yahoo.com/search?p=cubilia"
        )
        runTest {
            coEvery { api.getFact() }.returns(
                Response.success(
                    /* body = */ APIFact(
                        id = "evertitur",
                        text = "aliquid",
                        sourceUrl = "https://search.yahoo.com/search?p=cubilia"
                    )
                )
            )
            val actual = repository.getFact()
            assertEquals(expected, (actual as Resource.Success).data)
        }
    }

    @Test
    fun `getFact expect error response`() {
        val error = Throwable("404")
        runTest {
            coEvery { api.getFact() }.returns(
                Response.error(
                    404,
                    "dummy error".toResponseBody()
                )
            )
            val actual = repository.getFact()
            assertEquals(error.message, (actual as Resource.Error).error?.message)
        }
    }

    @Test
    fun `getFact with empty body expect error response`() {
        val error = Throwable("200")
        runTest {
            coEvery { api.getFact() }.returns(
                Response.success(null)
            )
            val actual = repository.getFact()
            assertEquals(error.message, (actual as Resource.Error).error?.message)
        }
    }
}