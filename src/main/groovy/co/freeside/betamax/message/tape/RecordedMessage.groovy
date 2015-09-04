package co.freeside.betamax.message.tape

import co.freeside.betamax.encoding.AbstractEncoder
import co.freeside.betamax.encoding.DeflateEncoder
import co.freeside.betamax.encoding.GzipEncoder
import co.freeside.betamax.encoding.NoOpEncoder
import co.freeside.betamax.message.AbstractMessage
import co.freeside.betamax.message.Message
import org.apache.http.HttpHeaders

abstract class RecordedMessage extends AbstractMessage implements Message {

	Map<String, String> headers = [:]
	def body

	final void addHeader(String name, String value) {
		if (headers[name]) {
			headers[name] = "${headers[name]}, $value".toString()
		} else {
			headers[name] = value
		}
	}

	final boolean hasBody() {
		body
	}

	@Override
	final Reader getBodyAsText() {
		def string = body instanceof String ? body : getEncoder().decode(bodyAsBinary, charset)
		new StringReader(string)
	}

	final InputStream getBodyAsBinary() {
		byte[] bytes = body instanceof String ? getEncoder().encode(body, charset) : body
		new ByteArrayInputStream(bytes)
	}

	private AbstractEncoder getEncoder() {
		switch (getHeader(HttpHeaders.CONTENT_ENCODING)) {
                        case 'gzip':
                                return new GzipEncoder()
			case 'deflate':
                		return new DeflateEncoder()
			default:
                		return new NoOpEncoder()
		}
	}
}
