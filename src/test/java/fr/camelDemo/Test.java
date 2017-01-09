package fr.camelDemo;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
	static final Logger LOG = LoggerFactory.getLogger(Test.class);

	@Before
	public void init() {
		LOG.info("Init");
	}

	@After
	public void finish() {
		LOG.info("Finish");
	}

	@org.junit.Test
	public void test() {
		LOG.info("Start test...");
		assertEquals(true, true);
		LOG.info("End test.");
	}
}
