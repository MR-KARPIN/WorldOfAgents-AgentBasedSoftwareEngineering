package es.upm.emse.absd;

import static es.upm.emse.absd.team1.agents.Utils.*;

import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.*;
import es.upm.emse.absd.team1.agents.platform.BuildingManager.AgBuildingManager;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import es.upm.emse.absd.team1.agents.platform.RegistrationManager.AgRegistrationManager;
import es.upm.emse.absd.team1.agents.platform.ResourceManager.AgResourceManager;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.AgTribeAccountant;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import es.upm.emse.absd.team1.agents.tribe.AgTribe;

import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * @author
 * @version     1.2.1
 *
 * Main Class.
 */
public class Main {

    private static jade.wrapper.AgentContainer cc;
    private static jade.core.Runtime rt;

    // Executed from Singletons.
    private static void loadBoot(){

        // Load JADE with GUI for debugging.
        Boot.main(new String[] {"-gui"});

        // Create a default profile
        rt = jade.core.Runtime.instance();

        System.out.println("Agent Containers created...");

    }

    private static void loadMyPlatformAgents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, "Platform-Container");
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            cc.createNewAgent("PhaseManager", AgPhaseManager.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("RegistrationManager", AgRegistrationManager.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("TribeAccountant", AgTribeAccountant.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("UnitManager", AgUnitManager.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("MapManager", AgMapManager.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("ResourceManager", AgResourceManager.class.getName(), new Object[]{"0"}).start();
            cc.createNewAgent("BuildingManager", AgBuildingManager.class.getName(), new Object[]{"0"}).start();

        } catch (StaleProxyException e) {
            System.err.println("Error creating platform agents!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void loadMyTribeAgents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, TRIBE_0);
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            AgentController tribeController1 = cc.createNewAgent("Team1", AgTribe.class.getName(), new Object[]{"0"});
            tribeController1.start();
            //cc.createNewAgent("Team2", AgTribe.class.getName(), new Object[]{"0"}).start();
            //cc.createNewAgent("Team3", AgTribe.class.getName(), new Object[]{"0"}).start();
            //Thread.sleep(22000);
            //cc.createNewAgent("Team4", AgTribe.class.getName(), new Object[]{"0"}).start();

        } catch (StaleProxyException e) {
            System.err.println("Error creating my tribe agents!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadTribe1Agents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, TRIBE_1);
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            cc.createNewAgent("Team1", es.upm.emse.absd.team1.agents.tribe.AgTribe.class.getName(), new Object[]{"0"}).start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating agents from tribe Demo!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadTribe2Agents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, TRIBE_2);
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            cc.createNewAgent("Team2", es.upm.emse.absd.team2.agents.tribe.AgTribe.class.getName(), new Object[]{"0"}).start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating agents from tribe Demo!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadTribe3Agents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, TRIBE_3);
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            cc.createNewAgent("Team3", es.upm.emse.absd.team3.agents.tribe.tribecontroller.AgTribe.class.getName(), new Object[]{"0"}).start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating agents from tribe Demo!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void loadTribe4Agents() {
        // now set the profiles to start the containers
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, TRIBE_4);
        cc = rt.createAgentContainer(agentContainerProfile);

        try {
            cc.createNewAgent("Team4", es.upm.emse.absd.team4.agents.tribe.AgTribe.class.getName(), new Object[]{"0"}).start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating agents from tribe Demo!!!");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void printHelp() {
        System.out.println("""
            --------------------------------------
            --- EMSE-ABSD JADE World of Agents ---
            --------------------------------------
            Agents:
                + Platform:
                    - AgPlatform: An agent that responds when its called by its name.
                + Tribe:
                    - AgTribe: An agent that try to talk with his/her colleague.
            Usage: java -jar woa.jar [options]
                + options:
                    -h, --help  Prints help
                    -d, -debug  Run JADE agents from the compilation
                    -b, -build  Run agents from generated jars
            """
        );
    }

    public static void main(String[] args) {

        System.setProperty("java.util.logging.SimpleFormatter.format", ANSI_WHITE + "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }

        System.out.println("Starting...");
        loadBoot();
        loadMyPlatformAgents();

        if (args[0].equals("-d") || args[0].equals("-debug")) {
            loadMyTribeAgents();
            modeRun="debug";
        } else if (args[0].equals("-b") || args[0].equals("-build")) {
            modeRun="build";
            loadTribe1Agents();
            loadTribe2Agents();
            loadTribe3Agents();
            loadTribe4Agents();
        }

        System.out.println("MAS loaded...");

    }

}
