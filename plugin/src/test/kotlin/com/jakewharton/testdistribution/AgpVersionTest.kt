package com.jakewharton.testdistribution

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import org.junit.Test

class AgpVersionTest {
	@Test fun parsing() {
		assertThat(AgpVersion.parse("9.0.0")).isEqualTo(AgpVersion(9))
		assertThat(AgpVersion.parse("9.1.0")).isEqualTo(AgpVersion(9, 1))
		assertThat(AgpVersion.parse("9.1.4")).isEqualTo(AgpVersion(9, 1, 4))
		assertThat(AgpVersion.parse("9.1.4-sup")).isEqualTo(AgpVersion(9, 1, 4, "sup"))
	}

	@Test fun comparison() {
		assertThat(AgpVersion(9)).isGreaterThan(AgpVersion(8))
		assertThat(AgpVersion(8, 1)).isGreaterThan(AgpVersion(8, 0))
		assertThat(AgpVersion(8, 0, 1)).isGreaterThan(AgpVersion(8, 0, 0))
		assertThat(AgpVersion(8, 0, 0)).isGreaterThan(AgpVersion(8, 0, 0, "alpha02"))
		assertThat(AgpVersion(8, 0, 0, "alpha02")).isGreaterThan(AgpVersion(8, 0, 0, "alpha01"))
	}
}
