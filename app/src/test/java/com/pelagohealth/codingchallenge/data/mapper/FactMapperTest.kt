package com.pelagohealth.codingchallenge.data.mapper

import com.pelagohealth.codingchallenge.data.datasource.rest.APIFact
import com.pelagohealth.codingchallenge.domain.model.Fact
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class FactMapperTest {

    private lateinit var mapper: FactMapper

    @Before
    fun setup() {
        mapper = FactMapper()
    }

    @Test
    fun `mapToFact expect Fact model`() {
        val expected = Fact(
            text = "ignota",
            url = "https://www.google.com/#q=laudem",
            id = "maiorum"
        )
        val actual = mapper.mapToFact(APIFact(
            text = "ignota",
            sourceUrl = "https://www.google.com/#q=laudem",
            id = "maiorum"
        ))

        assertEquals(expected, actual)
    }
}