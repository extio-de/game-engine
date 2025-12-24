package de.extio.game_engine.steamworks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamApps;
import com.codedisaster.steamworks.SteamAuth.AuthSessionResponse;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.OverlayDialog;
import com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode;
import com.codedisaster.steamworks.SteamFriends.PersonaChange;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.ChatEntryType;
import com.codedisaster.steamworks.SteamMatchmaking.ChatMemberStateChange;
import com.codedisaster.steamworks.SteamMatchmaking.ChatRoomEnterResponse;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNativeHandle;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUGC;
import com.codedisaster.steamworks.SteamUGC.ItemInstallInfo;
import com.codedisaster.steamworks.SteamUGC.ItemUpdateInfo;
import com.codedisaster.steamworks.SteamUGC.ItemUpdateStatus;
import com.codedisaster.steamworks.SteamUGCCallback;
import com.codedisaster.steamworks.SteamUGCDetails;
import com.codedisaster.steamworks.SteamUGCQuery;
import com.codedisaster.steamworks.SteamUGCUpdateHandle;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUserStatsCallback;
import com.codedisaster.steamworks.SteamUtils;
import com.codedisaster.steamworks.SteamUtilsCallback;

public final class SteamworksConnectorImpl extends SteamworksConnector {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int CALLBACK_RATE_MS = 66;
	
	private boolean active;
	
	private long lastCallbacks = System.currentTimeMillis();
	
	private final BlockingQueue<Runnable> callQueue = new LinkedBlockingQueue<>();
	
	private volatile boolean inCall;
	
	private SteamFriends steamFriends;
	
	private SteamUserStats steamUserStats;
	
	private SteamMatchmaking steamMatchmaking;
	
	private SteamUtils steamUtils;
	
	private SteamApps steamApps;
	
	private SteamUGC steamUGC;
	
	private SteamUser steamUser;
	
	private volatile SteamID lobby;
	
	private volatile String lobbyQuickJoinCode;
	
	private volatile SteamPublishedFileID publishedItemFileID;
	
	private volatile String createItemError;
	
	private volatile String updateItemError;
	
	private volatile SteamUGCUpdateHandle updateItemHandle;
	
	public SteamworksConnectorImpl() {
		try {
			LOGGER.info("Loading steamworks");
			
			SteamAPI.loadLibraries(); // After upgrading to 1.10.0+, a library loader wrapper impl is needed. See https://code-disaster.github.io/steamworks4j/getting-started.html
			if (SteamAPI.init()) {
				this.active = true;
				
				this.steamFriends = new SteamFriends(new FriendsCallback(this));
				this.steamUserStats = new SteamUserStats(new UserStatsCallback(this));
				this.steamMatchmaking = new SteamMatchmaking(new MatchmakingCallback(this));
				this.steamUtils = new SteamUtils(new UtilsCallback(this));
				this.steamApps = new SteamApps();
				// this.steamUGC = new SteamUGC(new UGCCallback(this));
				this.steamUser = new SteamUser(new UserCallback(this));
			}
			
			LOGGER.info("SteamworksConnector active: " + this.active);
		}
		catch (final Throwable t) {
			LOGGER.warn("Cannot initialize steamworks", t);
		}
	}
	
	@Override
	public void runCallbacks() {


		// TODO: Executor thread


		if (!this.active) {
			return;
		}
		final long now = System.currentTimeMillis();
		if (System.currentTimeMillis() - this.lastCallbacks >= CALLBACK_RATE_MS) {
			this.lastCallbacks = now;
			
			try {
				if (SteamAPI.isSteamRunning()) {
					SteamAPI.runCallbacks();
					
					if (!this.inCall) {
						final Runnable call = this.callQueue.poll();
						if (call != null) {
							this.inCall = true;
							call.run();
						}
					}
				}
			}
			catch (final Throwable t) {
				LOGGER.warn("Error running steam callbacks", t);
			}
		}
	}
	
	@Override
	public void shutdown() {
		
		if (!this.active) {
			return;
		}
		try {
			LOGGER.debug(() -> "shutdown()");
			SteamAPI.shutdown();
		}
		catch (final Throwable t) {
		}
	}
	
	@Override
	public boolean isActive() {
		return this.active;
	}
	
	@Override
	public String getSteamName() {
		
		if (!this.active) {
			return null;
		}
		
		try {
			return this.steamFriends.getPersonaName();
		}
		catch (final Throwable t) {
			return null;
		}
	}
	
