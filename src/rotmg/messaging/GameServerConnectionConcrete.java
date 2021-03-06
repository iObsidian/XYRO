package rotmg.messaging;

import alde.flash.utils.RSA;
import alde.flash.utils.Vector;
import alde.flash.utils.XML;
import alde.flash.utils.consumer.EventConsumer;
import alde.flash.utils.consumer.MessageConsumer;
import com.hurlant.crypto.symmetric.ICipher;
import flash.events.Event;
import flash.events.TimerEvent;
import flash.utils.timer.Timer;
import rotmg.GameSprite;
import rotmg.account.core.Account;
import rotmg.classes.model.CharacterClass;
import rotmg.classes.model.ClassesModel;
import rotmg.constants.GeneralConstants;
import rotmg.constants.ItemConstants;
import rotmg.map.AbstractMap;
import rotmg.map.GroundLibrary;
import rotmg.map.Map;
import rotmg.messaging.data.*;
import rotmg.messaging.impl.*;
import rotmg.messaging.incoming.*;
import rotmg.messaging.incoming.arena.ArenaDeath;
import rotmg.messaging.incoming.arena.ImminentArenaWave;
import rotmg.messaging.incoming.pets.DeletePetMessage;
import rotmg.messaging.outgoing.*;
import rotmg.messaging.outgoing.arena.EnterArena;
import rotmg.messaging.outgoing.arena.QuestRedeem;
import rotmg.model.GameModel;
import rotmg.model.PotionInventoryModel;
import rotmg.net.Server;
import rotmg.net.SocketServer;
import rotmg.net.impl.Message;
import rotmg.net.impl.MessageCenter;
import rotmg.objects.*;
import rotmg.parameters.Parameters;
import rotmg.sound.SoundEffectLibrary;
import rotmg.util.ConditionEffect;
import rotmg.util.ConversionUtil;
import rotmg.util.Currency;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static flash.utils.timer.getTimer.getTimer;

public class GameServerConnectionConcrete extends GameServerConnection {

	private static final int TO_MILLISECONDS = 1000;
	private MessageCenter messages;
	private int playerId = -1;
	private Player player;
	private boolean retryConnection = true;
	private Timer retryTimer;
	private ClassesModel classesModel;
	private GameModel model;
	private int delayBeforeReconect;

	public GameServerConnectionConcrete(GameSprite gs, Server server, int gameId, boolean createCharacter, int charId, int keyTime, byte[] key, byte[] mapJSON, boolean isFromArena) {
		super();
		this.classesModel = ClassesModel.getInstance();
		this.messages = new MessageCenter();
		this.serverConnection = new SocketServer(messages);
		this.model = GameModel.getInstance();
		this.gs = gs;
		this.server = server;
		this.gameId = gameId;
		this.createCharacter = createCharacter;
		this.charId = charId;
		this.keyTime = keyTime;
		this.key = key;
		this.mapJSON = mapJSON;
		this.isFromArena = isFromArena;
		instance = this;
	}

	private static boolean isStatPotion(int param1) {
		return param1 == 2591 || param1 == 5465 || param1 == 9064 || (param1 == 2592 || param1 == 5466 || param1 == 9065) || (param1 == 2593 || param1 == 5467 || param1 == 9066)
				|| (param1 == 2612 || param1 == 5468 || param1 == 9067) || (param1 == 2613 || param1 == 5469 || param1 == 9068) || (param1 == 2636 || param1 == 5470 || param1 == 9069)
				|| (param1 == 2793 || param1 == 5471 || param1 == 9070) || (param1 == 2794 || param1 == 5472 || param1 == 9071)
				|| (param1 == 9724 || param1 == 9725 || param1 == 9726 || param1 == 9727 || param1 == 9728 || param1 == 9729 || param1 == 9730 || param1 == 9731);
	}

	public void connect() {
		this.mapMessages();
		System.out.println("Connecting to " + server.name + ".");
		serverConnection.connect(server.address, server.port);

		onConnected();
	}

