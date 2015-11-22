package me.reddev.osucelebrity.core.api;

import me.reddev.osucelebrity.core.SpectatorImpl;
import me.reddev.osucelebrity.osu.OsuStatus.Type;
import me.reddev.osucelebrity.osu.OsuStatus;
import org.mockito.internal.matchers.Contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.server.ServerConnector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;

import javax.ws.rs.core.UriBuilder;

import me.reddev.osucelebrity.AbstractJDOTest;
import me.reddev.osucelebrity.core.CoreSettings;
import me.reddev.osucelebrity.core.QueuedPlayer;
import me.reddev.osucelebrity.core.Spectator;
import me.reddev.osucelebrity.core.QueuedPlayer.QueueSource;
import me.reddev.osucelebrity.osu.Osu;
import me.reddev.osucelebrity.osuapi.MockOsuApi;
import me.reddev.osucelebrity.osuapi.OsuApi;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CoreApiApplicationTest extends AbstractJDOTest {
  @Mock
  Spectator spectator;
  @Mock
  CoreSettings coreSettings;
  @Mock
  Osu osu;

  OsuApi osuApi = new MockOsuApi();

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCurrentPlayerService() throws Exception {
    URI baseUri = UriBuilder.fromUri("http://localhost/").port(0).build();

    when(spectator.getCurrentPlayer(any())).thenReturn(
        new QueuedPlayer(osuApi.getUser("that player", pmf.getPersistenceManager(), 0),
            QueueSource.OSU, 0));

    when(spectator.getNextPlayer(any())).thenReturn(
        new QueuedPlayer(osuApi.getUser("next player", pmf.getPersistenceManager(), 0),
            QueueSource.OSU, 0));

    when(osu.getClientStatus()).thenReturn(new OsuStatus(Type.PLAYING, "that beatmap"));

    CurrentPlayerService cps =
        new CurrentPlayerService(pmf, spectator, coreSettings, osu, osuApi,
            Executors.newCachedThreadPool());
    QueueService queueService = new QueueService(pmf, spectator);
    CoreApiApplication apiServerApp = new CoreApiApplication(cps, queueService);

    Server apiServer =
        JettyHttpContainerFactory
            .createServer(baseUri, ResourceConfig.forApplication(apiServerApp));

    apiServer.start();
    int port = ((ServerConnector) apiServer.getConnectors()[0]).getLocalPort();

    when(spectator.getCurrentQueue(any())).thenReturn(
        Arrays.asList(new DisplayQueuePlayer("thisguy", "1:45", 12)));

    String result = readUrl(new URL("http://localhost:" + port + "/current"));

    assertThat(result, new Contains("\"name\":\"that player\""));
    assertThat(result, new Contains("\"nextPlayer\":\"next player\""));
    assertThat(result, new Contains("\"beatmap\":\"that beatmap\""));
    assertThat(result, new Contains("\"source\":\"queue\""));

    System.out.println(queueService.queue());
    result = readUrl(new URL("http://localhost:" + port + "/queue"));

    assertThat(result, new Contains("\"name\":\"thisguy\""));
    assertThat(result, new Contains("\"timeInQueue\":\"1:45\""));
    assertThat(result, new Contains("\"votes\":12"));

    apiServer.stop();
  }

  private String readUrl(URL url) throws IOException, UnsupportedEncodingException {
    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
    try {
      assertEquals(200, httpCon.getResponseCode());

      InputStream inputStream = httpCon.getInputStream();
      try {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int len; (len = inputStream.read(buf)) > 0;) {
          baos.write(buf, 0, len);
        }

        return baos.toString("UTF-8");
      } finally {
        inputStream.close();
      }
    } finally {
      httpCon.disconnect();
    }
  }
}
