package com.example;

import static com.example.Adder.add;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class AddTest {
	@Test
	public void simple() {
		assertEquals(5, add(3, 2));
	}
}
