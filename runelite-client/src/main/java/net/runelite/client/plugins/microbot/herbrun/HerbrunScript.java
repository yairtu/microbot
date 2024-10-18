package net.runelite.client.plugins.microbot.herbrun;

import java.awt.*;
import java.awt.event.KeyEvent;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
//import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;


import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.herbrun.HerbrunInfo.*;


public class HerbrunScript extends Script {

    // Define the herb patch locations
    private static final WorldPoint trollheimHerb = new WorldPoint(2826, 3693, 0);
    private static final WorldPoint catherbyHerb = new WorldPoint(2812, 3465, 0);
    private static final WorldPoint morytaniaHerb = new WorldPoint(3604, 3529, 0);
    private static final WorldPoint varlamoreHerb = new WorldPoint(1582, 3093, 0);
    private static final WorldPoint hosidiusHerb = new WorldPoint(1739, 3552, 0);
    //    private static final WorldPoint ardougneHerb = new WorldPoint(2669, 3374, 0); No need talk walk there from teleport
    private static final WorldPoint cabbageHerb = new WorldPoint(3058, 3310, 0);
    private static final WorldPoint farmingGuildHerb = new WorldPoint(1239, 3728, 0);
    private static final WorldPoint weissHerb = new WorldPoint(2847, 3935, 0);

    //herb patch Object ID
    private static final int trollheimHerbPatchID = 18816;
    private static final int catherbyHerbPatchID = 8151;
    private static final int morytaniaHerbPatchID = 8153;
    private static final int varlamoreHerbPatchID = 50697;
    private static final int hosidiusHerbPatchID = 27115;
    private static final int ardougneHerbPatchID = 8152; //leprechaun 0
    private static final int cabbageHerbPatchID = 8150; //50698?
    private static final int farmingGuildHerbPatchID = 33979;
    private static final int weissHerbPatchID = 33176;

    //Leprechaun IDs:
    //IDS that are 0: Ardougne, Farming guild, morytania, hosidius, catherby, falador, weiss
    private static final int varlamoreLeprechaunID = NpcID.TOOL_LEPRECHAUN_12765;
    private static final int trollHeimLeprechaunID = NpcID.TOOL_LEPRECHAUN_757;

    //seed type
//    public static ItemID seeds = ;
    public static boolean test = false;

    public HerbrunScript() throws AWTException {
    }

