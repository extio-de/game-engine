package de.extio.game_engine.steamworks;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public abstract class SteamworksConnector {
	
	private static SteamworksConnector instance;
	
	public static SteamworksConnector instance() {
		return instance;
	}
	
	public static void setInstance(final SteamworksConnector i) {
		instance = i;
	}
	
	public abstract void runCallbacks();
	
	public abstract void shutdown();
	
	public abstract boolean isActive();
	
	public abstract int getSteamId();
	
	public abstract String getSteamName();
	
	public abstract String getSteamLanguage();
	
	public abstract void activateOverlay();
	
	public abstract boolean hasOverlayAvailable();
	
	public abstract boolean hasLobbyInviteAvailable();
	
	public abstract void lobbyCreate(String quickJoinCode);
	
	public abstract void lobbyLeave();
	
	public abstract void lobbyJoin(long id);
	
	public abstract void lobbyOpenInviteDialog();
	
	public abstract void achievement(final String id);
	
	public abstract void richPresence(final String status, final String steamDisplay, final String gameMode, final String quickJoinCode);
	
	// public abstract void workshopCreateItem(final Consumer<Long> onSuccess, final Consumer<String> onError);
	
	// public abstract void workshopOpenTerms();
	
	// public abstract void workshopUpdateItem(long fileId, String title, String description, String language, String folder, String previewImage, String changeComment, final Consumer<Long> onSuccess, final Consumer<String> onError);
	
	// public abstract boolean workshopGetProgress(AtomicLong processed, AtomicLong total);
	
	// public abstract List<String> workshopListFolders();
	
}
