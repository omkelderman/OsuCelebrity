package me.reddev.osucelebrity.core.api;

import static org.mockito.Mockito.*;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import me.reddev.osucelebrity.AbstractJDOTest;
import me.reddev.osucelebrity.core.QueuedPlayer;
import me.reddev.osucelebrity.core.Spectator;
import me.reddev.osucelebrity.core.QueuedPlayer.QueueSource;
import me.reddev.osucelebrity.osuapi.MockOsuApi;
import me.reddev.osucelebrity.osuapi.OsuApi;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CoreApiApplicationTest extends AbstractJDOTest {

  public static void main(String[] args) throws Exception {
    CoreApiApplicationTest coreAPIServerTest = new CoreApiApplicationTest();
    MockitoAnnotations.initMocks(coreAPIServerTest);
    coreAPIServerTest.createDatastore();
    coreAPIServerTest.main();
  }

  @Mock
  Spectator spectator;

  OsuApi osuApi = new MockOsuApi();

  void main() throws Exception {
    URI baseUri = UriBuilder.fromUri("http://localhost/").port(1666).build();

    when(spectator.getCurrentPlayer(any())).thenReturn(
        new QueuedPlayer(osuApi.getUser("that player", 0, pmf.getPersistenceManager(), 0),
            QueueSource.OSU, 0));

    CoreApiApplication apiServerApp =
        new CoreApiApplication(new CurrentPlayerService(pmf, spectator));

    Server apiServer =
        JettyHttpContainerFactory
            .createServer(baseUri, ResourceConfig.forApplication(apiServerApp));

    apiServer.start();
  }
}