	@Override
	public int getSteamId() {
		
		if (!this.active) {
			return 0;
		}
		
		try {
			return this.steamUser.getSteamID().getAccountID();
		}
		catch (final Throwable t) {
			return 0;
		}
	}
	
	@Override
	public String getSteamLanguage() {
		
		if (!this.active) {
			return null;
		}
		
		try {
			return this.steamApps.getCurrentGameLanguage();
		}
		catch (final Throwable t) {
			return null;
		}
	}
	
	@Override
	public void activateOverlay() {
		
		if (!this.active) {
			return;
		}
		
		try {
			this.steamFriends.activateGameOverlay(OverlayDialog.Friends);
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public boolean hasOverlayAvailable() {
		if (!this.active) {
			return false;
		}
		return this.steamUtils.isOverlayEnabled();
	}
	
	@Override
	public boolean hasLobbyInviteAvailable() {
		if (!this.active) {
			return false;
		}
		return this.lobby != null && this.steamUtils.isOverlayEnabled();
	}
	
	@Override
	public void lobbyCreate(final String quickJoinCode) {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Creating lobby");
		
		try {
			final AtomicBoolean proceed = new AtomicBoolean(true);
			this.callQueue.add(() -> {
				if (quickJoinCode.equals(this.lobbyQuickJoinCode)) {
					proceed.set(false);
					LOGGER.debug(() -> "quickJoinCode already matches");
				}
				this.inCall = false;
			});
			this.callQueue.add(() -> {
				if (proceed.get() && this.lobby != null) {
					LOGGER.info(() -> "Leaving lobby");
					this.steamMatchmaking.leaveLobby(this.lobby);
					this.lobby = null;
					this.lobbyQuickJoinCode = null;
				}
				this.inCall = false;
			});
			this.callQueue.add(() -> {
				if (proceed.get()) {
					LOGGER.debug(() -> "Creating lobby");
					this.steamMatchmaking.createLobby(LobbyType.FriendsOnly, 64);
					this.lobbyQuickJoinCode = quickJoinCode;
				}
				else {
					this.inCall = false;
				}
			});
			this.callQueue.add(() -> {
				if (proceed.get() && this.lobby != null) {
					LOGGER.debug(() -> "Setting quickJoinCode " + quickJoinCode);
					this.steamMatchmaking.setLobbyData(this.lobby, "quickJoinCode", quickJoinCode);
				}
				this.inCall = false;
			});
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public void lobbyLeave() {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Leaving lobby");
		
		try {
			this.callQueue.add(() -> {
				if (this.lobby != null) {
					LOGGER.info(() -> "Leaving lobby");
					this.steamMatchmaking.leaveLobby(this.lobby);
					this.lobby = null;
					this.lobbyQuickJoinCode = null;
				}
				this.inCall = false;
			});
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public void lobbyJoin(final long id) {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Join lobby " + id);
		
		try {
			this.callQueue.add(() -> {
				if (this.lobby != null) {
					LOGGER.info(() -> "Leaving lobby");
					this.steamMatchmaking.leaveLobby(this.lobby);
					this.lobby = null;
					this.lobbyQuickJoinCode = null;
				}
				this.inCall = false;
			});
			this.callQueue.add(() -> {
				LOGGER.debug(() -> "Joining lobby");
				this.steamMatchmaking.joinLobby(SteamID.createFromNativeHandle(id));
			});
			this.callQueue.add(new AfterLobbyJoined(this));
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public void lobbyOpenInviteDialog() {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Opening lobby invite dialog");
		
		try {
			this.callQueue.add(() -> {
				if (this.lobby != null) {
					this.steamFriends.activateGameOverlayInviteDialog(this.lobby);
				}
				this.inCall = false;
			});
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public void achievement(final String id) {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Achievement " + id);
		
		try {
			this.callQueue.add(() -> {
				if (! this.steamUserStats.requestCurrentStats()) {
					this.inCall = false;
				}
			});
			this.callQueue.add(() -> {
				this.steamUserStats.setAchievement(id);
				this.inCall = false;
			});
			this.callQueue.add(() -> {
				if (! this.steamUserStats.storeStats()) {
					this.inCall = false;
				}
			});			
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	@Override
	public void richPresence(final String status, final String steamDisplay, final String gameMode, final String quickJoinCode) {
		
		if (!this.active) {
			return;
		}
		LOGGER.debug(() -> "Rich presence " + steamDisplay + " " + gameMode);
		
		try {
			this.callQueue.add(() -> {
				this.steamFriends.setRichPresence("status", status);
				this.steamFriends.setRichPresence("steam_display", steamDisplay);
				this.steamFriends.setRichPresence("mode", gameMode);
				this.steamFriends.setRichPresence("steam_player_group", quickJoinCode);
				// GetLaunchCommandLine call missing in current java lib version
				//				if (StringUtils.isNotEmpty(quickJoinCode)) {
				//					this.steamFriends.setRichPresence("connect", "code" + quickJoinCode);
				//				}
				//				else {
				//					this.steamFriends.setRichPresence("connect", "");
				//				}
				this.inCall = false;
			});
		}
		catch (final Throwable t) {
			return;
		}
	}
	
	// @Override
	// public void workshopCreateItem(final Consumer<Long> onSuccess, final Consumer<String> onError) {
		
	// 	if (!this.active) {
	// 		return;
	// 	}
	// 	LOGGER.debug(() -> "Create Workshop Item");
		
	// 	try {
	// 		this.callQueue.add(() -> {
	// 			this.publishedItemFileID = null;
	// 			this.steamUGC.createItem(this.steamUtils.getAppID(), SteamRemoteStorage.WorkshopFileType.Community);
	// 		});
	// 		this.callQueue.add(() -> {
	// 			this.inCall = false;
	// 			if (this.publishedItemFileID == null) {
	// 				onError.accept(this.createItemError);
	// 			}
	// 			else {
	// 				onSuccess.accept(SteamNativeHandle.getNativeHandle(this.publishedItemFileID));
	// 			}
	// 		});
	// 	}
	// 	catch (final Throwable t) {
	// 		return;
	// 	}
	// }
	
	// @Override
	// public void workshopUpdateItem(final long fileId, final String title, final String description, final String language, final String folder, final String previewImage, final String changeComment, final Consumer<Long> onSuccess, final Consumer<String> onError) {
		
	// 	if (!this.active) {
	// 		return;
	// 	}
	// 	LOGGER.debug(() -> "Create Workshop Item");
		
	// 	try {
	// 		this.updateItemHandle = null;
	// 		this.callQueue.add(() -> {
	// 			this.updateItemError = null;
	// 			this.publishedItemFileID = new SteamPublishedFileID(fileId);
				
	// 			this.updateItemHandle = this.steamUGC.startItemUpdate(this.steamUtils.getAppID(), this.publishedItemFileID);
	// 			this.steamUGC.setItemTitle(this.updateItemHandle, title);
	// 			this.steamUGC.setItemDescription(this.updateItemHandle, description);
	// 			this.steamUGC.setItemUpdateLanguage(this.updateItemHandle, language);
	// 			this.steamUGC.setItemVisibility(this.updateItemHandle, PublishedFileVisibility.Public);
	// 			this.steamUGC.setItemContent(this.updateItemHandle, folder);
	// 			this.steamUGC.setItemPreview(this.updateItemHandle, previewImage);
	// 			this.steamUGC.submitItemUpdate(this.updateItemHandle, changeComment);
	// 		});
	// 		this.callQueue.add(() -> {
	// 			this.inCall = false;
	// 			if (this.updateItemError == null) {
	// 				onSuccess.accept(SteamNativeHandle.getNativeHandle(this.publishedItemFileID));
	// 				this.steamFriends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + Long.toUnsignedString(SteamNativeHandle.getNativeHandle(this.publishedItemFileID)), OverlayToWebPageMode.Default);
	// 			}
	// 			else {
	// 				onError.accept(this.updateItemError);
	// 			}
	// 		});
	// 	}
	// 	catch (final Throwable t) {
	// 		return;
	// 	}
	// }
	
	// @Override
	// public void workshopOpenTerms() {
		
	// 	if (!this.active) {
	// 		return;
	// 	}
	// 	LOGGER.debug(() -> "Open Workshop Terms");
		
	// 	try {
	// 		this.steamFriends.activateGameOverlayToWebPage("http://steamcommunity.com/sharedfiles/workshoplegalagreement", OverlayToWebPageMode.Default);
	// 	}
	// 	catch (final Throwable t) {
	// 		return;
	// 	}
	// }
	
	// @Override
	// public boolean workshopGetProgress(final AtomicLong processed, final AtomicLong total) {
	// 	if (!this.active) {
	// 		return false;
	// 	}
	// 	final SteamUGCUpdateHandle handle = this.updateItemHandle;
	// 	if (handle == null) {
	// 		return false;
	// 	}
		
	// 	try {
	// 		final ItemUpdateInfo info = new ItemUpdateInfo();
	// 		final ItemUpdateStatus status = this.steamUGC.getItemUpdateProgress(handle, info);
	// 		processed.setValue(info.getBytesProcessed());
	// 		total.setValue(info.getBytesTotal());
	// 		return status != ItemUpdateStatus.Invalid;
	// 	}
	// 	catch (final Throwable t) {
	// 		return false;
	// 	}
	// }
	
	// @Override
	// public List<String> workshopListFolders() {
	// 	if (!this.active) {
	// 		return List.of();
	// 	}
	// 	LOGGER.debug(() -> "List workshop folders");
		
	// 	try {
	// 		final SteamPublishedFileID[] fileIds = new SteamPublishedFileID[512];
	// 		final int cnt = this.steamUGC.getSubscribedItems(fileIds);
			
	// 		final List<String> result = new ArrayList<>();
			
	// 		for (int i = 0; i < cnt; i++) {
	// 			final ItemInstallInfo itemInstallInfo = new ItemInstallInfo();
	// 			if (this.steamUGC.getItemInstallInfo(fileIds[i], itemInstallInfo)) {
	// 				LOGGER.debug(() -> "-> " + itemInstallInfo.getFolder());
	// 				result.add(itemInstallInfo.getFolder());
	// 			}
	// 		}
			
	// 		return result;
	// 	}
	// 	catch (final Throwable t) {
	// 		return List.of();
	// 	}
	// }
	
	private static class FriendsCallback implements SteamFriendsCallback {
		
		private final SteamworksConnectorImpl context;
		
		FriendsCallback(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void onSetPersonaNameResponse(final boolean success, final boolean localSuccess, final SteamResult result) {
			
		}
		
		@Override
		public void onPersonaStateChange(final SteamID steamID, final PersonaChange change) {
			
		}
		
		@Override
		public void onGameOverlayActivated(final boolean active) {
			
		}
		
		@Override
		public void onGameLobbyJoinRequested(final SteamID steamIDLobby, final SteamID steamIDFriend) {
			LOGGER.info(() -> "Game lobby join requested");
			
			this.context.callQueue.add(() -> {
				if (this.context.lobby != null) {
					LOGGER.info(() -> "Leaving lobby");
					this.context.steamMatchmaking.leaveLobby(this.context.lobby);
					this.context.lobby = null;
					this.context.lobbyQuickJoinCode = null;
				}
				this.context.inCall = false;
			});
			this.context.callQueue.add(() -> {
				LOGGER.debug(() -> "Joining lobby");
				this.context.steamMatchmaking.joinLobby(steamIDLobby);
			});
			this.context.callQueue.add(new AfterLobbyJoined(this.context));
		}
		
		@Override
		public void onAvatarImageLoaded(final SteamID steamID, final int image, final int width, final int height) {
			
		}
		
		@Override
		public void onFriendRichPresenceUpdate(final SteamID steamIDFriend, final int appID) {
			
		}
		
		@Override
		public void onGameRichPresenceJoinRequested(final SteamID steamIDFriend, final String connect) {
			
		}
		
		@Override
		public void onGameServerChangeRequested(final String server, final String password) {
			
		}
		
	}
	
	private static class UserStatsCallback implements SteamUserStatsCallback {
		
		private final SteamworksConnectorImpl context;
		
		UserStatsCallback(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void onUserStatsReceived(final long gameId, final SteamID steamIDUser, final SteamResult result) {
			this.context.inCall = false;
			LOGGER.debug(() -> "User stats received");
		}
		
		@Override
		public void onUserStatsStored(final long gameId, final SteamResult result) {
			this.context.inCall = false;
			LOGGER.debug(() -> "User stats stored");
		}
		
		@Override
		public void onUserStatsUnloaded(final SteamID steamIDUser) {
			
		}
		
		@Override
		public void onUserAchievementStored(final long gameId, final boolean isGroupAchievement, final String achievementName, final int curProgress, final int maxProgress) {
			
		}
		
		@Override
		public void onLeaderboardFindResult(final SteamLeaderboardHandle leaderboard, final boolean found) {
			
		}
		
		@Override
		public void onLeaderboardScoresDownloaded(final SteamLeaderboardHandle leaderboard, final SteamLeaderboardEntriesHandle entries, final int numEntries) {
			
		}
		
		@Override
		public void onLeaderboardScoreUploaded(final boolean success, final SteamLeaderboardHandle leaderboard, final int score, final boolean scoreChanged, final int globalRankNew, final int globalRankPrevious) {
			
		}
		
		@Override
		public void onNumberOfCurrentPlayersReceived(final boolean success, final int players) {
			
		}
		
		@Override
		public void onGlobalStatsReceived(final long gameId, final SteamResult result) {
			
		}
		
	}
	
	private static class MatchmakingCallback implements SteamMatchmakingCallback {
		
		private final SteamworksConnectorImpl context;
		
		MatchmakingCallback(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void onFavoritesListChanged(final int ip, final int queryPort, final int connPort, final int appID, final int flags, final boolean add, final int accountID) {
			
		}
		
		@Override
		public void onLobbyInvite(final SteamID steamIDUser, final SteamID steamIDLobby, final long gameID) {
			
		}
		
		@Override
		public void onLobbyEnter(final SteamID steamIDLobby, final int chatPermissions, final boolean blocked, final ChatRoomEnterResponse response) {
			if (response == ChatRoomEnterResponse.Success) {
				LOGGER.info(() -> "Lobby has been entered");
				this.context.lobby = steamIDLobby;
			}
			else {
				LOGGER.info(() -> "Could not enter lobby");
			}
			this.context.inCall = false;
		}
		
		@Override
		public void onLobbyDataUpdate(final SteamID steamIDLobby, final SteamID steamIDMember, final boolean success) {
			
		}
		
		@Override
		public void onLobbyChatUpdate(final SteamID steamIDLobby, final SteamID steamIDUserChanged, final SteamID steamIDMakingChange, final ChatMemberStateChange stateChange) {
			
		}
		
		@Override
		public void onLobbyChatMessage(final SteamID steamIDLobby, final SteamID steamIDUser, final ChatEntryType entryType, final int chatID) {
			
		}
		
		@Override
		public void onLobbyGameCreated(final SteamID steamIDLobby, final SteamID steamIDGameServer, final int ip, final short port) {
			
		}
		
		@Override
		public void onLobbyMatchList(final int lobbiesMatching) {
			
		}
		
		@Override
		public void onLobbyKicked(final SteamID steamIDLobby, final SteamID steamIDAdmin, final boolean kickedDueToDisconnect) {
			this.context.lobby = null;
			this.context.lobbyQuickJoinCode = null;
			LOGGER.info(() -> "Kicked from lobby");
		}
		
		@Override
		public void onLobbyCreated(final SteamResult result, final SteamID steamIDLobby) {
			if (result == SteamResult.OK) {
				LOGGER.info(() -> "Lobby has been created");
				this.context.lobby = steamIDLobby;
			}
			else {
				LOGGER.info(() -> "Lobby could not been created");
			}
			this.context.inCall = false;
		}
		
		@Override
		public void onFavoritesListAccountsUpdated(final SteamResult result) {
			
		}
		
	}
	
	private static class UtilsCallback implements SteamUtilsCallback {
		
		@SuppressWarnings("unused")
		private final SteamworksConnectorImpl context;
		
		UtilsCallback(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void onSteamShutdown() {
			
		}
		
	}
	
	private static class AfterLobbyJoined implements Runnable {
		
		private final SteamworksConnectorImpl context;
		
		AfterLobbyJoined(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void run() {
			this.context.inCall = false;
			if (this.context.lobby != null) {
				final String quickJoinCode = this.context.steamMatchmaking.getLobbyData(this.context.lobby, "quickJoinCode");
				LOGGER.debug(() -> "Quick join code " + quickJoinCode);

				// TODO: Re-implement lobby joining via quick join code

				// if (StringUtils.isNotEmpty(quickJoinCode)) {
				// 	this.context.lobbyQuickJoinCode = quickJoinCode;
					
				// 	LOGGER.info(() -> "Launching game lobby connecting to quickJoinCode " + quickJoinCode);
					
				// 	EngineFacade.instance(false).stopEmbeddedServer();
				// 	EngineFacade.instance(false).closeNetworkClient(true);
				// 	this.context.clientEngineData.getClientModActivator().activateConfiguredMods();
					
				// 	final LobbyModule2Settings lobbyModuleSettings = (LobbyModule2Settings) EngineFacade.instance(false).getClientState().getState().get("LobbyModule2Settings");
				// 	lobbyModuleSettings.reset();
				// 	lobbyModuleSettings.setJoinServerImmediately(quickJoinCode);
				// 	EngineFacade.instance(false).getMenuLauncher().launch("lobby");
				// }
			}
		}
		
	}
	
	// private static class UGCCallback implements SteamUGCCallback {
		
	// 	private final SteamworksConnectorImpl context;
		
	// 	UGCCallback(final SteamworksConnectorImpl context) {
	// 		this.context = context;
	// 	}
		
	// 	@Override
	// 	public void onUGCQueryCompleted(final SteamUGCQuery query, final int numResultsReturned, final int totalMatchingResults, final boolean isCachedData, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onSubscribeItem(final SteamPublishedFileID publishedFileID, final SteamResult result) {
	// 		EngineFacade.instance(false).getModuleManager().changeActiveState("de.extio.spacecraft.game_client.engine.ui.SteamWorkshopInstallWarningModule", true);
	// 		EngineFacade.instance(false).getModuleManager().changeDisplayState("de.extio.spacecraft.game_client.engine.ui.SteamWorkshopInstallWarningModule", true);
	// 	}
		
	// 	@Override
	// 	public void onUnsubscribeItem(final SteamPublishedFileID publishedFileID, final SteamResult result) {
	// 		EngineFacade.instance(false).getModuleManager().changeActiveState("de.extio.spacecraft.game_client.engine.ui.SteamWorkshopInstallWarningModule", true);
	// 		EngineFacade.instance(false).getModuleManager().changeDisplayState("de.extio.spacecraft.game_client.engine.ui.SteamWorkshopInstallWarningModule", true);
	// 	}
		
	// 	@Override
	// 	public void onRequestUGCDetails(final SteamUGCDetails details, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onCreateItem(final SteamPublishedFileID publishedFileID, final boolean needsToAcceptWLA, final SteamResult result) {
	// 		if (result == SteamResult.OK) {
	// 			this.context.publishedItemFileID = publishedFileID;
	// 		}
	// 		else {
	// 			this.context.createItemError = result.name();
	// 		}
	// 		this.context.inCall = false;
	// 	}
		
	// 	@Override
	// 	public void onSubmitItemUpdate(final SteamPublishedFileID publishedFileID, final boolean needsToAcceptWLA, final SteamResult result) {
	// 		if (result == SteamResult.OK) {
	// 			this.context.publishedItemFileID = publishedFileID;
	// 			this.context.updateItemHandle = null;
	// 		}
	// 		else {
	// 			this.context.updateItemError = result.name();
	// 			this.context.updateItemHandle = null;
	// 		}
	// 		this.context.inCall = false;
	// 	}
		
	// 	@Override
	// 	public void onDownloadItemResult(final int appID, final SteamPublishedFileID publishedFileID, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onUserFavoriteItemsListChanged(final SteamPublishedFileID publishedFileID, final boolean wasAddRequest, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onSetUserItemVote(final SteamPublishedFileID publishedFileID, final boolean voteUp, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onGetUserItemVote(final SteamPublishedFileID publishedFileID, final boolean votedUp, final boolean votedDown, final boolean voteSkipped, final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onStartPlaytimeTracking(final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onStopPlaytimeTracking(final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onStopPlaytimeTrackingForAllItems(final SteamResult result) {
			
	// 	}
		
	// 	@Override
	// 	public void onDeleteItem(final SteamPublishedFileID publishedFileID, final SteamResult result) {
			
	// 	}
		
	// }
	
	private static class UserCallback implements SteamUserCallback {
		
		@SuppressWarnings("unused")
		private final SteamworksConnectorImpl context;
		
		UserCallback(final SteamworksConnectorImpl context) {
			this.context = context;
		}
		
		@Override
		public void onAuthSessionTicket(final SteamAuthTicket authTicket, final SteamResult result) {
			
		}
		
		@Override
		public void onValidateAuthTicket(final SteamID steamID, final AuthSessionResponse authSessionResponse, final SteamID ownerSteamID) {
			
		}
		
		@Override
		public void onMicroTxnAuthorization(final int appID, final long orderID, final boolean authorized) {
			
		}
		
		@Override
		public void onEncryptedAppTicket(final SteamResult result) {
			
		}
		
	}
	
}