    public boolean run(HerbrunConfig config) {

        int seedToPlant = config.SEED().getItemId();
        int cloak = config.CLOAK().getItemId();
        int ring = config.RING().getItemId();

        Microbot.enableAutoRunOn = false;
        botStatus = states.GEARING;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;


                switch (botStatus) {
                    case GEARING:
                        //Bank everything and withdraw all farming equipment
                        System.out.println("Gearing up");
                        if (!Rs2Bank.isOpen()) {
                            System.out.println("Bank opened");
                            Rs2Bank.useBank();
                            Rs2Bank.depositAll();
                            Rs2Bank.depositEquipment();
                            if (config.GRACEFUL()) {
                                equipGraceful();
                            }
                        }
                        withdrawHerbSetup(config);
                        Rs2Bank.closeBank();
                        sleep(100);
                        System.out.println("Gearing complete");
                        sleep(200, 800);
                        botStatus = states.TROLLHEIM_TELEPORT;
                        break;
                    case TROLLHEIM_TELEPORT:
                        System.out.println("Current state: TROLLHEIM_TELEPORT");
                        handleTeleportToTrollheim();
                        break;
                    case TROLLHEIM_WALKING_TO_PATCH:
                        System.out.println("Current state: TROLLHEIM_WALKING_TO_PATCH");
                        handleWalkingToPatch(trollheimHerb, states.TROLLHEIM_HANDLE_PATCH);
                        break;
                    case TROLLHEIM_HANDLE_PATCH:
                        if (!Rs2Player.isWalking()) {
                            System.out.println("Current state: TROLLHEIM_HANDLE_PATCH");
                            printHerbPatchActions(trollheimHerbPatchID);
                            handleHerbPatch(trollheimHerbPatchID, seedToPlant, config, trollHeimLeprechaunID);
                            addCompost(config, trollheimHerbPatchID);
                            plantSeed(trollheimHerbPatchID, seedToPlant, states.CATHERBY_TELEPORT);
                            break;
                        }
                    case CATHERBY_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            System.out.println("Current state: CATHERBY_TELEPORT");
                            handleTeleportToCatherby();
                        }
                    case CATHERBY_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            System.out.println("Current state: CATHERBY_WALKING_TO_PATCH");
                            handleWalkingToPatch(catherbyHerb, states.CATHERBY_HANDLE_PATCH);
                        }
                    case CATHERBY_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            System.out.println("Current state: CATHERBY_HANDLE_PATCH");
                            printHerbPatchActions(catherbyHerbPatchID);
                            handleHerbPatch(catherbyHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, catherbyHerbPatchID);
                            plantSeed(catherbyHerbPatchID, seedToPlant, states.MORYTANIA_TELEPORT);
                        }
                        break;
                    case MORYTANIA_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleTeleportToMorytania();
                        }
                        break;
                    case MORYTANIA_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleWalkingToPatch(morytaniaHerb, states.MORYTANIA_HANDLE_PATCH);
                            break;
                        }
                    case MORYTANIA_HANDLE_PATCH:
                        System.out.println("Handling Morytania patch");
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            printHerbPatchActions(morytaniaHerbPatchID);
                            handleHerbPatch(morytaniaHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, morytaniaHerbPatchID);
                            plantSeed(morytaniaHerbPatchID, seedToPlant, states.VARLAMORE_TELEPORT);
                            break;
                        }
                    case VARLAMORE_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleTeleportToVarlamore();
                        }
                        break;
                    case VARLAMORE_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleWalkingToPatch(varlamoreHerb, states.VARLAMORE_HANDLE_PATCH);
                        }
                        break;
                    case VARLAMORE_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            printHerbPatchActions(varlamoreHerbPatchID);
                            handleHerbPatch(varlamoreHerbPatchID, seedToPlant, config, varlamoreLeprechaunID);
                            addCompost(config, varlamoreHerbPatchID);
                            plantSeed(varlamoreHerbPatchID, seedToPlant, states.HOSIDIUS_TELEPORT);
                            break;
                        }
                    case HOSIDIUS_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleTeleportToHosidius();
                        }
                        break;
                    case HOSIDIUS_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleWalkingToPatch(hosidiusHerb, states.HOSIDIUS_HANDLE_PATCH);
                        }
                        break;
                    case HOSIDIUS_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            printHerbPatchActions(hosidiusHerbPatchID);
                            handleHerbPatch(hosidiusHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, hosidiusHerbPatchID);
                            plantSeed(hosidiusHerbPatchID, seedToPlant, states.ARDOUGNE_TELEPORT);
                        }
                        break;
                    case ARDOUGNE_TELEPORT:
                        handleTeleportToArdougne(config);
                        break;
                    case ARDOUGNE_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Rs2Player.isInteracting()) {
                            printHerbPatchActions(ardougneHerbPatchID);
                            handleHerbPatch(ardougneHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, ardougneHerbPatchID);
                            plantSeed(ardougneHerbPatchID, seedToPlant, states.FALADOR_TELEPORT);
                        }
                        break;
                    case FALADOR_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleTeleportToFalador(config);
                        }
                        break;
                    case FALADOR_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleWalkingToPatch(cabbageHerb, states.FALADOR_HANDLE_PATCH);
                        }
                        break;
                    case FALADOR_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            printHerbPatchActions(cabbageHerbPatchID);
                            handleHerbPatch(cabbageHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, cabbageHerbPatchID);
                            plantSeed(cabbageHerbPatchID, seedToPlant, botStatus = states.WEISS_TELEPORT);
                        }
                        break;
                        case WEISS_TELEPORT:
                            if (!Rs2Player.isMoving() &&
                                    !Rs2Player.isAnimating() &&
                                    !Microbot.getClient().getLocalPlayer().isInteracting()) {
                                handleTeleportToWeiss();
                            }
                            break;
                    case GUILD_TELEPORT:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleTeleportToGuild();
                        }
                        break;
                    case GUILD_WALKING_TO_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            handleWalkingToPatch(farmingGuildHerb, states.GUILD_HANDLE_PATCH);
                        }
                        break;
                    case GUILD_HANDLE_PATCH:
                        if (!Rs2Player.isMoving() &&
                                !Rs2Player.isAnimating() &&
                                !Microbot.getClient().getLocalPlayer().isInteracting()) {
                            printHerbPatchActions(farmingGuildHerbPatchID);
                            handleHerbPatch(farmingGuildHerbPatchID, seedToPlant, config, 0);
                            addCompost(config, farmingGuildHerbPatchID);
                            plantSeed(farmingGuildHerbPatchID, seedToPlant, botStatus = states.FINISHED);
                        }
                        break;
                    case FINISHED:
                        shutdown();  // Optionally handle completion
                        break;
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void checkBeforeWithdrawAndEquip(int itemId) {
        if (!Rs2Equipment.isWearing(itemId)) {
            Rs2Bank.withdrawAndEquip(itemId);
        }
    }

    private void checkBeforeWithdrawAndEquip(String itemName) {
        if (!Rs2Equipment.isWearing(itemName)) {
            Rs2Bank.withdrawAndEquip(itemName);
        }
    }

    private boolean interactWithObject(int objectId) {
        Rs2GameObject.interact(objectId);
        sleepUntil(Rs2Player::isInteracting);
        sleepUntil(() -> !Rs2Player.isInteracting());
        return true;
    }

    private void equipGraceful() {
        checkBeforeWithdrawAndEquip("GRACEFUL HOOD");
        checkBeforeWithdrawAndEquip("GRACEFUL CAPE");
        checkBeforeWithdrawAndEquip("GRACEFUL BOOTS");
        checkBeforeWithdrawAndEquip("GRACEFUL GLOVES");
        checkBeforeWithdrawAndEquip("GRACEFUL TOP");
        checkBeforeWithdrawAndEquip("GRACEFUL LEGS");
    }

    private void withdrawHerbSetup(HerbrunConfig config) {
        Rs2Bank.withdrawX(config.SEED().getItemId(), 8);
        if (config.COMPOST()) {
            Rs2Bank.withdrawOne(ItemID.BOTTOMLESS_COMPOST_BUCKET_22997);
        } else {
            Rs2Bank.withdrawX(ItemID.ULTRACOMPOST, 8);
        }
        Rs2Bank.withdrawOne(config.CLOAK().getItemId());
        Rs2Bank.withdrawOne(ItemID.SEED_DIBBER);
        Rs2Bank.withdrawOne(ItemID.SPADE);
        Rs2Bank.withdrawOne(ItemID.ECTOPHIAL);
        if (Rs2Bank.hasItem(ItemID.PERFECTED_QUETZAL_WHISTLE)) {
            Rs2Bank.withdrawOne(ItemID.PERFECTED_QUETZAL_WHISTLE);
        } else if (Rs2Bank.hasItem(ItemID.ENHANCED_QUETZAL_WHISTLE)) {
            Rs2Bank.withdrawOne(ItemID.ENHANCED_QUETZAL_WHISTLE);
        } else if (Rs2Bank.hasItem(ItemID.BASIC_QUETZAL_WHISTLE)) {
            Rs2Bank.withdrawOne(ItemID.BASIC_QUETZAL_WHISTLE);
        }
        Rs2Bank.withdrawOne(ItemID.STONY_BASALT);
        Rs2Bank.withdrawOne(ItemID.XERICS_TALISMAN);
        Rs2Bank.withdrawOne(ItemID.RAKE);
        Rs2Bank.withdrawX(ItemID.AIR_RUNE, 5);
        Rs2Bank.withdrawX(ItemID.LAW_RUNE, 1);
        Rs2Bank.withdrawOne(ItemID.SKILLS_NECKLACE6);
        Rs2Bank.withdrawOne(config.RING().getItemId());
        checkBeforeWithdrawAndEquip("Magic secateurs");
    }

    private boolean trollheimTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            boolean success = Rs2Inventory.interact(ItemID.STONY_BASALT, "Troll Stronghold");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return true;
    }

    private void handleTeleportToTrollheim() {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Trollheim");
            boolean success = trollheimTeleport();
            if (success) {
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving());
                System.out.println("Arrived at Trollheim teleport spot.");
                botStatus = states.TROLLHEIM_WALKING_TO_PATCH;
            }
        }
    }


    private boolean catherbyTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Catherby");
            boolean success = Rs2Magic.cast(MagicAction.CAMELOT_TELEPORT);
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return false;
    }

    private void handleTeleportToCatherby() {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Catherby...");
            boolean success = catherbyTeleport();

            if (success) {
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving());
                System.out.println("Arrived at Catherby teleport spot.");
                botStatus = states.CATHERBY_WALKING_TO_PATCH;
            } else {
                System.out.println("Teleport to Catherby failed!");
            }
        }
    }


    private boolean morytaniaTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Morytania");
            boolean success = Rs2Inventory.interact(ItemID.ECTOPHIAL, "empty");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return false;
    }


    private void handleTeleportToMorytania() {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Morytania...");
            boolean success = morytaniaTeleport();  // Perform the teleport

            if (success) {
                // Wait until the player has stopped animating and moving
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                System.out.println("Arrived at Morytania teleport spot.");
                botStatus = states.MORYTANIA_WALKING_TO_PATCH;  // Move to the next state
            } else {
                System.out.println("Teleport to Morytania failed!");
            }
        }
    }

    private boolean varlamoreTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Varlamore");
            boolean success = Rs2Inventory.interact(ItemID.PERFECTED_QUETZAL_WHISTLE, "Signal");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return true;
        }
        return false;
    }


    private void handleTeleportToVarlamore() {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Varlamore...");
            boolean success = varlamoreTeleport();  // Perform teleportation
            if (success) {
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving());
                System.out.println("Arrived at Varlamore teleport spot.");
                botStatus = states.VARLAMORE_WALKING_TO_PATCH;
            } else {
                System.out.println("Teleport to Varlamore failed!");
            }
        }
    }

    private boolean hosidiusTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Hosidius");
            boolean success = Rs2Inventory.interact(ItemID.XERICS_TALISMAN, "rub");
            sleep(500, 900);
            Rs2Keyboard.keyPress('2');