	public void mapMessages() {
		messages.map(CREATE).toMessage(Create.class);
		messages.map(PLAYERSHOOT).toMessage(PlayerShoot.class);
		messages.map(MOVE).toMessage(Move.class);
		messages.map(PLAYERTEXT).toMessage(PlayerText.class);
		messages.map(UPDATEACK).toMessage(Message.class);
		messages.map(INVSWAP).toMessage(InvSwap.class);
		messages.map(USEITEM).toMessage(UseItem.class);
		messages.map(HELLO).toMessage(Hello.class);
		messages.map(INVDROP).toMessage(InvDrop.class);
		messages.map(PONG).toMessage(Pong.class);
		messages.map(LOAD).toMessage(Load.class);
		messages.map(SETCONDITION).toMessage(SetCondition.class);
		messages.map(TELEPORT).toMessage(Teleport.class);
		messages.map(USEPORTAL).toMessage(UsePortal.class);
		messages.map(BUY).toMessage(Buy.class);
		messages.map(PLAYERHIT).toMessage(PlayerHit.class);
		messages.map(ENEMYHIT).toMessage(EnemyHit.class);
		messages.map(AOEACK).toMessage(AoeAck.class);
		messages.map(SHOOTACK).toMessage(ShootAck.class);
		messages.map(OTHERHIT).toMessage(OtherHit.class);
		messages.map(SQUAREHIT).toMessage(SquareHit.class);
		messages.map(GOTOACK).toMessage(GotoAck.class);
		messages.map(GROUNDDAMAGE).toMessage(GroundDamage.class);
		messages.map(CHOOSENAME).toMessage(ChooseName.class);
		messages.map(CREATEGUILD).toMessage(CreateGuild.class);
		messages.map(GUILDREMOVE).toMessage(GuildRemove.class);
		messages.map(GUILDINVITE).toMessage(GuildInvite.class);
		messages.map(REQUESTTRADE).toMessage(RequestTrade.class);
		messages.map(CHANGETRADE).toMessage(ChangeTrade.class);
		messages.map(ACCEPTTRADE).toMessage(AcceptTrade.class);
		messages.map(CANCELTRADE).toMessage(CancelTrade.class);
		messages.map(CHECKCREDITS).toMessage(CheckCredits.class);
		messages.map(ESCAPE).toMessage(Escape.class);
		messages.map(QUEST_ROOM_MSG).toMessage(GoToQuestRoom.class);
		messages.map(JOINGUILD).toMessage(JoinGuild.class);
		messages.map(CHANGEGUILDRANK).toMessage(ChangeGuildRank.class);
		messages.map(EDITACCOUNTLIST).toMessage(EditAccountList.class);
		messages.map(ACTIVE_PET_UPDATE_REQUEST).toMessage(ActivePetUpdateRequest.class);
		messages.map(PETUPGRADEREQUEST).toMessage(PetUpgradeRequest.class);
		messages.map(ENTER_ARENA).toMessage(EnterArena.class);
		messages.map(ACCEPT_ARENA_DEATH).toMessage(OutgoingMessage.class);
		messages.map(QUEST_FETCH_ASK).toMessage(OutgoingMessage.class);
		messages.map(QUEST_REDEEM).toMessage(QuestRedeem.class);
		messages.map(KEY_INFO_REQUEST).toMessage(KeyInfoRequest.class);
		messages.map(PET_CHANGE_FORM_MSG).toMessage(ReskinPet.class);
		messages.map(CLAIM_LOGIN_REWARD_MSG).toMessage(ClaimDailyRewardMessage.class);
		messages.map(FAILURE).toMessage(Failure.class).toMethod(new MessageConsumer<>(this::onFailure));
		messages.map(CREATE_SUCCESS).toMessage(CreateSuccess.class).toMethod(new MessageConsumer<>(this::onCreateSuccess));
		messages.map(SERVERPLAYERSHOOT).toMessage(ServerPlayerShoot.class).toMethod(new MessageConsumer<>(this::onServerPlayerShoot));
		messages.map(DAMAGE).toMessage(Damage.class).toMethod(new MessageConsumer<>(this::onDamage));
		messages.map(UPDATE).toMessage(Update.class).toMethod(new MessageConsumer<>(this::onUpdate));
		messages.map(NOTIFICATION).toMessage(Notification.class).toMethod(new MessageConsumer<>(this::onNotification));
		messages.map(GLOBAL_NOTIFICATION).toMessage(GlobalNotification.class).toMethod(new MessageConsumer<>(this::onGlobalNotification));
		messages.map(NEWTICK).toMessage(NewTick.class).toMethod(new MessageConsumer<>(this::onNewTick));
		messages.map(SHOWEFFECT).toMessage(ShowEffect.class).toMethod(new MessageConsumer<>(this::onShowEffect));
		messages.map(GOTO).toMessage(Goto.class).toMethod(new MessageConsumer<>(this::onGoto));
		messages.map(INVRESULT).toMessage(InvResult.class).toMethod(new MessageConsumer<>(this::onInvResult));
		messages.map(RECONNECT).toMessage(Reconnect.class).toMethod(new MessageConsumer<>(this::onReconnect));
		messages.map(PING).toMessage(Ping.class).toMethod(new MessageConsumer<>(this::onPing));
		messages.map(MAPINFO).toMessage(MapInfo.class).toMethod(new MessageConsumer<>(this::onMapInfo));
		//_loc1.map(PIC).toMessage(Pic.class).toMethod(new MessageConsumer<>(this::onPic));
		messages.map(DEATH).toMessage(Death.class).toMethod(new MessageConsumer<>(this::onDeath));
		messages.map(BUYRESULT).toMessage(BuyResult.class).toMethod(new MessageConsumer<>(this::onBuyResult));
		messages.map(AOE).toMessage(Aoe.class).toMethod(new MessageConsumer<>(this::onAoe));
		messages.map(ACCOUNTLIST).toMessage(AccountList.class).toMethod(new MessageConsumer<>(this::onAccountList));
		messages.map(QUESTOBJID).toMessage(QuestObjId.class).toMethod(new MessageConsumer<>(this::onQuestObjId));
		messages.map(NAMERESULT).toMessage(NameResult.class).toMethod(new MessageConsumer<>(this::onNameResult));
		messages.map(GUILDRESULT).toMessage(GuildResult.class).toMethod(new MessageConsumer<>(this::onGuildResult));
		messages.map(ALLYSHOOT).toMessage(AllyShoot.class).toMethod(new MessageConsumer<>(this::onAllyShoot));
		messages.map(ENEMYSHOOT).toMessage(EnemyShoot.class).toMethod(new MessageConsumer<>(this::onEnemyShoot));
		messages.map(TRADEREQUESTED).toMessage(TradeRequested.class).toMethod(new MessageConsumer<>(this::onTradeRequested));
		messages.map(TRADESTART).toMessage(TradeStart.class).toMethod(new MessageConsumer<>(this::onTradeStart));
		messages.map(TRADECHANGED).toMessage(TradeChanged.class).toMethod(new MessageConsumer<>(this::onTradeChanged));
		messages.map(TRADEDONE).toMessage(TradeDone.class).toMethod(new MessageConsumer<>(this::onTradeDone));
		messages.map(TRADEACCEPTED).toMessage(TradeAccepted.class).toMethod(new MessageConsumer<>(this::onTradeAccepted));
		messages.map(CLIENTSTAT).toMessage(ClientStat.class).toMethod(new MessageConsumer<>(this::onClientStat));
		messages.map(FILE).toMessage(File.class).toMethod(new MessageConsumer<>(this::onFile));
		messages.map(INVITEDTOGUILD).toMessage(InvitedToGuild.class).toMethod(new MessageConsumer<>(this::onInvitedToGuild));
		messages.map(ACTIVEPETUPDATE).toMessage(ActivePet.class).toMethod(new MessageConsumer<>(this::onActivePetUpdate));
		messages.map(NEW_ABILITY).toMessage(NewAbilityMessage.class).toMethod(new MessageConsumer<>(this::onNewAbility));
		messages.map(PETYARDUPDATE).toMessage(PetYard.class).toMethod(new MessageConsumer<>(this::onPetYardUpdate));
		messages.map(EVOLVE_PET).toMessage(EvolvedPetMessage.class).toMethod(new MessageConsumer<>(this::onEvolvedPet));
		messages.map(DELETE_PET).toMessage(DeletePetMessage.class).toMethod(new MessageConsumer<>(this::onDeletePet));
		messages.map(HATCH_PET).toMessage(HatchPetMessage.class).toMethod(new MessageConsumer<>(this::onHatchPet));
		messages.map(IMMINENT_ARENA_WAVE).toMessage(ImminentArenaWave.class).toMethod(new MessageConsumer<>(this::onImminentArenaWave));
		messages.map(ARENA_DEATH).toMessage(ArenaDeath.class).toMethod(new MessageConsumer<>(this::onArenaDeath));
		messages.map(VERIFY_EMAIL).toMessage(VerifyEmail.class).toMethod(new MessageConsumer<>(this::onVerifyEmail));
		messages.map(RESKIN_UNLOCK).toMessage(ReskinUnlock.class).toMethod(new MessageConsumer<>(this::onReskinUnlock));
		messages.map(PASSWORD_PROMPT).toMessage(PasswordPrompt.class).toMethod(new MessageConsumer<>(this::onPasswordPrompt));
		messages.map(QUEST_FETCH_RESPONSE).toMessage(QuestFetchResponse.class).toMethod(new MessageConsumer<>(this::onQuestFetchResponse));
		messages.map(QUEST_REDEEM_RESPONSE).toMessage(QuestRedeemResponse.class).toMethod(new MessageConsumer<>(this::onQuestRedeemResponse));
		messages.map(KEY_INFO_RESPONSE).toMessage(KeyInfoResponse.class).toMethod(new MessageConsumer<>(this::onKeyInfoResponse));
		messages.map(LOGIN_REWARD_MSG).toMessage(ClaimDailyRewardResponse.class).toMethod(new MessageConsumer<>(this::onLoginRewardResponse));
		messages.map(RESKIN).toMessage(Reskin.class);
		messages.map(TEXT).toMessage(Text.class).toMethod(new MessageConsumer<>(this::onText));
	}

	public void onHatchPet(HatchPetMessage param1) {

	}

