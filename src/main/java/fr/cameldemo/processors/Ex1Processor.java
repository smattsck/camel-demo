package fr.cameldemo.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ex1Processor implements Processor {
	/** Logger */
	static final Logger LOG = LoggerFactory.getLogger(Ex1Processor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		LOG.info(exchange.toString());
	}

}
