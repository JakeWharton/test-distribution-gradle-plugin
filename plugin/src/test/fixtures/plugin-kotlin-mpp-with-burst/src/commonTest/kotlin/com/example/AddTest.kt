package com.example

import app.cash.burst.Burst
import kotlin.test.Test
import kotlin.test.assertEquals

@Burst
class AddTest(
	private val strategy: Strategy,
) {
	@Test fun simple() {
		assertEquals(5, strategy.add(3, 2))
	}

	enum class Strategy {
		Language {
			override fun add(a: Int, b: Int): Int = a + b
		},
		Library {
			override fun add(a: Int, b: Int): Int = com.example.add(a, b)
		},
		;

		abstract fun add(a: Int, b: Int): Int
	}
}
