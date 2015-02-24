package se.bjurr.sscc;

import static com.google.common.collect.Lists.newArrayList;
import static se.bjurr.sscc.data.SSCCChangeSetBuilder.changeSetBuilder;

import java.io.IOException;

import org.junit.Test;

public class ProductionTemplateTest extends BaseSSCCTest {

	/**
	 * Other test cases test edge cases. This is intended to be used as an
	 * example of how the entire response looks like. And again, dont test edge
	 * cases like this, that will result in alot of work if the template is
	 * changed.
	 */
	@Test
	public void testThatRejectResponseLooksGood() throws IOException {
		assertResponseEquals("testProdThatRejectResponseLooksGood.txt" //
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

	/**
	 * Other test cases test edge cases. This is intended to be used as an
	 * example of how the entire response looks like. And again, dont test edge
	 * cases like this, that will result in alot of work if the template is
	 * changed.
	 */
	@Test
	public void testThatSuccessResponseLooksGood() throws IOException {
		assertResponseEquals("testProdThatSuccessResponseLooksGood.txt" //
				, newArrayList(refChangeBuilder() //
						.withChangeSet(changeSetBuilder() //
								.withId("1") //
								.withMessage("SB-5678 fixing stuff") //
								.build()) //
								.build()));
	}
}
