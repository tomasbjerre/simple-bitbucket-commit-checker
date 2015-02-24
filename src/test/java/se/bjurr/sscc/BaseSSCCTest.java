package se.bjurr.sscc;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Before;
import org.mockito.Mock;

import se.bjurr.sscc.ChangeSetsService;
import se.bjurr.sscc.SsccPreReceiveRepositoryHook;
import se.bjurr.sscc.util.RefChangeBuilder;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.google.common.io.Resources;

public class BaseSSCCTest {
	@Mock
	private ChangeSetsService changeSetsService;
	protected SsccPreReceiveRepositoryHook hook;
	@Mock
	private HookResponse hookResponse;

	private final OutputStream outputAll = newOutputStream();
	private final PrintWriter printWriterReject = new PrintWriter(outputAll);
	private final PrintWriter printWriterStandard = new PrintWriter(outputAll);
	@Mock
	private RepositoryHookContext repositoryHookContext;

	@Before
	public void before() {
		initMocks(this);
		when(hookResponse.out()).thenReturn(printWriterStandard);
		when(hookResponse.err()).thenReturn(printWriterReject);
		this.hook = new SsccPreReceiveRepositoryHook(changeSetsService);
	}

	protected RefChangeBuilder refChangeBuilder() {
		return RefChangeBuilder.refChangeBuilder(repositoryHookContext.getRepository(), changeSetsService);
	}

	protected void assertResponseEquals(String filename, List<RefChange> refChanges) throws IOException {
		runRefChanges(refChanges);
		assertEquals(Resources.toString(getResource(filename), UTF_8), outputAll.toString());
	}

	protected void assertTrimmedResponseEquals(String filename, List<RefChange> refChanges) throws IOException {
		runRefChanges(refChanges);
		assertEquals(Resources.toString(getResource(filename), UTF_8).trim(), outputAll.toString().trim());
	}

	private OutputStream newOutputStream() {
		return new OutputStream() {
			private final StringBuilder string = new StringBuilder();

			@Override
			public String toString() {
				return this.string.toString();
			}

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}
		};
	}

	private void runRefChanges(List<RefChange> refChanges) {
		hook.setChangesetsService(changeSetsService);
		hook.onReceive(repositoryHookContext, refChanges, hookResponse);
		printWriterReject.flush();
		printWriterStandard.flush();
	}
}