	private void onDeletePet(DeletePetMessage param1) {

	}

	private void onNewAbility(NewAbilityMessage param1) {

	}

	private void onPetYardUpdate(PetYard param1) {

	}

	private void onEvolvedPet(EvolvedPetMessage param1) {

	}

	private void onActivePetUpdate(ActivePet param1) {

	}

	private void encryptConnection() {
		serverConnection.setOutgoingCipher(new ICipher("6a39570cc9de4ec71d64821894"));
		serverConnection.setIncomingCipher(new ICipher("c79332b197f92ba85ed281a023"));
	}

	/**
	 * This method uses the Java 7 way of getting a random int between ranges.
	 * It serves the same purpose as 'AS3's Random.getNextIntInRange()...
	 */
	@Override
	public int getNextDamage(int param1, int param2) {
		return ThreadLocalRandom.current().nextInt(param1, param2 + 1);
	}

	private void create() {
		CharacterClass selectedClass = this.classesModel.getSelected();
		Create create = (Create) this.messages.require(CREATE);
		create.classType = selectedClass.id;
		create.skinType = 0; //selectedClass.skins.getSelectedSkin().id
		serverConnection.sendMessage(create);
	}

	private void load() {
		System.out.println("Loading character '" + charId + "'...");

		Load load = (Load) this.messages.require(LOAD);
		load.charId = charId;
		load.isFromArena = isFromArena;

		System.out.println(load);

		serverConnection.sendMessage(load);
	}

	public void playerShoot(int time, Projectile proj) {
		PlayerShoot playerShoot = (PlayerShoot) this.messages.require(PLAYERSHOOT);
		playerShoot.time = time;
		playerShoot.bulletId = proj.bulletId;
		playerShoot.containerType = proj.containerType;
		playerShoot.startingPos.x = proj.x;
		playerShoot.startingPos.y = proj.y;
		playerShoot.angle = proj.angle;
		this.serverConnection.sendMessage(playerShoot);
	}

	public void playerHit(int bulletId, int objectId) {
		PlayerHit playerHit = (PlayerHit) this.messages.require(PLAYERHIT);
		playerHit.bulletId = bulletId;
		playerHit.objectId = objectId;
		this.serverConnection.sendMessage(playerHit);
	}

	public void enemyHit(int time, int bulletId, int targetId, boolean kill) {
		EnemyHit enemyHit = (EnemyHit) this.messages.require(ENEMYHIT);
		enemyHit.time = time;
		enemyHit.bulletId = bulletId;
		enemyHit.targetId = targetId;
		enemyHit.kill = kill;
		this.serverConnection.sendMessage(enemyHit);
	}

	public void otherHit(int time, int bulletId, int objectId, int targetId) {
		OtherHit otherHit = (OtherHit) this.messages.require(OTHERHIT);
		otherHit.time = time;
		otherHit.bulletId = bulletId;
		otherHit.objectId = objectId;
		otherHit.targetId = targetId;
		this.serverConnection.sendMessage(otherHit);
	}

	public void squareHit(int time, int bulletId, int objectId) {
		SquareHit squareHit = (SquareHit) this.messages.require(SQUAREHIT);
		squareHit.time = time;
		squareHit.bulletId = bulletId;
		squareHit.objectId = objectId;
		this.serverConnection.sendMessage(squareHit);
	}

	public void aoeAck(int time, double x, double y) {
		AoeAck aoeAck = (AoeAck) this.messages.require(AOEACK);
		aoeAck.time = time;
		aoeAck.position.x = x;
		aoeAck.position.y = y;
		this.serverConnection.sendMessage(aoeAck);
	}

	public void groundDamage(int time, double x, double y) {
		GroundDamage groundDamage = (GroundDamage) this.messages.require(GROUNDDAMAGE);
		groundDamage.time = time;
		groundDamage.position.x = x;
		groundDamage.position.y = y;
		this.serverConnection.sendMessage(groundDamage);
	}

	public void shootAck(int time) {
		ShootAck shootAck = (ShootAck) this.messages.require(SHOOTACK);
		shootAck.time = time;
		this.serverConnection.sendMessage(shootAck);
	}

	public void playerText(String textStr) {
		PlayerText playerTextMessage = (PlayerText) this.messages.require(PLAYERTEXT);
		playerTextMessage.text = textStr;
		this.serverConnection.sendMessage(playerTextMessage);
	}

	public boolean invSwap(Player player, GameObject sourceObj, int slotId1, int itemId, GameObject targetObj, int slotId2, int objectType2) {
		if (this.gs == null) {
			return false;
		}
		InvSwap invSwap = (InvSwap) this.messages.require(INVSWAP);
		invSwap.time = this.gs.lastUpdate;
		invSwap.position.x = player.x;
		invSwap.position.y = player.y;
		invSwap.slotObject1.objectId = sourceObj.objectId;
		invSwap.slotObject1.slotId = slotId1;
		invSwap.slotObject1.objectType = itemId;
		invSwap.slotObject2.objectId = targetObj.objectId;
		invSwap.slotObject2.slotId = slotId2;
		invSwap.slotObject2.objectType = objectType2;
		this.serverConnection.sendMessage(invSwap);
		int tempType = sourceObj.equipment.get(slotId1);
		sourceObj.equipment.set(slotId1, targetObj.equipment.get(slotId2));
		targetObj.equipment.set(slotId2, tempType);
		SoundEffectLibrary.play("inventory_move_item");
		return true;
	}

	@Override
	public boolean invSwapPotion(Player player, GameObject sourceObj, int slotId1, int itemId, GameObject targetObj, int slotId2, int objectType2) {
		if (this.gs == null) {
			return false;
		}
		InvSwap invSwap = (InvSwap) this.messages.require(INVSWAP);
		invSwap.time = this.gs.lastUpdate;
		invSwap.position.x = player.x;
		invSwap.position.y = player.y;
		invSwap.slotObject1.objectId = sourceObj.objectId;
		invSwap.slotObject1.slotId = slotId1;
		invSwap.slotObject1.objectType = itemId;
		invSwap.slotObject2.objectId = targetObj.objectId;
		invSwap.slotObject2.slotId = slotId2;
		invSwap.slotObject2.objectType = objectType2;
		sourceObj.equipment.set(slotId1, ItemConstants.NO_ITEM);
		if (itemId == PotionInventoryModel.HEALTH_POTION_ID) {
			player.healthPotionCount++;
		} else if (itemId == PotionInventoryModel.MAGIC_POTION_ID) {
			player.magicPotionCount++;
		}
		this.serverConnection.sendMessage(invSwap);
		SoundEffectLibrary.play("inventory_move_item");
		return true;
	}

	@Override
	public boolean invSwapRaw(Player player, int objectId1, int slotId1, int objectType1, int objectId2, int slotId2, int objectType2) {
		if (this.gs == null) {
			return false;
		}
		InvSwap loc8 = (InvSwap) this.messages.require(INVSWAP);
		loc8.time = gs.lastUpdate;
		loc8.position.x = player.x;
		loc8.position.y = player.y;
		loc8.slotObject1.objectId = objectId1;
		loc8.slotObject1.slotId = slotId1;
		loc8.slotObject1.objectType = objectType1;
		loc8.slotObject2.objectId = objectId2;
		loc8.slotObject2.slotId = slotId2;
		loc8.slotObject2.objectType = objectType2;
		//this.addTextLine.dispatch(ChatMessage.make("",  "INVSWAP;
		serverConnection.sendMessage(loc8);
		SoundEffectLibrary.play("inventory_move_item");
		return true;
	}

