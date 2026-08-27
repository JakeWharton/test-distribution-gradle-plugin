package com.jakewharton.testdistribution

internal data class AgpVersion(
	val major: Int,
	val minor: Int = 0,
	val patch: Int = 0,
	val prerelease: String? = null,
) : Comparable<AgpVersion> {
	override fun toString() = buildString {
		append("Android Gradle Plugin ")
		append(major)
		append('.')
		append(minor)
		append('.')
		append(patch)
		if (prerelease != null) {
			append('-')
			append(prerelease)
		}
	}

	override fun compareTo(other: AgpVersion) = comparator.compare(this, other)

	companion object {
		fun parse(version: String): AgpVersion {
			val majorStart = 0
			val majorEnd = version.indexOf('.', startIndex = majorStart).checkFound()
			val minorStart = majorEnd + 1
			val minorEnd = version.indexOf('.', startIndex = minorStart).checkFound()
			val patchStart = minorEnd + 1
			var patchEnd = version.indexOf('-', startIndex = patchStart)
			val prerelease = if (patchEnd != -1) {
				version.substring(patchEnd + 1)
			} else {
				patchEnd = version.length
				null
			}
			return AgpVersion(
				major = version.substring(majorStart, majorEnd).toInt(),
				minor = version.substring(minorStart, minorEnd).toInt(),
				patch = version.substring(patchStart, patchEnd).toInt(),
				prerelease = prerelease,
			)
		}

		private fun Int.checkFound() = apply {
			check(this != -1)
		}

		private val comparator = compareBy(
			AgpVersion::major,
			AgpVersion::minor,
			AgpVersion::patch,
		).thenComparing(
			compareBy(nullsLast(), AgpVersion::prerelease),
		)
	}
}
