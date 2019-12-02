package cubix.levels;

import cubix.FinalProject;
import edu.utc.game.GameObject;
import edu.utc.game.Scene;
import edu.utc.game.Sound;
import edu.utc.game.Texture;
import cubix.objects.Exit;
import cubix.objects.Player;
import cubix.objects.Switch;
import cubix.objects.Trap;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static edu.utc.game.Game.ui;

public class Level implements Scene {
    //GO for background image
    private static GameObject background = new GameObject();
    //Background texture
    private static Texture bg = new Texture("res\\background.png");

    private static GameObject transition = new GameObject();

    //Current level
    protected static int sceneIndex = 0;
    protected int currentScene;

    //Platforms & walls
    protected List<GameObject> platforms = new java.util.LinkedList<>();

    //Player traps
    protected List<Trap> traps = new java.util.LinkedList<>();

    //Trap activation / de-activation switches
    protected List<Switch> switches = new java.util.LinkedList<>();

    //Player cubix.objects
    protected Player redPlayer;
    protected Player bluePlayer;

    //Level exits
    protected Exit blueExit;
    protected Exit redExit;

    //List of platforms, traps, exits, walls, and players
    protected List<GameObject> colliders = new java.util.LinkedList<>();

    private static boolean starting = true;
    private static boolean exiting = false;
    private static int transitionTimer = 0;
    private static Sound BGM = new Sound("res\\BGM.wav");

    static
    {
        background.getHitbox().setBounds(0, 0, ui.getWidth(), ui.getHeight());
        transition.getHitbox().setBounds(0,0,ui.getWidth(),ui.getHeight());
        BGM.play();
    }

    {
        currentScene = sceneIndex++;
    }

    /**
     * Toggle which player is active
     * Only toggles if the currently inactive player is also not kinematic
     */
    public static void togglePlayers()
    {
        Player blue = getPlayer(Player.COLORS.BLUE);
        Player red = getPlayer(Player.COLORS.RED);
        // If the first player is active and the second isn't frozen
        if (blue.isActive() && !red.isKinematic())
        {
            //Toggle
            blue.setActive(false);
            red.setActive(true);
        }
        //If the second player is active and the first isn't frozen
        else if (!blue.isKinematic())
        {
            //Toggle
            blue.setActive(true);
            red.setActive(false);
        }
        // If it gets this far the player cannot be toggled
    }

    @Override
    public void onKeyEvent(int key, int scancode, int action, int mods) {
        if (key== GLFW.GLFW_KEY_SPACE & action==GLFW.GLFW_PRESS)
        {
            togglePlayers();
        }

        if (key== GLFW.GLFW_KEY_R & action==GLFW.GLFW_PRESS)
        {
            redPlayer.respawn();
            bluePlayer.respawn();
        }
    }

    @Override
    public Scene drawFrame(int delta) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        GL11.glColor3f(1, 1, 1);
        bg.draw(background);

        //Update moving parts of the environment
        //Switches control Traps, so they need to be updated in this order
        for (Switch s : switches)
        {
            s.update(delta);
        }

        //Traps can capture players so they need to be updated before players
        for (Trap t : traps)
        {
            t.update(delta);
        }

        //Update each player
        redPlayer.update(delta);
        bluePlayer.update(delta);

        //Traps are drawn behind everything else
        for (Trap t : traps)
        {
            t.draw();
        }

        // Draw platforms & walls
        for (GameObject p : platforms){
            p.draw();
        }

        //Draw players
        bluePlayer.draw();
        redPlayer.draw();

        //Switches are drawn in front of the player
        for (Switch s : switches)
        {
            s.draw();
        }

        //Exits have to be drawn after players for glow effect to show on them
        blueExit.draw();
        redExit.draw();

        //Win condition
        if (bluePlayer.onExit() && redPlayer.onExit()) {
            if (!exiting)
            {
                exiting = true;
                transitionTimer = 0;
            }
        }

        if (starting)
        {
            if (transitionTimer <= 2000) {
                if (transitionTimer < 100)
                {
                    bluePlayer.respawn();
                    redPlayer.respawn();
                }
                transition.setColor(1f, 1f, 1f, (1 - transitionTimer / 2000f));
                transitionTimer += delta;
            }
            else {
                starting = false;
                transitionTimer = 0;
            }
            transition.draw();
        }

        if (exiting)
        {
            if (transitionTimer <= 1500) {
                transition.setColor(1f, 1f, 1f, transitionTimer / 1500f);
                transition.draw();
                transitionTimer += delta;
            }
            else {
                transition.draw();
                bluePlayer.respawn();
                redPlayer.respawn();
                exiting = false;
                starting = true;
                transitionTimer = 0;
                FinalProject.currentIndex = this.currentScene + 1;
                return cubix.FinalProject.scenes().get(this.currentScene + 1);
            }
        }
        return this;
    }

    public static Player getPlayer(Player.COLORS color)
    {
        switch (color)
        {
            case RED:
                return FinalProject.currentLevel().redPlayer;
            default:
                return FinalProject.currentLevel().bluePlayer;
        }
    }

    public List<Trap> getTraps()
    {
        return traps;
    }
}
