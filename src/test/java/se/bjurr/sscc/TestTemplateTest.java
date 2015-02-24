package se.bjurr.sscc;

import static com.google.common.collect.Lists.newArrayList;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TestTemplateTest extends BaseSSCCTest {

	@Before
	@Override
	public void before() {
		super.before();
		hook.setRESPONSE_REJECT_TXT("test_response_reject.txt");
		hook.setRESPONSE_SUCCESS_TXT("test_response_success.txt");
	}

	@Test
	public void testThatRejectResponseLooksGood() throws IOException {
		assertResponseEquals("testThatRejectResponseLooksGood.txt" //
				, newArrayList(refChangeBuilder() //
						.withChangeSet(changeSetBuilder() //
								.withId("1") //
								.withMessage("fixing stuff") //
								.build()) //
								.withChangeSet(changeSetBuilder() //
										.withId("2") //
										.withMessage("fix") //
										.build()) //
										.build()));
	}

	@Test
	public void testThatSuccessResponseLooksGood() throws IOException {
		assertResponseEquals("testThatSuccessResponseLooksGood.txt" //
				, newArrayList(refChangeBuilder() //
						.withChangeSet(changeSetBuilder() //
								.withId("1") //
								.withMessage("SB-5678 fixing stuff") //
								.build()) //
								.build()));
	}
}
