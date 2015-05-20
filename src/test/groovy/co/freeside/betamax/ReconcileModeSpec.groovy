package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Stepwise
class ReconcileModeSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	@Shared RESTClient http = new BetamaxRESTClient()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@Betamax(tape = 'reconcilemode', mode = WRITE_ONLY)
	void 'proxy makes a real HTTP request the first time it gets a request for a URI'() {
		when:
		HttpResponseDecorator response = http.get(uri: endpoint.url)

		then:
                response.status == HTTP_OK
                response.getFirstHeader(VIA)?.value == 'Betamax'
		recorder.tape.size() == 1
	}

        @Ignore('Not implemented yet')
        @Betamax(tape = 'reconcilemode', mode = RECONCILE)
        void 'Reconcile mode plays response and records no errors when live response matches tape'()  {
          	when:
		HttpResponseDecorator response = http.get(uri: endpoint.url)

		then:
                response.status == HTTP_OK
                reconciliationErrorFileContents() == []
        }

        @Ignore('Not implemented yet')
  void 'Reconcile mode records reconciliation error tape when live response doesn\'t match taped response for the matching request'() {
               given:
               endpoint.start(HelloHandler) // Gives different response for same uri

               when:
               HttpResponseDecorator response = http.get(uri: endpoint.url)

               then:
               response.status == HTTP_OK
               reconciliationErrorFileContents().size == 1
               // Maybe verify some of the yaml
        }

        @Ignore('Not implemented yet')
        void 'Reconcile mode behaves identically to read-only mode if no matching request found'()   {
          // Spin up svr, make request to /otherpath, which svr responds to, but no
          // recorded request is on tape.  Should get an error.
        }

        def reconciliationErrorFileContents() {}
  }