	public void invDrop(GameObject object, int slotId, int objectType) {
		InvDrop invDrop = (InvDrop) this.messages.require(INVDROP);
		invDrop.slotObject.objectId = object.objectId;
		invDrop.slotObject.slotId = slotId;
		invDrop.slotObject.objectType = objectType;
		this.serverConnection.sendMessage(invDrop);
		if (slotId != PotionInventoryModel.HEALTH_POTION_SLOT && slotId != PotionInventoryModel.MAGIC_POTION_SLOT) {
			object.equipment.put(slotId, ItemConstants.NO_ITEM);
		}
	}

	public void useItem(int time, int objectId, int slotId, int objectType, double posX, double posY, int useType) {
		UseItem useItemMess = (UseItem) this.messages.require(USEITEM);
		useItemMess.time = time;
		useItemMess.slotObject.objectId = objectId;
		useItemMess.slotObject.slotId = slotId;
		useItemMess.slotObject.objectType = objectType;
		useItemMess.itemUsePos.x = posX;
		useItemMess.itemUsePos.y = posY;
		useItemMess.useType = useType;
		this.serverConnection.sendMessage(useItemMess);
	}

	public boolean useItem_new(GameObject itemOwner, int slotId) {
		int itemId = itemOwner.equipment.get(slotId);
		XML objectXML = ObjectLibrary.xmlLibrary.get(itemId);
		if ((objectXML != null) && !itemOwner.isPaused() && (objectXML.hasOwnProperty("Consumable") || objectXML.hasOwnProperty("InvUse"))) {
			this.applyUseItem(itemOwner, slotId, itemId, objectXML);
			SoundEffectLibrary.play("use_potion");
			return true;
		}
		SoundEffectLibrary.play("error");
		return false;
	}

	private void applyUseItem(GameObject owner, int slotId, int objectType, XML itemData) {
		UseItem useItemMess = (UseItem) this.messages.require(USEITEM);
		useItemMess.time = getTimer();
		useItemMess.slotObject.objectId = owner.objectId;
		useItemMess.slotObject.slotId = slotId;
		useItemMess.slotObject.objectType = objectType;
		useItemMess.itemUsePos.x = 0;
		useItemMess.itemUsePos.y = 0;
		this.serverConnection.sendMessage(useItemMess);
		if (itemData.hasOwnProperty("Consumable")) {
			owner.equipment.set(slotId, -1);
		}
	}

	public void setCondition(int conditionEffect, double conditionDuration) {
		SetCondition setCondition = (SetCondition) this.messages.require(SETCONDITION);
		setCondition.conditionEffect = conditionEffect;
		setCondition.conditionDuration = conditionDuration;
		this.serverConnection.sendMessage(setCondition);
	}

	public void move(int tickId, Player player) {

		int len = 0;
		int i = 0;
		double x = -1;
		double y = -1;
		if (player != null && !player.isPaused()) {
			x = player.x;
			y = player.y;
		}
		Move move = (Move) this.messages.require(MOVE);
		move.tickId = tickId;
		move.time = this.gs.lastUpdate;
		move.newPosition.x = x;
		move.newPosition.y = y;
		int lastMove = this.gs.moveRecords.lastClearTime;
		move.records.clear();
		if (lastMove >= 0 && move.time - lastMove > 125) {
			len = Math.min(10, this.gs.moveRecords.records.size());
			for (i = 0; i < len; i++) {
				if (this.gs.moveRecords.records.get(i).time >= move.time - 25) {
					break;
				}
				move.records.add(this.gs.moveRecords.records.get(i));
			}
		}
		this.gs.moveRecords.clear(move.time);
		this.serverConnection.sendMessage(move);
		if (player != null)
			player.onMove();
	}

	public void teleport(int objectId) {
		Teleport teleport = (Teleport) this.messages.require(TELEPORT);
		teleport.objectId = objectId;
		this.serverConnection.sendMessage(teleport);
	}

	public void usePortal(int objectId) {
		UsePortal usePortalMess = (UsePortal) this.messages.require(USEPORTAL);
		usePortalMess.objectId = objectId;
		this.serverConnection.sendMessage(usePortalMess);
	}

	public void buy(int sellableObjectId, int currencyType) {
		if (this.outstandingBuy != null) {
			return;
		}
		SellableObject sObj = (SellableObject) this.gs.map.goDict.get(sellableObjectId);
		if (sObj == null) {
			return;
		}
		boolean converted = false;
		if (sObj.currency == Currency.GOLD) {
			converted = this.gs.playerModel.getConverted() || this.player.credits > 100 || sObj.price > this.player.credits;
		}
		this.outstandingBuy = new OutstandingBuy(sObj.soldObjectInternalName(), sObj.price, sObj.currency, converted);
		Buy buyMesssage = (Buy) this.messages.require(BUY);
		buyMesssage.objectId = sellableObjectId;
		this.serverConnection.sendMessage(buyMesssage);
	}

	public void gotoAck(int time) {
		GotoAck gotoAck = (GotoAck) this.messages.require(GOTOACK);
		gotoAck.time = time;
		this.serverConnection.sendMessage(gotoAck);
	}

	public void editAccountList(int accountListId, boolean add, int objectId) {
		EditAccountList eal = (EditAccountList) this.messages.require(EDITACCOUNTLIST);
		eal.accountListId = accountListId;
		eal.add = add;
		eal.objectId = objectId;
		this.serverConnection.sendMessage(eal);
	}

	public void chooseName(String name) {
		ChooseName chooseName = (ChooseName) this.messages.require(CHOOSENAME);
		chooseName.name = name;
		this.serverConnection.sendMessage(chooseName);
	}

	public void createGuild(String name) {
		CreateGuild createGuild = (CreateGuild) this.messages.require(CREATEGUILD);
		createGuild.name = name;
		this.serverConnection.sendMessage(createGuild);
	}

	public void guildRemove(String name) {
		GuildRemove guildRemove = (GuildRemove) this.messages.require(GUILDREMOVE);
		guildRemove.name = name;
		this.serverConnection.sendMessage(guildRemove);
	}

	public void guildInvite(String name) {
		GuildInvite guildInvite = (GuildInvite) this.messages.require(GUILDINVITE);
		guildInvite.name = name;
		this.serverConnection.sendMessage(guildInvite);
	}

	public void requestTrade(String name) {
		RequestTrade requestTrade = (RequestTrade) this.messages.require(REQUESTTRADE);
		requestTrade.name = name;
		this.serverConnection.sendMessage(requestTrade);
	}

	public void changeTrade(boolean[] offer) {
		ChangeTrade changeTrade = (ChangeTrade) this.messages.require(CHANGETRADE);
		changeTrade.offer = offer;
		this.serverConnection.sendMessage(changeTrade);
	}

