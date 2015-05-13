package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeWriterSpec extends Specification {

	Recorder recorder = Mock(Recorder)
	TapeWriter handler = new TapeWriter(recorder)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

	void 'writes chained response to tape before returning it'() {
		given:
		def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		handler << nextHandler

		and:
		def tape = Mock(Tape)
		recorder.tape >> tape
		tape.isWritable() >> true

		when:
		def result = handler.handle(request)

		then:
		result.is(response)

		and:
		1 * tape.record(request, response)
	}

	void 'throws an exception if there is no tape inserted'() {
		given:
		recorder.tape >> null

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
	}

	void 'throws an exception if the tape is not writable'() {
		given:
		def tape = Mock(Tape)
		recorder.tape >> tape
		tape.isWritable() >> false

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
	}

        void 'behaves normally in reconciliation mode if live response matched taped response'() {
                given:
                def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		handler << nextHandler

                and:
                def tape = Mock(Tape)
                recorder.tape >> tape
                tape.getMode() >> TapeMode.RECONCILE
                tape.seek(request) >> true
                tape.play(request) >> response

                when:
                handler.handle(request)

                then:
                def result = handler.handle(request)

		then:
		result.is(response)

		and:
		0 * tape.record(request, response)
                0 * reconciler.record(tape, request, response)
        }

  //void 'writes reconciliation error and throws exception if live response didn\'t match taped response'() {
  //
  //      }
}
