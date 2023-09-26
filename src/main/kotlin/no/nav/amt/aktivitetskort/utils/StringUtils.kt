package no.nav.amt.aktivitetskort.utils

val FORKORTELSER_MED_STORE_BOKSTAVER = listOf(
	"as",
)

val ORD_MED_SMA_BOKSTAVER = listOf(
	"i",
	"og",
)

fun toTitleCase(tekst: String): String {
	return tekst.lowercase().split(' ').joinToString(" ") {
		if (it in FORKORTELSER_MED_STORE_BOKSTAVER) {
			it.uppercase()
		} else if (it in ORD_MED_SMA_BOKSTAVER) {
			it
		} else {
			it.replaceFirstChar(Char::uppercaseChar)
		}
	}
}