//            Rs2Widget.clickWidget("2: Xeric's glade");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return false;
    }

    private void handleTeleportToHosidius() {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Hosidius...");
            boolean success = hosidiusTeleport();

            if (success) {
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving());
                System.out.println("Arrived at Hosidius teleport spot.");
                botStatus = states.HOSIDIUS_WALKING_TO_PATCH;
            } else {
                System.out.println("Teleport to Hosidius failed!");
            }
        }
    }

    private boolean ardougneTeleport(HerbrunConfig config) {
        if (!Rs2Player.isAnimating() && !Rs2Player.isMoving() && !Rs2Player.isInteracting()) {
            System.out.println("Teleporting to Ardougne farm patch");
            boolean success = Rs2Inventory.interact(config.CLOAK().getItemId(), "Farm Teleport");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            sleep(100, 400);
            return success;
        }
        return false;
    }

    private void handleTeleportToArdougne(HerbrunConfig config) {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Ardougne...");
            boolean success = ardougneTeleport(config);
            if (success) {
                // Wait until the player stops animating and moving after the teleport
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);

                // Ensure the teleport was successful before moving to the next state
                if (!Rs2Player.isAnimating() && !Rs2Player.isMoving()) {
                    System.out.println("Arrived at Ardougne teleport spot.");
                    botStatus = states.ARDOUGNE_HANDLE_PATCH; // Move to the next step only if teleport is complete
                } else {
                    System.out.println("Teleport to Ardougne failed! Retrying...");
                    botStatus = states.ARDOUGNE_TELEPORT; // Retry teleport if failed
                }
            }
        }
    }

    private boolean faladorTeleport(HerbrunConfig config) {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Falador herb patch");
            boolean success = Rs2Inventory.interact(config.RING().getItemId(), "Teleport");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            sleep(200, 600);
            return true;
        }
        return false;
    }

    private void handleTeleportToFalador(HerbrunConfig config) {
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Falador...");
            boolean success = faladorTeleport(config);  // Perform the teleport

            if (success) {
                // Wait until the player has stopped animating and moving
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving());
                System.out.println("Arrived at Falador teleport spot.");
                botStatus = states.FALADOR_WALKING_TO_PATCH;  // Move to the next state
            } else {
                System.out.println("Teleport to Falador failed!");
            }
        }
    }

    private boolean guildTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to the Farming guild");
            boolean success = Rs2Inventory.interact(ItemID.SKILLS_NECKLACE6, "rub");
            sleep(500, 900);
            Rs2Keyboard.keyPress('6');
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return false;
    }

    private void handleTeleportToGuild() {
        boolean hasTeleported = false;
        if (!hasTeleported) {
            System.out.println("Teleporting to the Farming guild...");
            guildTeleport();  // Perform Hosidius teleport
            hasTeleported = true;
        }
        botStatus = states.GUILD_WALKING_TO_PATCH;
    }

    private boolean weissTeleport() {
        sleep(100);
        if (!Rs2Player.isAnimating()) {
            System.out.println("Teleporting to Weiss");
            boolean success = Rs2Inventory.interact(ItemID.STONY_BASALT, "Weiss");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            return success;
        }
        return false;
    }

    private void handleTeleportToWeiss() {
        boolean hasTeleported = false;
        if (!hasTeleported) {
            System.out.println("Teleporting to Weiss...");
            weissTeleport();  // Perform WEISS teleport
            hasTeleported = true;
        }
        botStatus = states.WEISS_HANDLE_PATCH;
    }


    private void handleWalkingToPatch(WorldPoint location, states nextState) {
        System.out.println("Walking to the herb patch...");

        // Start walking to the location
        Rs2Walker.walkTo(location);
        Rs2Player.waitForWalking();
        // Wait until the player reaches within 2 tiles of the location and has stopped moving
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(location) < 10);
        if (!Rs2Player.isWalking()) {
            System.out.println("Arrived at herb patch.");
            botStatus = nextState;
        }
    }


    private void handleHerbPatch(int patchId, int seedToPlant, HerbrunConfig config, int leprechaunID) {
        // Define possible actions the herb patch could have
        if (!Rs2Player.isMoving() &&
                !Rs2Player.isWalking() &&
                !Rs2Player.isAnimating() &&
                !Rs2Player.isInteracting()) {
            String[] possibleActions = {"pick", "rake", "Clear", "Inspect"};

            GameObject herbPatch = null;
            String foundAction = null;

            // Loop through the possible actions and try to find the herb patch with any valid action
            for (String action : possibleActions) {
                herbPatch = Rs2GameObject.findObjectByImposter(patchId, action);  // Find object by patchId and action
                if (herbPatch != null) {
                    foundAction = action;
                    break;  // Exit the loop once we find the patch with a valid action
                }
            }

            // If no herb patch is found, print an error and return
            if (herbPatch == null) {
                System.out.println("Herb patch not found with any of the possible actions!");
                return;
            }

            // Handle the patch based on the action found
            switch (foundAction) {
                case "pick":
                    handlePickAction(herbPatch, patchId, leprechaunID, config);
                    break;
                case "rake":
                    handleRakeAction(herbPatch);
                    break;
                case "Clear":
                    handleClearAction(herbPatch);
                    break;
                default:
                    System.out.println("Unexpected action found on herb patch: " + foundAction);
                    break;
            }

        }

    }

    private void handlePickAction(GameObject herbPatch, int patchId, int leprechaunID, HerbrunConfig config) {
        System.out.println("Picking herbs...");

        // Pick the herbs first
        if (!Rs2Player.isMoving() && !Rs2Player.isWalking()) {
            Rs2GameObject.interact(herbPatch, "pick");

        }
        Rs2Player.waitForAnimation();
        sleepUntil(() -> !Rs2GameObject.hasAction(Rs2GameObject.findObjectComposition(patchId), "Pick") ||
                !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isMoving());
        // Now that the player has finished picking herbs, check if there's more to pick
        if (Rs2GameObject.hasAction(Rs2GameObject.findObjectComposition(patchId), "pick") && Rs2Inventory.isFull()) {
            System.out.println("Noting herbs with tool leprechaun...");
            Rs2Inventory.useItemOnNpc(config.SEED().getHerbId(), leprechaunID); // Use picked herbs on tool leprechaun (NPC ID 0)
            Rs2Player.waitForAnimation();

            // Pick the remaining herbs
            if (!Rs2Player.isMoving() &&
                    !Rs2Player.isAnimating() &&
                    !Rs2Player.isInteracting() && !Rs2Player.isWalking() && Rs2Inventory.contains(config.SEED().getHerbId() + 1)) {
                Rs2GameObject.interact(herbPatch, "pick");
                Rs2Player.waitForAnimation();
                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isInteracting()); // Wait until the player stops animating
                sleepUntil(() -> !Rs2GameObject.hasAction(Rs2GameObject.findObjectComposition(patchId), "Pick"));
            }
        }

        sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving() && !Rs2Player.isInteracting(), 200);

    }

    private void handleRakeAction(GameObject herbPatch) {
        System.out.println("Raking the patch...");

        // Rake the patch
        Rs2GameObject.interact(herbPatch, "rake");

        Rs2Player.waitForAnimation();
        sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isInteracting());

        // Drop the weeds (assuming weeds are added to the inventory)
        if (!Rs2Player.isMoving() &&
                !Rs2Player.isAnimating() &&
                !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            System.out.println("Dropping weeds...");
            Rs2Inventory.dropAll(ItemID.WEEDS);
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isInteracting());
        }
    }

    private void handleClearAction(GameObject herbPatch) {
        System.out.println("Clearing the herb patch...");

        // Try to interact with the patch using the "clear" action
        boolean interactionSuccess = Rs2GameObject.interact(herbPatch, "clear");
        Rs2Player.waitForAnimation();
        sleepUntil(() -> !Rs2Player.isAnimating());

        if (!interactionSuccess) {
            System.out.println("Failed to interact with the herb patch to clear it.");
            return;
        }

        // Wait for the clearing animation to finish
        Rs2Player.waitForAnimation();
        sleepUntil(() -> !Rs2Player.isAnimating() && Rs2Player.isInteracting() && Rs2Player.isWalking());
    }

    private void printHerbPatchActions(int patchId) {
        GameObject herbPatch = Rs2GameObject.findObjectByImposter(patchId, "clear");
        if (herbPatch == null) {
            System.out.println("Herb patch not found for ID: " + patchId);
            return;
        }

        ObjectComposition herbPatchComposition = Rs2GameObject.findObjectComposition(patchId);
        System.out.println("Available actions for herb patch:");
        for (String action : herbPatchComposition.getActions()) {
            if (action != null) {
                System.out.println(action);  // Print each available action
            }
        }
    }

    private void addCompost(HerbrunConfig config, int patchId) {

        // Apply bottomless compost bucket on the herb patch
        if (!Rs2Player.isMoving() &&
                !Rs2Player.isAnimating() &&
                !Rs2Player.isInteracting() &&
                !Rs2Player.isWalking()) {
            System.out.println("Applying compost...");
            if (config.COMPOST()) {
                Rs2Inventory.use(ItemID.BOTTOMLESS_COMPOST_BUCKET_22997);
                Rs2GameObject.interact(patchId, "use");
                sleepUntil(Rs2Player::isInteracting);
                sleepUntil(() -> !Rs2Player.isInteracting());
            } else {
                Rs2Inventory.use(ItemID.ULTRACOMPOST);
                Rs2GameObject.interact(patchId, "use");
                sleepUntil(Rs2Player::isInteracting);
                sleepUntil(() -> !Rs2Player.isInteracting());
            }
        }
    }

    private void plantSeed(int patchId, int seedToPlant, states state) {
        if (!Rs2Player.isMoving() &&
                !Rs2Player.isAnimating() &&
                !Rs2Player.isInteracting() &&
                !Rs2Player.isWalking()) {

            System.out.println("Planting seeds...");
            Rs2Inventory.use(seedToPlant);
            Rs2GameObject.interact(patchId, "use");
            sleepUntil(Rs2Player::isInteracting);
            sleepUntil(() -> !Rs2Player.isInteracting());
            botStatus = state;
        }
    }

}