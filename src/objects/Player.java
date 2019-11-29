package objects;

import edu.utc.game.Game;
import edu.utc.game.GameObject;
import edu.utc.game.Texture;
import edu.utc.game.XYPair;
import javafx.scene.shape.ArcTo;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unchecked")
public class Player extends GameObject {
    private XYPair<Float> velocity;
    private XYPair<Float> accel = new XYPair<>(0f,5f);
    public enum COLORS {RED, BLUE}
    private COLORS color;
    private static Texture blueTexture = new Texture("res\\BluePlayer.png");
    private static Texture redTexture = new Texture("res\\RedPlayer.png");
    private boolean kinematic = false;
    private boolean active = true;
    private boolean grounded = false;
    private List<GameObject> colliders = new java.util.LinkedList();
    private XYPair<Integer> startPos = new XYPair<>(0,0);

    {
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.velocity = new XYPair<>(0f, 0f);
    }


    public Player(int x, int y, COLORS color){
        int size = 50;
        this.hitbox.setBounds(x,y,size,size);
        startPos.x = x;
        startPos.y = y;
        this.color = color;
        this.active = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public boolean isKinematic() {
        return kinematic;
    }

    public void setColliders(List<GameObject> colliders)
    {
        this.colliders = colliders;
    }

    public void respawn()
    {
        this.hitbox.x = startPos.x;
        this.hitbox.y = startPos.y;
        this.velocity.x = 0f;
        this.velocity.y = 0f;
    }


    @Override
    public void update(int delta) {

        Float time = delta / 1000f;
        if (this.hitbox.x < 0)
            respawn();
        if (this.hitbox.x > Game.ui.getWidth())
            respawn();
        if (this.hitbox.y > Game.ui.getHeight())
            respawn();
        if (this.kinematic)
            return;

        if (grounded)
        {
            accel.y = 0f;
            velocity.y = 0f;
        }
        else {
            accel.y = 3.5f;
        }

        // update velocity based on acceleration
        velocity.x += accel.x*time;
        velocity.y += accel.y*time;

        if (this.active && Game.ui.keyPressed(GLFW_KEY_W))
        {
            if (grounded)
                this.velocity.y = -0.85f;
        }

        // update position based on velocity
        double x = delta*velocity.x;
        double y = delta*velocity.y;

        grounded = false;


        this.translate((int) x, (int) y);

        float speed = 0.5f;
        if (this.active && Game.ui.keyPressed(GLFW_KEY_A))
        {
            this.translate((int)(-speed *delta), 0);
        }

        if (this.active && Game.ui.keyPressed(GLFW_KEY_D))
        {
            this.translate((int)(speed *delta), 0);
        }


    }

    public void checkCollisions()
    {
        int newX = this.hitbox.x;
        int newY = this.hitbox.y;
        for (GameObject p : colliders)
        {
            if (p.equals(this))
                continue;
            if (this.intersects(p))
            {
                if (this.hitbox.y < p.getHitbox().y) {
                    this.accel.y = 0f;
                    this.velocity.y = 0f;

                    System.out.println("Fell onto something!");

                    newY = p.getHitbox().y - this.hitbox.height;

                    grounded = true;
                }

                else if (this.hitbox.y > p.getHitbox().y) {// && this.hitbox.y + this.hitbox.height > p.getHitbox().y  + p.getHitbox().height) {
                    this.velocity.y = 0f;

                    System.out.println("Hit my head!");

                    newY = p.getHitbox().y + p.getHitbox().height;

                }

            }
            if (this.hitbox.x < 0) {
                newX = 0;
            }
            if (this.hitbox.x + this.hitbox.width > Game.ui.getWidth()) {
                newX = Game.ui.getWidth() - this.hitbox.width;
            }
        }
        this.hitbox.setLocation(newX, newY);
    }

    public void translate(int x, int y)
    {
        Rectangle test = new Rectangle(this.hitbox.x + x, this.hitbox.y+y, this.hitbox.width, this.hitbox.height);
        int deltaX = x;
        int deltaY = y;
        test.translate(deltaX, deltaY);
        for (GameObject coll : colliders)
        {
            if (test.intersects(coll.getHitbox()))
            {
                int margin = 1;
                if (coll.equals(this)) { continue;}
                Rectangle hit = coll.getHitbox();
                // If player right overlaps object left
                // We hit on the left side so back out to the left
                // If player bottom overlaps object top
                // We hit on the top side of the object so back out to the top
                if (test.y + test.height > hit.y && deltaY > 0)
                {
                    deltaY = hit.y - this.hitbox.y - this.hitbox.height;
                    grounded = true;
                    //System.out.println("Object top");
                }
                else if (test.y + test.height > hit.y + hit.height && deltaY < 0)
                {
                    deltaY = hit.y + hit.height - this.hitbox.y;
                    this.velocity.y = 0f;
                    //System.out.println("Object bottom");
                }
                if (test.x + test.width > hit.x && deltaX > 0)
                {
                    deltaX = hit.x - this.hitbox.x  - this.hitbox.width;
                    //System.out.println("Object left");
                }
                // If player left overlaps object right
                // We hit on the right side of the object so back out to the right
                if (test.x < hit.x + hit.width && deltaX < 0)
                {
                    deltaX = hit.x + hit.width - this.hitbox.x;
                    //System.out.println("Object right");
                }
                if (coll.getClass()==Exit.class)
                {
                    Exit exit = (Exit) coll;
                    if (exit.getColor() == this.color)
                    {
                        System.out.println(this.color.toString()+" exit ready!");
                    }
                }
            }
        }
        this.hitbox.translate(deltaX, deltaY);
    }

    public void draw()
    {
        Texture currentTexture;
        if (color == COLORS.RED) {
            currentTexture = redTexture;
        } else {
            currentTexture = blueTexture;
        }

        float adjust = 0f;
        if (this.kinematic | !this.active) {adjust = 0.5f;}

        GL11.glColor3f(1,1,1);
        currentTexture.bind();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0+adjust,0);
        GL11.glVertex2f(this.hitbox.x, this.hitbox.y);
        GL11.glTexCoord2f(0.5f+adjust,0);
        GL11.glVertex2f(this.hitbox.x+this.hitbox.width, this.hitbox.y);
        GL11.glTexCoord2f(0.5f+adjust,1);
        GL11.glVertex2f(this.hitbox.x+this.hitbox.width, this.hitbox.y+this.hitbox.height);
        GL11.glTexCoord2f(0+adjust,1);
        GL11.glVertex2f(this.hitbox.x, this.hitbox.y+this.hitbox.height);
        GL11.glEnd();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D,  0);
    }

    public void freeze()
    {
        this.kinematic = true;
    }

    public void unfreeze()
    {
        this.kinematic = false;
    }

    public COLORS getColor() {
        return color;
    }
}