	public void acceptTrade(boolean[] myOffer, boolean[] yourOffer) {
		AcceptTrade acceptTrade = (AcceptTrade) this.messages.require(ACCEPTTRADE);
		acceptTrade.myOffer = myOffer;
		acceptTrade.yourOffer = yourOffer;
		this.serverConnection.sendMessage(acceptTrade);
	}

	public void cancelTrade() {
		this.serverConnection.sendMessage(this.messages.require(CANCELTRADE));
	}

	public void checkCredits() {
		this.serverConnection.sendMessage(this.messages.require(CHECKCREDITS));
	}

	public void escape() {
		if (this.playerId == -1) {
			return;
		}
		this.serverConnection.sendMessage(this.messages.require(ESCAPE));
	}

	public void joinGuild(String guildName) {
		JoinGuild joinGuild = (JoinGuild) this.messages.require(JOINGUILD);
		joinGuild.guildName = guildName;
		this.serverConnection.sendMessage(joinGuild);
	}

	public void changeGuildRank(String name, int rank) {
		ChangeGuildRank changeGuildRank = (ChangeGuildRank) this.messages.require(CHANGEGUILDRANK);
		changeGuildRank.name = name;
		changeGuildRank.guildRank = rank;
		this.serverConnection.sendMessage(changeGuildRank);
	}

	private String rsaEncrypt(String data) {
		return RSA.encrypt(data);
	}

	/**
	 * This method needs verification (mapJSON is a String, not a byte[])
	 */
	private void onConnected() {
		Account loc1 = gs.playerModel.account;

		System.out.println("Connected!...");

		this.encryptConnection();
		Hello hello = (Hello) this.messages.require(HELLO);
		hello.buildVersion = Parameters.BUILD_VERSION + "." + Parameters.MINOR_VERSION;
		hello.gameId = gameId;
		hello.guid = this.rsaEncrypt(loc1.userId);
		hello.password = this.rsaEncrypt(loc1.password);
		hello.secret = this.rsaEncrypt(loc1.secret);
		hello.keyTime = keyTime;
		if (key != null) {
			hello.key = key;
		} else {
			hello.key = new byte[0];
		}
		if (mapJSON != null) {
			hello.mapJSON = mapJSON;
		} else {
			hello.mapJSON = new byte[0];
		}
		hello.entrytag = loc1.entryTag;
		hello.gameNet = loc1.gameNetwork;
		hello.gameNetUserId = loc1.gameNetworkUserId;
		hello.playPlatform = loc1.playPlatform;
		hello.platformToken = loc1.platformToken;
		hello.userToken = loc1.token;

		System.out.println("Sending Hello...");

		serverConnection.sendMessage(hello);
	}

	public void onCreateSuccess(CreateSuccess createSuccess) {
		System.out.println("Create success");

		this.playerId = createSuccess.objectId;
		this.charId = createSuccess.charId;
		//this.gs.initialize();
		this.createCharacter = false;
	}

	public void onDamage(Damage damage) {
		int projId = 0;
		Map map = (Map) this.gs.map;
		Projectile proj = null;
		if (damage.objectId >= 0 && damage.bulletId > 0) {
			projId = Projectile.findObjId(damage.objectId, damage.bulletId);
			proj = (Projectile) map.boDict.get(projId);
			if (proj != null && !proj.projProps.multiHit) {
				map.removeObj(projId);
			}
		}
		GameObject target = map.goDict.get(damage.targetId);
		if (target != null) {
			target.damage(false, damage.damageAmount, ConversionUtil.toIntVector(damage.effects), damage.kill, proj);
		}
	}

	public void onServerPlayerShoot(ServerPlayerShoot serverPlayerShoot) {
		boolean needsAck = (serverPlayerShoot.ownerId == this.playerId);
		GameObject owner = this.gs.map.goDict.get(serverPlayerShoot.ownerId);
		if (owner == null || owner.dead) {
			if (needsAck) {
				this.shootAck(-1);
			}
			return;
		}
		Projectile proj = new Projectile();
		proj.reset(serverPlayerShoot.containerType, 0, serverPlayerShoot.ownerId, serverPlayerShoot.bulletId, serverPlayerShoot.angle, this.gs.lastUpdate);
		proj.setDamage(serverPlayerShoot.damage);
		this.gs.map.addObj(proj, serverPlayerShoot.startingPos.x, serverPlayerShoot.startingPos.y);
		if (needsAck) {
			this.shootAck(this.gs.lastUpdate);
		}
	}

	void onAllyShoot(AllyShoot allyShoot) {
		GameObject owner = this.gs.map.goDict.get(allyShoot.ownerId);
		if (owner == null || owner.dead) {
			return;
		}
		Projectile proj = new Projectile();
		proj.reset(allyShoot.containerType, 0, allyShoot.ownerId, allyShoot.bulletId, allyShoot.angle, this.gs.lastUpdate);
		this.gs.map.addObj(proj, owner.x, owner.y);
		owner.setAttack(allyShoot.containerType, allyShoot.angle);
	}

	private void onReskinUnlock(ReskinUnlock param1) {
		/*Object _loc2 = null;
		CharacterSkin _loc3 = null;
		PetsModel _loc4 = null;
		if(param1.isPetSkin == 0)
		{
		  for(this.contains(_loc2).model.player.lockedSlot)
		  {
		     if(this.model.player.lockedSlot[_loc2] == param1.skinID)
		     {
		        this.model.player.lockedSlot[_loc2] = 0;
		     }
		  }
		  _loc3 = this.classesModel.getCharacterClass(this.model.player.objectType).skins.getSkin(param1.skinID);
		  _loc3.setState(CharacterSkinState.OWNED);
		}
		else
		{
		  _loc4 = StaticInjectorContext.getInjector().getInstance(PetsModel);
		  _loc4.unlockSkin(param1.skinID);
		}*/
	}

	void onEnemyShoot(EnemyShoot enemyShoot) {
		GameObject owner = this.gs.map.goDict.get(enemyShoot.ownerId);
		if (owner == null || owner.dead) {
			this.shootAck(-1);
			return;
		}
		for (int i = 0; i < enemyShoot.numShots; i++) {
			Projectile proj = new Projectile();
			double angle = enemyShoot.angle + enemyShoot.angleInc * i;
			proj.reset(owner.objectType, enemyShoot.bulletType, enemyShoot.ownerId, (enemyShoot.bulletId + i) % 256, angle, this.gs.lastUpdate);
			proj.setDamage(enemyShoot.damage);
			this.gs.map.addObj(proj, enemyShoot.startingPos.x, enemyShoot.startingPos.y);
		}
		this.shootAck(this.gs.lastUpdate);
		owner.setAttack(owner.objectType, enemyShoot.angle + enemyShoot.angleInc * ((enemyShoot.numShots - 1) / 2));
	}

	public void onTradeRequested(TradeRequested tradeRequested) {

		System.out.println(tradeRequested.name + " wants to " + "trade with you.  Type \"/trade " + tradeRequested.name + "\" to trade.");

	}

	public void onTradeStart(TradeStart tradeStart) {
		//this.gs.hudView.startTrade(this.gs, tradeStart);
	}

