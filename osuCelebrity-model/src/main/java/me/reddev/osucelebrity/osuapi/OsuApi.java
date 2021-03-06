package me.reddev.osucelebrity.osuapi;

import me.reddev.osucelebrity.PassAndReturnNonnull;
import me.reddev.osucelebrity.osu.OsuIrcUser;
import me.reddev.osucelebrity.osu.OsuUser;
import me.reddev.osucelebrity.osu.PlayerActivity;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jdo.PersistenceManager;

@PassAndReturnNonnull
public interface OsuApi {
  /**
   * Get a user object from the osu api.
   * 
   * @param userid the user's id
   * @param pm the requests's persistence manager
   * @param maxAge maximum age of the returned object. If there is a cached object which is younger
   *        than maximum age or maxAge is <= 0, it may be returned.
   * @return null if the user does not exist
   */
  @CheckForNull
  OsuUser getUser(@UserId int userid, PersistenceManager pm, long maxAge)
      throws IOException;

  /**
   * Get a user object from the osu api.
   * 
   * @param userName the user's name
   * @param pm the requests's persistence manager
   * @param maxAge maximum age of the returned object. If there is a cached object which is younger
   *        than maximum age or maxAge is <= 0, it may be returned.
   * @return null if the user does not exist
   */
  @CheckForNull
  OsuUser getUser(String userName, PersistenceManager pm, long maxAge)
      throws IOException;

  /**
   * Get an irc user object from the osu api.
   * 
   * @param ircUserName the user's name on the osu irc chat
   * @param pm the requests's persistence manager
   * @param maxAge maximum age of the returned object. If there is a cached object which is younger
   *        than maximum age or maxAge is <= 0, it may be returned.
   * @return null if the user does not exist
   */
  @CheckForNull
  OsuIrcUser getIrcUser(String ircUserName, PersistenceManager pm, long maxAge) throws IOException;

  /**
   * Get a user's data for a specific mode.
   * 
   * @param userid user id
   * @param gameMode game mode
   * @param pm the requests's persistence manager
   * @param maxAge maximum age of the returned object. If there is a cached object which is younger
   *        than maximum age or maxAge is <= 0, it may be returned.
   * @return null if the user does not exist
   */
  @CheckForNull
  ApiUser getUserData(@UserId int userid, @GameMode int gameMode, PersistenceManager pm, 
      long maxAge) throws IOException;
  
  /**
   * Get a user's recent activity.
   * 
   * @param user target user. game mode is taken into account.
   * @param pm the requests's persistence manager
   * @param maxAge maximum age of the returned object. If there is a cached object which is younger
   *        than maximum age or maxAge is <= 0, it may be returned.
   * @return the game mode specific recent activity
   */
  @Nonnull
  PlayerActivity getPlayerActivity(ApiUser user, PersistenceManager pm, long maxAge)
      throws IOException;
}
