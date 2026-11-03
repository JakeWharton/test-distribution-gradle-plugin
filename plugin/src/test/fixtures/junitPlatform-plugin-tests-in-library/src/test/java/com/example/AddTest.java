package com.example;

import static com.example.Adder.add;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public final class AddTest {
	@Test
	public void simple() {
		assertEquals(5, add(3, 2));
	}
}