	public void onTradeChanged(TradeChanged tradeChanged) {
		//this.gs.hudView.tradeChanged(tradeChanged);
	}

	public void onTradeDone(TradeDone tradeDone) {
		//this.gs.hudView.tradeDone();
		System.out.println(tradeDone.description);
	}

	public void onTradeAccepted(TradeAccepted tradeAccepted) {
		//this.gs.hudView.tradeAccepted(tradeAccepted);
	}

	private void addObject(ObjectData obj) {
		Map map = (Map) this.gs.map;
		GameObject go = ObjectLibrary.getObjectFromType(obj.objectType);
		if (go == null) {
			return;
		}
		ObjectStatusData status = obj.status;
		go.setObjectId(status.objectId);
		map.addObj(go, status.pos.x, status.pos.y);
		if (go instanceof Player) {
			this.handleNewPlayer((Player) go, map);
		}
		this.processObjectStatus(status, 0, -1);
	}

	/**
	 * From addObject(ObjectData param1)
	 */
	private void handleNewPlayer(Player player, Map map) {
		this.setPlayerSkinTemplate(player, 0);
		if (player.objectId == this.playerId) {
			this.player = player;
			this.model.player = player;
			map.player = player;
		}
	}

	private void onUpdate(Update update) {
		int loc3 = 0;
		GroundTileData loc4 = null;
		Message loc2 = this.messages.require(UPDATEACK);
		serverConnection.sendMessage(loc2);
		loc3 = 0;
		while (loc3 < update.tiles.length) {
			loc4 = update.tiles[loc3];
			gs.map.setGroundTile(loc4.x, loc4.y, loc4.type);
			loc3++;
		}
		loc3 = 0;
		while (loc3 < update.newObjs.length) {
			this.addObject(update.newObjs[loc3]);
			loc3++;
		}
		loc3 = 0;
		while (loc3 < update.drops.length) {
			gs.map.removeObj(update.drops[loc3]);
			loc3++;
		}
	}

	private void onNotification(Notification notification) {

		GameObject go = this.gs.map.goDict.get(notification.objectId);
		if (go != null) {
			/*StringBuilder b = new StringBuilder(notification.message); // Workaround
			text = new QueuedStatusText(go, b, notification.color, 2000);
			this.gs.map.mapOverlay.addQueuedText(text);**/
			if (go == this.player && notification.message.equals("Quest Complete!")) {
				this.gs.map.quest.completed();
			}
		}
	}

	private void onGlobalNotification(GlobalNotification notification) {
		System.out.println("Notification : " + notification.text);
	}

	private void onNewTick(NewTick newTick) {

		this.move(newTick.tickId, this.player);
		for (ObjectStatusData objectStatus : newTick.statuses) {
			this.processObjectStatus(objectStatus, newTick.tickTime, newTick.tickId);
		}
		this.lastTickId = newTick.tickId;
	}

	private void onShowEffect(ShowEffect showEffect) {
		//System.out.println("Show effect : " + showEffect);
	}

	/**
	 * In Java goto is a reserved keyword
	 */
	private void onGoto(Goto gotoPacket) {
		this.gotoAck(this.gs.lastUpdate);
		GameObject go = this.gs.map.goDict.get(gotoPacket.objectId);
		if (go == null) {
			return;
		}
		go.onGoto(gotoPacket.pos.x, gotoPacket.pos.y, this.gs.lastUpdate);
	}

