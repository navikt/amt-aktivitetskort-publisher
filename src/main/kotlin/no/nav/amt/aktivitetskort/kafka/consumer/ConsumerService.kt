package no.nav.amt.aktivitetskort.kafka.consumer

import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ConsumerService {

	private val log = LoggerFactory.getLogger(javaClass)
	fun consumeDeltaker(id: UUID, deltaker: DeltakerDto?) {
		if (deltaker == null) {
			// Skal vi slette noe?
			log.info("Mottok tombstone for deltaker $id")
			return
		}
		// service.kanskjeOpprettAktivitetskort(deltaker)
		log.info("Konsumerte melding med deltaker $id")
	}

	fun consumeDeltakerliste(id: UUID, deltakerlisteDto: DeltakerlisteDto?) {
		if (deltakerlisteDto == null) {
			// Skal vi slette noe?
			log.info("Mottok tombstone for deltakerliste $id")
		}
		// service.kanskjeOpprettAktivitetskort(deltakerlisteDto)
		log.info("Konsumerte melding med deltakerliste $id")
	}

	fun consumeArrangor(id: UUID, arrangor: ArrangorDto?) {
		if (arrangor == null) {
			// Ikke slett, behold historisk navn i kort?
			log.info("Mottok tombstone for arrangør $id")
			return
		}

		// service.kanskjeOpprettAktivitetskort(arrangor)
		log.info("Konsumerte melding med arrangør $id")
	}
}
