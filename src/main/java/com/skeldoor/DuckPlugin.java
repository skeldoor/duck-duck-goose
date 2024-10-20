package com.skeldoor;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Duck Duck Goose",
	description = "Adds ducks to empty ponds to help bring life to the world"
)
public class DuckPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	ChatMessageManager chatMessageManager;

	@Inject
	private DuckOverlay duckOverlay;

	@Inject
	private DuckConfig config;

	List<Duck> ducks;

	DuckPond yanillePond = new DuckPond(new WorldPoint(2542, 3082, 0), new WorldPoint(2544, 3079, 0), 3);
	DuckPond barbVillagePond = new DuckPond(new WorldPoint(3110, 3435, 0), new WorldPoint(3113, 3431, 0), 4);
	DuckPond zulandraFishingPond = new DuckPond(new WorldPoint(2184, 3076,0), new WorldPoint(2195,3071, 0), 5);
	DuckPond zulandraPierPond = new DuckPond(new WorldPoint(2217, 3052,0), new WorldPoint(2210,3055, 0), 3);
	DuckPond undergroundBloodveldPond = new DuckPond(new WorldPoint(3618, 9742,0), new WorldPoint(3624,9736, 0), 2);
	DuckPond southFarmingGuildPond = new DuckPond(new WorldPoint(1235, 3690,0), new WorldPoint(1226,3693, 0), 5);
	DuckPond fossilIslandCleaningPond = new DuckPond(new WorldPoint(3691, 3884,0), new WorldPoint(3692,3882, 0), 2);
	DuckPond lletyaPond = new DuckPond(new WorldPoint(2325, 3152,0), new WorldPoint(2328,3151, 0), 3);
	DuckPond ardyZooPond = new DuckPond(new WorldPoint(2629, 3270,0), new WorldPoint(2631,3269, 0), 2);
	DuckPond[] staticDuckPonds = {yanillePond, barbVillagePond, zulandraFishingPond, zulandraPierPond, undergroundBloodveldPond, southFarmingGuildPond, fossilIslandCleaningPond, lletyaPond, ardyZooPond};
	List<DuckPond> dynamicDuckPonds;

	int breadItemId = 2309;
	int POHPondID = 4527;

	boolean ducksInitialised = false;

	@Override
	protected void startUp()
	{
		ducks = new ArrayList<>();
		dynamicDuckPonds = new ArrayList<>();
		overlayManager.add(duckOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(duckOverlay);
		for (Duck duck : ducks)
		{
			duck.despawn();
		}
	}

	public Client getClient(){
		return client;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN && !ducksInitialised)
		{
			for (DuckPond duckpond : staticDuckPonds){
				for (int i = 0; i < duckpond.getMaxDucks(); i++)
				{
					Duck duck = new Duck();
					ducks.add(duck);
					duck.init(client, duckpond, false);
				}
			}
			ducksInitialised = true;
		}
		if (gameStateChanged.getGameState() == GameState.LOADING || gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			for (Duck duck : ducks)
			{
				duck.despawn();
			}
		}
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			for (Duck duck : ducks){
				duck.spawn(duck.pond.getRandomPointInPond(), getRandom(0, 2047));
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned){
		NPC npc = npcSpawned.getNpc();
		if (npc.getId() == 12808){ // Minimus in colosseum
			WorldPoint SWTile = npc.getWorldLocation();
			if (!dynamicPondAlreadyExists(SWTile)) {
				DuckPond colosseumPond1 = new DuckPond(new WorldPoint(SWTile.getX()+20, SWTile.getY()+20, 0), new WorldPoint(SWTile.getX()+15, SWTile.getY()+15, 0), 6);
				DuckPond colosseumPond2 = new DuckPond(new WorldPoint(SWTile.getX()-20, SWTile.getY()+20, 0), new WorldPoint(SWTile.getX()-15, SWTile.getY()+15, 0), 6);
				DuckPond colosseumPond3 = new DuckPond(new WorldPoint(SWTile.getX()-20, SWTile.getY()-20, 0), new WorldPoint(SWTile.getX()-15, SWTile.getY()-15, 0), 6);
				DuckPond colosseumPond4 = new DuckPond(new WorldPoint(SWTile.getX()+20, SWTile.getY()-20, 0), new WorldPoint(SWTile.getX()+15, SWTile.getY()-15, 0), 6);

				dynamicDuckPonds.add(colosseumPond1);
				dynamicDuckPonds.add(colosseumPond2);
				dynamicDuckPonds.add(colosseumPond3);
				dynamicDuckPonds.add(colosseumPond4);
				for (DuckPond duckPond: new ArrayList<>(Arrays.asList(colosseumPond1, colosseumPond2, colosseumPond3, colosseumPond4))){
					for (int i = 0; i < duckPond.getMaxDucks(); i++)
					{
						Duck duck = new Duck();
						ducks.add(duck);
						duck.init(client, duckPond, true);
					}
				}
			}
			// Refresh runelite objects after colosseum loading
			for (Duck duck : ducks){
				duck.despawn();
				duck.spawn(duck.pond.getRandomPointInPond(), getRandom(0, 2047));
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gos){
		GameObject housePond = gos.getGameObject();
		if (housePond.getId() == POHPondID){
			if (!dynamicPondAlreadyExists(housePond.getWorldLocation())) {
				WorldPoint SWTile = housePond.getWorldLocation();
				DuckPond dynamicHousePond = new DuckPond(new WorldPoint(SWTile.getX(), SWTile.getY() +1, SWTile.getPlane()), new WorldPoint(SWTile.getX() +1, SWTile.getY(),SWTile.getPlane()), 2);
				dynamicDuckPonds.add(dynamicHousePond);
				for (int i = 0; i < dynamicHousePond.getMaxDucks(); i++)
				{
					Duck duck = new Duck();
					ducks.add(duck);
					duck.init(client, dynamicHousePond, false);
					duck.getRlObject().setRadius(250); // Make duck render on top of pond
				}
			}
			// Refresh runelite objects after POH loading screens
			for (Duck duck : ducks){
				duck.despawn();
				duck.spawn(duck.pond.getRandomPointInPond(), getRandom(0, 2047));
			}
		}
	}

	private boolean dynamicPondAlreadyExists(WorldPoint housePondSWTile) {
		for (DuckPond existingDynamicDuckPond : dynamicDuckPonds){
			if (existingDynamicDuckPond.compareSWTiles(housePondSWTile)){
				return true;
			}
		}
		return false;
	}

	@Schedule(
			period = 10,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void waddleToNewPoint(){
		for (Duck duck : ducks) {
			if (duck.isActive()) {
				if (getRandom(0, 3) == 0){ // Only move the ducks a third of the time
					WorldPoint newPoint = duck.pond.getRandomPointInPond();
					duck.moveTo(newPoint, radToJau(Math.atan2(newPoint.getX(), newPoint.getY())));
					duck.quack(config.silenceDucks(), config.onlyEncourage());
				}
			}
		}
	}

	@Schedule(
			period = 5,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void quack(){
		for (Duck duck : ducks) {
			if (duck.isActive()) {
				if (getRandom(0, 3) == 0){
					duck.quack(config.silenceDucks(), config.onlyEncourage());
				}
			}
		}
	}

	static int radToJau(double a)
	{
		int j = (int) Math.round(a / Perspective.UNIT);
		return j & 2047;
	}

	@Subscribe
	public void onClientTick(ClientTick ignored) {
		for (Duck duck : ducks) {
			if (duck.getRlObject() != null && duck.animationPoses != null) {
				duck.onClientTick();
			}
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened ignored)
	{
		int firstMenuIndex = 1;

		for (Duck duck : ducks){
			if (duck.isActive() && duck.getClickbox() != null && client.getMouseCanvasPosition() != null){
				if (duck.getClickbox().contains(client.getMouseCanvasPosition().getX(),client.getMouseCanvasPosition().getY()))
				{
					String option;
					if (Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).contains(breadItemId)){
						option = "Feed";
					} else {
						option = "Examine";
					}

					client.createMenuEntry(firstMenuIndex)
							.setOption(option)
							.setTarget("<col=fffe00>" + duck.getDuckName() + "</col>")
							.setType(MenuAction.RUNELITE)
							.setParam0(0)
							.setParam1(0)
							.setDeprioritized(true);
				}
			}
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event) {
		if (event.getMenuOption().equals("Feed") || event.getMenuOption().equals("Examine")) {
			if (!(event.getMenuTarget().equals("<col=fffe00>Duck</col>") || event.getMenuTarget().equals("<col=fffe00>Drake</col>")))
				return;
			event.consume();
			String messageText;
			if (Objects.equals(event.getMenuOption(), "Feed")){
				messageText = "You tear off a chunk of bread and offer it to the duck. They take it enthusiastically and quack.";
			} else {
				messageText = ducks.get(1).getExamine(event.getMenuTarget());
			}
			String chatMessage = new ChatMessageBuilder()
					.append(ChatColorType.NORMAL)
					.append(messageText)
					.build();

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.NPC_EXAMINE)
					.runeLiteFormattedMessage(chatMessage)
					.timestamp((int) (System.currentTimeMillis() / 1000))
					.build());
		}

	}

	public int getRandom(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min) + min;
	}

	@Provides
	DuckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DuckConfig.class);
	}
}