	private void updateGameObject(GameObject go, StatData[] stats, boolean isMyObject) {
		int index = 0;

		Player player = null;
		if (go instanceof Player) {
			player = (Player) go;
		}

		Merchant merchant = null;
		if (go instanceof Merchant) {
			merchant = (Merchant) go;
		}

		for (StatData stat : stats) {
			int value = stat.statValue;
			switch (stat.statType) {
			case StatData.MAX_HP_STAT:
				go.maxHP = value;
				continue;
			case StatData.HP_STAT:
				go.hp = value;
				continue;
			case StatData.SIZE_STAT:
				go.size = value;
				continue;
			case StatData.MAX_MP_STAT:
				player.maxMP = value;
				continue;
			case StatData.MP_STAT:
				player.mp = value;
				continue;
			case StatData.NEXT_LEVEL_EXP_STAT:
				player.nextLevelExp = value;
				continue;
			case StatData.EXP_STAT:
				player.exp = value;
				continue;
			case StatData.LEVEL_STAT:
				go.level = value;
				continue;
			case StatData.ATTACK_STAT:
				player.attack = value;
				continue;
			case StatData.DEFENSE_STAT:
				go.defense = value;
				continue;
			case StatData.SPEED_STAT:
				player.speed = value;
				continue;
			case StatData.DEXTERITY_STAT:
				player.dexterity = value;
				continue;
			case StatData.VITALITY_STAT:
				player.vitality = value;
				continue;
			case StatData.WISDOM_STAT:
				player.wisdom = value;
				continue;
			case StatData.CONDITION_STAT:
				go.condition.put(ConditionEffect.CE_FIRST_BATCH, value);
				continue;
			case StatData.INVENTORY_0_STAT:
			case StatData.INVENTORY_1_STAT:
			case StatData.INVENTORY_2_STAT:
			case StatData.INVENTORY_3_STAT:
			case StatData.INVENTORY_4_STAT:
			case StatData.INVENTORY_5_STAT:
			case StatData.INVENTORY_6_STAT:
			case StatData.INVENTORY_7_STAT:
			case StatData.INVENTORY_8_STAT:
			case StatData.INVENTORY_9_STAT:
			case StatData.INVENTORY_10_STAT:
			case StatData.INVENTORY_11_STAT:
				go.equipment.put(stat.statType - StatData.INVENTORY_0_STAT, value);
				continue;
			case StatData.NUM_STARS_STAT:
				player.numStars = value;
				continue;
			case StatData.NAME_STAT:
				if (!go.name.equals(stat.strStatValue)) {
					go.name = stat.strStatValue;
					go.nameBitmapData = null;
				}
				continue;
			case StatData.TEX1_STAT:
				go.setTexture(value);
				continue;
			case StatData.TEX2_STAT:
				go.setAltTexture(value);
				continue;
			case StatData.MERCHANDISE_TYPE_STAT:
				merchant.setMerchandiseType(value);
				continue;
			case StatData.CREDITS_STAT:
				player.setCredits(value);
				continue;
			case StatData.MERCHANDISE_PRICE_STAT:
				//(SellableObject) go.setPrice(value);
				continue;
			case StatData.ACTIVE_STAT:
				//(Portal) go.active = value != 0;
				continue;
			case StatData.ACCOUNT_ID_STAT:
				player.accountId = stat.strStatValue;
				continue;
			case StatData.FAME_STAT:
				player.fame = value;
				continue;
			case StatData.MERCHANDISE_CURRENCY_STAT:
				//(SellableObject) go.setCurrency(value);
				continue;
			case StatData.CONNECT_STAT:
				go.connectType = value;
				continue;
			case StatData.MERCHANDISE_COUNT_STAT:
				merchant.count = value;
				merchant.untilNextMessage = 0;
				continue;
			case StatData.MERCHANDISE_MINS_LEFT_STAT:
				merchant.minsLeft = value;
				merchant.untilNextMessage = 0;
				continue;
			case StatData.MERCHANDISE_DISCOUNT_STAT:
				merchant.discount = value;
				merchant.untilNextMessage = 0;
				continue;
			case StatData.MERCHANDISE_RANK_REQ_STAT:
				//(SellableObject) go.setRankReq(value);
				continue;
			case StatData.MAX_HP_BOOST_STAT:
				player.maxHPBoost = value;
				continue;
			case StatData.MAX_MP_BOOST_STAT:
				player.maxMPBoost = value;
				continue;
			case StatData.ATTACK_BOOST_STAT:
				player.attackBoost = value;
				continue;
			case StatData.DEFENSE_BOOST_STAT:
				player.defenseBoost = value;
				continue;
			case StatData.SPEED_BOOST_STAT:
				player.speedBoost = value;
				continue;
			case StatData.VITALITY_BOOST_STAT:
				player.vitalityBoost = value;
				continue;
			case StatData.WISDOM_BOOST_STAT:
				player.wisdomBoost = value;
				continue;
			case StatData.DEXTERITY_BOOST_STAT:
				player.dexterityBoost = value;
				continue;
			case StatData.OWNER_ACCOUNT_ID_STAT:
				//(Container) go.setOwnerId(value);
				continue;
			case StatData.RANK_REQUIRED_STAT:
				//(NameChanger) go.setRankRequired(value);
				continue;
			case StatData.NAME_CHOSEN_STAT:
				player.nameChosen = value != 0;
				go.nameBitmapData = null;
				continue;
			case StatData.CURR_FAME_STAT:
				player.currFame = value;
				continue;
			case StatData.NEXT_CLASS_QUEST_FAME_STAT:
				player.nextClassQuestFame = value;
				continue;
			case StatData.LEGENDARY_RANK_STAT:
				player.legendaryRank = value;
				continue;
			case StatData.SINK_LEVEL_STAT:
				if (!isMyObject) {
					player.sinkLevel = value;
				}
				continue;
			case StatData.ALT_TEXTURE_STAT:
				go.setAltTexture(value);
				continue;
			case StatData.GUILD_NAME_STAT:
				player.setGuildName(stat.strStatValue);
				continue;
			case StatData.GUILD_RANK_STAT:
				player.guildRank = value;
				continue;
			case StatData.BREATH_STAT:
				player.breath = value;
				continue;
			case StatData.XP_BOOSTED_STAT:
				player.xpBoost = value;
				continue;
			case StatData.XP_TIMER_STAT:
				player.xpTimer = value * TO_MILLISECONDS;
				continue;
			case StatData.LD_TIMER_STAT:
				player.dropBoost = value * TO_MILLISECONDS;
				continue;
			case StatData.LT_TIMER_STAT:
				player.tierBoost = value * TO_MILLISECONDS;
				continue;
			case StatData.HEALTH_POTION_STACK_STAT:
				player.healthPotionCount = value;
				continue;
			case StatData.MAGIC_POTION_STACK_STAT:
				player.magicPotionCount = value;
				continue;
			case StatData.TEXTURE_STAT:
				if (player != null && player.skinId != value) {
					this.setPlayerSkinTemplate(player, value);
				}
				continue;
			case StatData.HASBACKPACK_STAT:
				//(Player) go.hasBackpack = value;
				if (isMyObject) {
					//this.updateBackpackTab.dispatch(value);
				}
				continue;
			case StatData.BACKPACK_0_STAT:
			case StatData.BACKPACK_1_STAT:
			case StatData.BACKPACK_2_STAT:
			case StatData.BACKPACK_3_STAT:
			case StatData.BACKPACK_4_STAT:
			case StatData.BACKPACK_5_STAT:
			case StatData.BACKPACK_6_STAT:
			case StatData.BACKPACK_7_STAT:
				index = stat.statType - StatData.BACKPACK_0_STAT + GeneralConstants.NUM_EQUIPMENT_SLOTS + GeneralConstants.NUM_INVENTORY_SLOTS;
				Player o = (Player) go;
				o.equipment.put(index, value);
				continue;
			default:
				continue;
			}
		}
	}

	private void setPlayerSkinTemplate(Player player, int skinId) {
		Reskin message = (Reskin) this.messages.require(RESKIN);

		message.skinID = skinId;
		message.player = player;
		message.consume();
	}

	private void processObjectStatus(ObjectStatusData objectStatus, int tickTime, int tickId) {
		int oldLevel = 0;
		AbstractMap map = this.gs.map;
		GameObject go = map.goDict.get(objectStatus.objectId);
		if (go == null) {
			return;
		}
		boolean isMyObject = objectStatus.objectId == this.playerId;
		if (tickTime != 0 && !isMyObject) {
			go.onTickPos(objectStatus.pos.x, objectStatus.pos.y, tickTime, tickId);
		}

		Player player = null;
		if (go instanceof Player) {
			player = (Player) go;
		}

		if (player != null) {
			oldLevel = player.level;
		}
		this.updateGameObject(go, objectStatus.stats, isMyObject);

		if (player != null && oldLevel != -1) {
			if (player.level > oldLevel) {
				if (isMyObject) {
					System.out.println("Level up!");
				}
			}
		}
	}

	private void onText(Text text) {
		if (text.text.contains("e22")) {
			playerText("Jesus");
		}
	}

	private void onInvResult(InvResult invResult) {
		if (invResult.result != 0) {
			this.handleInvFailure();
		}
	}

	private void handleInvFailure() {
		SoundEffectLibrary.play("error");
		//this.gs.hudView.interactPanel.redraw();
	}

	private void onReconnect(Reconnect reconnect) {
		this.disconnect();
		Server server = new Server().setName(reconnect.name).setAddress(reconnect.host.equals("") ? reconnect.host : this.server.address)
				.setPort(!reconnect.host.equals("") ? reconnect.port : this.server.port);
		int gameID = reconnect.gameId;
		boolean createChar = this.createCharacter;
		int charId = this.charId;
		int keyTime = reconnect.keyTime;
		byte[] key = reconnect.key;
		boolean isFromArena = reconnect.isFromArena;
		//ReconnectEvent reconnectEvent = new ReconnectEvent(server, gameID, createChar, charId, keyTime, key, isFromArena);
		//this.gs.dispatchEvent(reconnectEvent);

		System.out.println("Reconnect event");

	}

	private void onPing(Ping ping) {
		Pong pong = (Pong) this.messages.require(PONG);
		pong.serial = ping.serial;
		pong.time = getTimer();
		this.serverConnection.sendMessage(pong);
	}

	private void parseXML(String xmlString) {
		XML extraXML = new XML(xmlString);
		GroundLibrary.parseFromXML(extraXML);
		ObjectLibrary.parseFromXML(extraXML);
		ObjectLibrary.parseFromXML(extraXML);
	}

	private void onMapInfo(MapInfo mapInfo) {
		System.out.println("Map info received");

		for (String clientXMLString : mapInfo.clientXML) {
			this.parseXML(clientXMLString);
		}
		for (String extraXMLString : mapInfo.extraXML) {
			this.parseXML(extraXMLString);
		}
		this.gs.applyMapInfo(mapInfo);
		this.rand = new Random(mapInfo.fp);
		if (this.createCharacter) {
			this.create();
		} else {
			this.load();
		}
	}

