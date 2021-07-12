package no.nav.brevserver.command;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.jms.Message;

import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.vo.MessageVO;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for CommandFactory
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public class CommandFactoryTest {

    @Mock
    private Message messageMock;

    private static CommandFactory commandFactory;

    @BeforeClass
    public static void setUpBeforeClass() {
        commandFactory = CommandFactory.getInstance();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateBestillBrevCommandForBidrag() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.BI_BREVSERVER_ONLINEBREV, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(BestillBrevCommand.class));
    }

    @Test
    public void shouldCreateArkiverBrevCommandForMottakBidragOnline() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.BI_BREVSERVER_MOTTAK_ONLINE, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(ArkiverBrevCommand.class));
    }

    @Test
    public void shouldCreateArkiverBrevCommandForMottakBidragArkiv() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.BI_BREVSERVER_MOTTAK_ARKIV, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(ArkiverBrevCommand.class));
    }

    @Test
    public void shouldCreateBestillBrevCommandForPensjon() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.PE_BREVSERVER_ONLINEBREV, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(PEBestillBrevCommand.class));
    }

    @Test
    public void shouldCreateBestillBrevCommandForMottakPensjonOnline() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.PE_BREVSERVER_MOTTAK_ONLINE, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(PEArkiverBrevCommand.class));
    }

    @Test
    public void shouldCreateBestillBrevCommandForMottakPensjonArkiv() throws Exception {
        AbstractCommand command = commandFactory.createCommand(QueueType.PE_BREVSERVER_MOTTAK_ONLINE, new MessageVO(
                messageMock));

        assertThat(command, instanceOf(PEArkiverBrevCommand.class));
    }
}