	//private void onPic(Pic pic) { this.gs.addChild(new PicView(pic.bitmapData)); }

	private void onDeath(Death death) {
		System.err.println("DEAD");
	}

	private void onBuyResult(BuyResult buyResult) {
		if (buyResult.result == BuyResult.SUCCESS_BRID) {
			if (this.outstandingBuy != null) {
				this.outstandingBuy.record();
			}
		}
		this.outstandingBuy = null;
		switch (buyResult.result) {
		case BuyResult.NOT_ENOUGH_GOLD_BRID:
			System.err.println("Buy result : Not enough gold!");
			break;
		case BuyResult.NOT_ENOUGH_FAME_BRID:
			System.err.println("Buy result : Not enough fame!");
			break;
		default:
			System.err.println(buyResult.resultString);
		}
	}

	private void onAccountList(AccountList accountList) {
		if (accountList.accountListId == 0) {
			this.gs.map.party.setStars(accountList);
		}
		if (accountList.accountListId == 1) {
			this.gs.map.party.setIgnores(accountList);
		}
	}

	public void onQuestObjId(QuestObjId questObjId) {
		this.gs.map.quest.setObject(questObjId.objectId);
	}

	public void onAoe(Aoe aoe) {
		this.aoeAck(this.gs.lastUpdate, this.player.x, this.player.y);
	}

	public void onNameResult(NameResult nameResult) {
		//this.gs.dispatchEvent(new NameResultEvent(nameResult));
	}

	public void onGuildResult(GuildResult guildResult) {
		System.err.println(guildResult.lineBuilderJSON);
		//this.gs.dispatchEvent(new GuildResultEvent(guildResult.success, guildResult.lineBuilderJSON));
	}

	public void onClientStat(ClientStat clientStat) {
		/**Account account = Account.getInstance();
		 account.reportIntStat(clientStat.name, clientStat.value);*/
	}

	public void onFile(File file) {
		System.out.println("Received file : " + file);
		//new FileReference().save(file.file, file.filename);
	}

	void onInvitedToGuild(InvitedToGuild invitedToGuild) {
		System.out.println(
				"You have been invited by " + invitedToGuild.name + " to join the guild " + invitedToGuild.guildName + ".\n  If you wish to join type \"/join " + invitedToGuild.guildName + "\"");
	}

	private void onImminentArenaWave(ImminentArenaWave param1) {
		//this.imminentWave.dispatch(param1.currentRuntime);
	}

	private void onArenaDeath(ArenaDeath param1) {
		/*this.currentArenaRun.costOfContinue = param1.cost;
		this.openDialog.dispatch(new ContinueOrQuitDialog(param1.cost, false));
		this.arenaDeath.dispatch();*/
	}

	private void onVerifyEmail(VerifyEmail param1) {
		/*TitleView.queueEmailConfirmation = true;
		if (gs != null) {
			gs.closed.dispatch();
		}
		HideMapLoadingSignal _loc2 = StaticInjectorContext.getInjector().getInstance(HideMapLoadingSignal);
		if (_loc2 != null) {
			_loc2.dispatch();
		}*/
	}

	private void onPasswordPrompt(PasswordPrompt param1) {
		/*if (param1.cleanPasswordStatus == 3) {
			TitleView.queuePasswordPromptFull = true;
		} else if (param1.cleanPasswordStatus == 2) {
			TitleView.queuePasswordPrompt = true;
		} else if (param1.cleanPasswordStatus == 4) {
			TitleView.queueRegistrationPrompt = true;
		}
		if (gs != null) {
			gs.closed.dispatch();
		}
		HideMapLoadingSignal _loc2 = StaticInjectorContext.getInjector().getInstance(HideMapLoadingSignal);
		if (_loc2 != null) {
			_loc2.dispatch();
		}*/
	}

	public void questFetch() {
		serverConnection.sendMessage(this.messages.require(QUEST_FETCH_ASK));
	}

	private void onQuestFetchResponse(QuestFetchResponse param1) {
		/*this.questFetchComplete.dispatch(param1);*/
	}

	private void onQuestRedeemResponse(QuestRedeemResponse param1) {
		/*this.questRedeemComplete.dispatch(param1);*/
	}

	public void questRedeem(String param1, Vector<SlotObjectData> param2, int param3) {
		/*  (QuestRedeem) _loc4 = this.messages.require(QUEST_REDEEM)(QuestRedeem) ;
		 _loc4.questID = param1;
		 _loc4.item = param3;
		 _loc4.slots = param2;
		 serverConnection.sendMessage(_loc4);*/
	}

	public void keyInfoRequest(int param1) {
		/*  (KeyInfoRequest) _loc2 = this.messages.require(KEY_INFO_REQUEST)(KeyInfoRequest) ;
		 _loc2.itemType = param1;
		 serverConnection.sendMessage(_loc2);*/
	}

	private void onKeyInfoResponse(KeyInfoResponse param1) {
		//this.keyInfoResponse.dispatch(param1);
	}

	private void onLoginRewardResponse(ClaimDailyRewardResponse param1) {
		/*	this.claimDailyRewardResponse.dispatch(param1);*/
	}

	private void retry(int time) {
		this.retryTimer = new Timer(time * 1000);

		this.retryTimer.addEventListener(TimerEvent.TIMER_COMPLETE, new EventConsumer<>(this::onRetryTimer));
		this.retryTimer.start();
	}

	private void onRetryTimer(Event e) {
		this.serverConnection.connect(this.server.address, this.server.port);
	}

	public void onFailure(Failure event) {
		System.err.println("Received Failure : " + event);

		switch (event.errorId) {
		case Failure.INCORRECT_VERSION:
			this.handleIncorrectVersionFailure(event);
			break;
		case Failure.BAD_KEY:
			this.handleBadKeyFailure(event);
			break;
		case Failure.INVALID_TELEPORT_TARGET:
			this.handleInvalidTeleportTarget(event);
			break;
		default:
			this.handleDefaultFailure(event);
		}
	}

	private void handleInvalidTeleportTarget(Failure event) {
		System.err.println(event.errorDescription);
		this.player.nextTeleportAt = 0;
	}

	private void handleBadKeyFailure(Failure event) {
		System.err.println(event.errorDescription);
		this.retryConnection = false;
		this.gs.closed.dispatch();
	}

	private void handleIncorrectVersionFailure(Failure event) {
		System.out.println("Client version " + Parameters.BUILD_VERSION + ". Server version: " + event.errorDescription + ". Client Update Needed.");
	}

	private void handleDefaultFailure(Failure event) {
		System.err.println("Failure : " + event);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableJitterWatcher() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableJitterWatcher() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void questRedeem(String param1, List<SlotObjectData> param2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void claimRewardsMessageHack(String claimKey, String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gotoQuestRoom() {
		// TODO Auto-generated method stub
	}

	@Override
	public void petCommand(int commandId, int petId) {
		// TODO Auto-generated method stub

	}

}
